package neatlogic.module.process.sla.handler;

import neatlogic.framework.process.constvalue.SlaStatus;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerBase;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import org.junit.*;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

/** 固定毫秒时间验证生产区间算法；工作日历仅模拟确定的窗口交集。 */
public class DefaultSlaCalculateHandlerTest {
    private static final long HOUR = 3600000L;
    private final Access handler = new Access();
    private Object originalWorktime;

    /** 只暴露生产入口，不复制算法。 */
    private static class Access extends DefaultSlaCalculateHandler {
        ProcessTaskSlaTimeCostVo cost(List<ProcessTaskStepTimeAuditVo> rows, long now, String calendar) {
            return myCalculateTimeCost(rows, now, calendar);
        }
    }

    /** 保存基类共享字段，保证日历测试不污染其他测试。 */
    @Before public void saveSharedState() throws Exception {
        Field f = SlaCalculateHandlerBase.class.getDeclaredField("worktimeMapper");
        f.setAccessible(true); originalWorktime = f.get(null);
    }

    /** 恢复共享工作日历 Mapper。 */
    @After public void restoreSharedState() throws Exception {
        Field f = SlaCalculateHandlerBase.class.getDeclaredField("worktimeMapper");
        f.setAccessible(true); f.set(null, originalWorktime);
    }

    /** 以小时构造审计，生产入参仍为毫秒，审计 ID 明确保持顺序。 */
    private ProcessTaskStepTimeAuditVo row(long id, long step, long start, Long end) {
        ProcessTaskStepTimeAuditVo row = new ProcessTaskStepTimeAuditVo();
        row.setId(id); row.setProcessTaskStepId(step);
        row.setActiveTimeLong(start * HOUR);
        if (end != null) row.setCompleteTimeLong(end * HOUR);
        return row;
    }

    /** 无日历时两种耗时必须一致。 */
    private void hours(long expected, ProcessTaskStepTimeAuditVo... rows) {
        ProcessTaskSlaTimeCostVo cost = handler.cost(Arrays.asList(rows), 15 * HOUR, null);
        assertEquals(expected * HOUR, cost.getTimeCost());
        assertEquals(expected * HOUR, cost.getRealTimeCost());
    }

    /** 旧 while 清空栈的缺陷会把交叉并行区间算成两小时。 */
    @Test public void crossingParallelPeriodsHaveThreeHourUnion() {
        hours(3, row(1, 1, 9, 11L), row(2, 2, 10, 12L));
    }

    /** 嵌套区间不能重复计时。 */
    @Test public void nestedPeriodsCountOuterOnce() {
        hours(5, row(1, 1, 9, 14L), row(2, 2, 10, 11L), row(3, 3, 12, 13L));
    }

    /** 分离区间的间隔不计时。 */
    @Test public void separatedPeriodsExcludeGap() {
        hours(3, row(1, 1, 9, 10L), row(2, 2, 12, 14L));
    }

    /** 端点相接和零时长审计不能重复计数。 */
    @Test public void touchingAndZeroLengthPeriods() {
        hours(3, row(1, 1, 9, 10L), row(2, 2, 10, 12L), row(3, 3, 10, 10L));
    }

    /** 所有开放区间都应截止同一个查询时间，不能仅关闭最后一条。 */
    @Test public void multipleOpenPeriodsEndAtSameQueryTime() {
        hours(6, row(1, 1, 9, null), row(2, 2, 10, 11L), row(3, 3, 12, null));
    }

    /** 同一步后续激活关闭旧开放区间，其他步骤的开始不能提前截断。 */
    @Test public void nextAuditMustBelongToSameStep() {
        hours(5, row(1, 1, 9, null), row(2, 2, 10, 11L), row(3, 1, 12, 14L));
    }

    /** 后续审计同时存在激活和开始时，以较早的激活关闭旧区间。 */
    @Test public void nextActivationTakesPriorityOverStart() throws Exception {
        ProcessTaskStepTimeAuditVo first = row(1, 1, 9, null);
        ProcessTaskStepTimeAuditVo next = row(2, 1, 12, 14L);
        next.setStartTimeLong(13 * HOUR);
        // 并集求和可能掩盖错误终点，单独核对生产方法选出的边界。
        java.lang.reflect.Method boundary = DefaultSlaCalculateHandler.class.getDeclaredMethod(
                "getEndTime", ProcessTaskStepTimeAuditVo.class, List.class, long.class);
        boundary.setAccessible(true);
        assertEquals(12 * HOUR, boundary.invoke(handler, first, Arrays.asList(first, next), 15 * HOUR));
        hours(5, first, next);
    }

    /** 仅有开始时间的后续审计也能关闭旧区间。 */
    @Test public void nextStartClosesOldOpenPeriod() {
        ProcessTaskStepTimeAuditVo next = row(2, 1, 12, 13L);
        next.setActiveTimeLong(null); next.setStartTimeLong(12 * HOUR);
        hours(4, row(1, 1, 9, null), next);
    }

    /** 挂起明确结束旧段，恢复从新起点计时。 */
    @Test public void hangAndReactivateExcludeSuspendedTime() {
        ProcessTaskStepTimeAuditVo first = row(1, 1, 9, null);
        first.setPauseTimeLong(10 * HOUR);
        hours(3, first, row(2, 1, 12, 14L));
    }

    /** 空记录和只有结束时间的记录都不能产生耗时。 */
    @Test public void noEffectivePeriodsHaveZeroCost() {
        hours(0);
        ProcessTaskStepTimeAuditVo endOnly = row(1, 1, 9, 10L);
        endOnly.setActiveTimeLong(null);
        hours(0, endOnly);
    }

    /** 毫秒边界不得被截断为秒。 */
    @Test public void millisecondPrecisionIsPreserved() {
        ProcessTaskStepTimeAuditVo row = row(1, 1, 0, null);
        row.setActiveTimeLong(1001L); row.setCompleteTimeLong(1003L);
        assertEquals(2L, handler.cost(Collections.singletonList(row), 2000L, null).getTimeCost());
    }

    /** 工作窗口固定为周一到周五09到17点；自然耗时仍包含周末。 */
    @Test public void fridayToMondayCostsTwoWorkingHours() {
        long start = millis("2026-11-06T16:00:00"), end = millis("2026-11-09T10:00:00");
        List<List<Object>> calls = new ArrayList<>();
        // 仅替换数据库交集查询，记录实际入参；算法入口和汇总均真实执行。
        WorktimeMapper calendar = (WorktimeMapper) Proxy.newProxyInstance(WorktimeMapper.class.getClassLoader(),
                new Class<?>[]{WorktimeMapper.class}, (proxy, method, args) -> {
            assertEquals("calculateCostTime", method.getName());
            assertEquals("weekday", args[0]);
            calls.add(Arrays.asList(args.clone()));
            long from = (Long) args[1], to = (Long) args[2], total = 0;
            for (LocalDate day = LocalDate.of(2026, 11, 6); !day.isAfter(LocalDate.of(2026, 11, 9)); day = day.plusDays(1)) {
                if (day.getDayOfWeek().getValue() <= 5) {
                    long open = day.atTime(9, 0).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                    long close = day.atTime(17, 0).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                    total += Math.max(0, Math.min(to, close) - Math.max(from, open));
                }
            }
            return total;
        });
        handler.setWorktimeMapper(calendar);
        ProcessTaskStepTimeAuditVo row = row(1, 1, 0, null);
        row.setActiveTimeLong(start); row.setCompleteTimeLong(end);
        ProcessTaskSlaTimeCostVo result = handler.cost(Collections.singletonList(row), end, "weekday");
        assertEquals(66 * HOUR, result.getRealTimeCost());
        assertEquals(2 * HOUR, result.getTimeCost());
        assertEquals(Collections.singletonList(Arrays.asList("weekday", start, end)), calls);
    }

    /** 显式时区避免测试机时区改变算例。 */
    private long millis(String date) {
        return LocalDateTime.parse(date).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    /** 构造状态输入，与耗时审计分开验证。 */
    private ProcessTaskStepVo step(int active, String status) {
        ProcessTaskStepVo step = new ProcessTaskStepVo();
        step.setIsActive(active); step.setStatus(status); return step;
    }

    /** 自动挂起是HANG/0；其他分支仍在运行时整条SLA继续计时。 */
    @Test public void statusHandlesAutomaticHangAndParallelRunning() {
        assertEquals(SlaStatus.PAUSE, handler.getStatus(Arrays.asList(step(0, "hang"))));
        assertEquals(SlaStatus.DOING, handler.getStatus(Arrays.asList(step(0, "hang"), step(1, "running"))));
        assertEquals(SlaStatus.DOING, handler.getStatus(Arrays.asList(step(1, "pending"))));
        assertEquals(SlaStatus.PAUSE, handler.getStatus(Arrays.asList(step(-1, "running"))));
    }

    /** 正常完成标记为isActive=2；未激活步骤不应启动SLA。 */
    @Test public void statusHandlesCompletedAndUnactivatedSteps() {
        assertEquals(SlaStatus.DONE, handler.getStatus(Arrays.asList(step(2, "succeed"), step(2, "succeed"))));
        assertNull(handler.getStatus(Arrays.asList(step(0, "pending"))));
    }
}

