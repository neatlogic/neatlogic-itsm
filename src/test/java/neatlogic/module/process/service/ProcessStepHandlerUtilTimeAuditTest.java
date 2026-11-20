package neatlogic.module.process.service;

import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTimeAuditMapper;
import org.junit.*;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.Assert.*;

/** 审计写入回归：执行生产方法，捕获 Mapper 参数，不连接数据库。 */
public class ProcessStepHandlerUtilTimeAuditTest {
    private ProcessStepHandlerUtil service;
    private ProcessTaskStepTimeAuditMapper mapper;
    private ProcessTaskStepVo step;
    private ProcessTaskStepTimeAuditVo lastRow;
    private final List<ProcessTaskStepTimeAuditVo> inserts = new ArrayList<>();
    private final List<ProcessTaskStepTimeAuditVo> updates = new ArrayList<>();
    private final List<String> writes = new ArrayList<>();

    /** JDK 代理只接受本测试覆盖的 Mapper 方法；参数立即复制，避免后续修改掩盖错误。 */
    @Before public void setup() throws Exception {
        mapper = (ProcessTaskStepTimeAuditMapper) Proxy.newProxyInstance(
                ProcessTaskStepTimeAuditMapper.class.getClassLoader(),
                new Class<?>[]{ProcessTaskStepTimeAuditMapper.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getLastProcessTaskStepTimeAuditByStepId":
                            assertEquals(Long.valueOf(101), args[0]);
                            return lastRow;
                        case "insertProcessTaskStepTimeAudit":
                            inserts.add(snapshot((ProcessTaskStepTimeAuditVo) args[0]));
                            writes.add("insert"); return 1;
                        case "updateProcessTaskStepTimeAudit":
                            updates.add(snapshot((ProcessTaskStepTimeAuditVo) args[0]));
                            writes.add("update"); return 1;
                        default: throw new AssertionError("未配置的审计 Mapper 调用: " + method);
                    }
                });
        service = new ProcessStepHandlerUtil();
        Field field = ProcessStepHandlerUtil.class.getDeclaredField("processTaskStepTimeAuditMapper");
        field.setAccessible(true); field.set(service, mapper);
        step = new ProcessTaskStepVo(); step.setId(101L); step.setStatus("pending");
    }

    /** 复制身份和六类时间字段，保存调用当时的写入内容。 */
    private ProcessTaskStepTimeAuditVo snapshot(ProcessTaskStepTimeAuditVo source) {
        ProcessTaskStepTimeAuditVo row = new ProcessTaskStepTimeAuditVo();
        row.setId(source.getId()); row.setProcessTaskStepId(source.getProcessTaskStepId());
        row.setActiveTime(source.getActiveTime()); row.setStartTime(source.getStartTime());
        row.setCompleteTime(source.getCompleteTime()); row.setAbortTime(source.getAbortTime());
        row.setBackTime(source.getBackTime()); row.setPauseTime(source.getPauseTime());
        return row;
    }

    /** 循环中的每个场景独立，不沿用上一场景的历史记录或调用。 */
    private void resetHistory() {
        lastRow = null; inserts.clear(); updates.clear(); writes.clear();
    }

    /** 构造已有审计；字段值代表数据库已记录的时间。 */
    private ProcessTaskStepTimeAuditVo old() {
        lastRow = new ProcessTaskStepTimeAuditVo();
        lastRow.setId(1L); lastRow.setProcessTaskStepId(101L);
        return lastRow;
    }

    /** 使用真实雪花 ID，只验证身份有效且没有覆盖旧记录，不依赖具体时钟值。 */
    private ProcessTaskStepTimeAuditVo inserted() {
        assertEquals(1, inserts.size());
        ProcessTaskStepTimeAuditVo row = inserts.get(0);
        assertEquals(Long.valueOf(101), row.getProcessTaskStepId());
        assertNotNull(row.getId()); assertTrue(row.getId() > 1L);
        return row;
    }

    /** 更新必须保留旧记录 ID 和步骤 ID。 */
    private ProcessTaskStepTimeAuditVo updated() {
        assertEquals(1, updates.size());
        ProcessTaskStepTimeAuditVo row = updates.get(0);
        assertEquals(Long.valueOf(101), row.getProcessTaskStepId());
        assertEquals(Long.valueOf(1), row.getId());
        return row;
    }

    /** 激活、撤回、重做均创建本轮起点；自动开始在同一行记录开始时间。 */
    @Test public void activationRetreatRedoRespectStatus() {
        for (IOperationType op : new IOperationType[]{ProcessTaskStepOperationType.STEP_ACTIVE,
                ProcessTaskStepOperationType.STEP_RETREAT, ProcessTaskStepOperationType.STEP_REDO}) {
            for (String status : new String[]{"pending", "running"}) {
                resetHistory(); step.setStatus(status);
                service.timeAudit(step, op);
                ProcessTaskStepTimeAuditVo row = inserted();
                assertEquals("now", row.getActiveTime());
                assertEquals("running".equals(status) ? "now" : null, row.getStartTime());
                assertTrue(updates.isEmpty());
            }
        }
    }

    /** 已激活但未开始的记录应原地补开始时间。 */
    @Test public void startUpdatesActivation() {
        old().setActiveTime("recorded");
        service.timeAudit(step, ProcessTaskStepOperationType.STEP_START);
        assertEquals("now", updated().getStartTime());
        assertTrue(inserts.isEmpty());
    }

    /** 无记录或已有开始时间时，重新开始应插入新记录。 */
    @Test public void startInsertsWhenMissingOrAlreadyStarted() {
        for (boolean exists : new boolean[]{false, true}) {
            resetHistory();
            if (exists) old().setStartTime("recorded");
            service.timeAudit(step, ProcessTaskStepOperationType.STEP_START);
            assertEquals("now", inserted().getStartTime());
            assertTrue(updates.isEmpty());
        }
    }

    /** 完成应关闭原有计时段。 */
    @Test public void completeClosesExistingPeriod() {
        old().setActiveTime("recorded");
        service.timeAudit(step, ProcessTaskStepOperationType.STEP_COMPLETE);
        assertEquals("now", updated().getCompleteTime());
        assertTrue(inserts.isEmpty());
    }

    /** 完成兜底插入应为零时长闭合段，不能留下开放审计。 */
    @Test public void completeFallbackInsertsAllThreeTimes() {
        for (boolean exists : new boolean[]{false, true}) {
            resetHistory();
            if (exists) old().setCompleteTime("recorded");
            service.timeAudit(step, ProcessTaskStepOperationType.STEP_COMPLETE);
            ProcessTaskStepTimeAuditVo row = inserted();
            assertEquals("now", row.getActiveTime());
            assertEquals("now", row.getStartTime());
            assertEquals("now", row.getCompleteTime());
            assertTrue(updates.isEmpty());
        }
    }

    /** 四类结束事件在无记录、开放记录、已有结束字段时都不应落空。 */
    @Test public void closingEventsInsertOrUpdateCorrectly() throws Exception {
        IOperationType[] ops = {ProcessTaskStepOperationType.STEP_HANG, ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskOperationType.PROCESSTASK_ABORT, ProcessTaskStepOperationType.STEP_BACK};
        String[] fields = {"PauseTime", "PauseTime", "AbortTime", "BackTime"};
        for (int op = 0; op < ops.length; op++) {
            for (int state = 0; state < 3; state++) {
                resetHistory();
                if (state > 0) {
                    ProcessTaskStepTimeAuditVo row = old();
                    if (state == 2) row.getClass().getMethod("set" + fields[op], String.class).invoke(row, "recorded");
                }
                service.timeAudit(step, ops[op]);
                ProcessTaskStepTimeAuditVo written = state == 1 ? updated() : inserted();
                assertEquals("now", written.getClass().getMethod("get" + fields[op]).invoke(written));
                if (state == 1) assertTrue(inserts.isEmpty());
                else assertTrue(updates.isEmpty());
            }
        }
    }

    /** 恢复到待处理或处理中应建立相应起点，保留旧暂停记录。 */
    @Test public void recoverCreatesNewPeriodAfterPause() {
        for (String status : new String[]{"pending", "running"}) {
            resetHistory(); step.setStatus(status);
            ProcessTaskStepTimeAuditVo row = old();
            row.setActiveTime("recorded"); row.setStartTime("recorded"); row.setPauseTime("recorded");
            service.timeAudit(step, ProcessTaskOperationType.PROCESSTASK_RECOVER);
            ProcessTaskStepTimeAuditVo result = inserted();
            assertEquals("pending".equals(status) ? "now" : null, result.getActiveTime());
            assertEquals("running".equals(status) ? "now" : null, result.getStartTime());
        }
    }

    /** 恢复时没有旧记录也能建立新计时段。 */
    @Test public void recoverWithoutHistory() {
        for (String status : new String[]{"pending", "running"}) {
            resetHistory(); step.setStatus(status);
            service.timeAudit(step, ProcessTaskOperationType.PROCESSTASK_RECOVER);
            ProcessTaskStepTimeAuditVo row = inserted();
            assertEquals("now", "pending".equals(status) ? row.getActiveTime() : row.getStartTime());
        }
    }

    /** 恢复到处理中时，已有未写开始时间的记录按当前规则原地补齐。 */
    @Test public void recoverUpdatesMissingStart() {
        old(); step.setStatus("running");
        service.timeAudit(step, ProcessTaskOperationType.PROCESSTASK_RECOVER);
        assertEquals("now", updated().getStartTime());
    }

    /** 转交先关闭旧段，再按转交后状态创建新段，并保留旧记录的步骤关联。 */
    @Test public void transferClosesOldBeforeOpeningNew() {
        for (String status : new String[]{"pending", "running"}) {
            resetHistory(); old(); step.setStatus(status);
            service.timeAudit(step, ProcessTaskStepOperationType.STEP_TRANSFER);
            assertEquals("now", updated().getAbortTime());
            ProcessTaskStepTimeAuditVo row = inserted();
            assertEquals("now", row.getActiveTime());
            assertEquals("running".equals(status) ? "now" : null, row.getStartTime());
            assertEquals(Arrays.asList("update", "insert"), writes);
        }
    }

    /** 无旧审计或旧段已终止时，转交不能重复关闭旧段。 */
    @Test public void transferWithoutOpenHistory() {
        for (boolean exists : new boolean[]{false, true}) {
            resetHistory();
            if (exists) old().setAbortTime("recorded");
            service.timeAudit(step, ProcessTaskStepOperationType.STEP_TRANSFER);
            assertEquals("now", inserted().getActiveTime());
            assertTrue(updates.isEmpty());
        }
    }
}

