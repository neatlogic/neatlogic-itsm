/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sla.handler;

import codedriver.framework.process.dto.ProcessTaskStepTimeAuditVo;
import codedriver.framework.process.sla.core.SlaCalculateHandlerBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author linbq
 * @since 2021/11/22 14:59
 **/
@Component
public class DefaultSlaCalculateHandler extends SlaCalculateHandlerBase {
    @Override
    public String getName() {
        return "处理时效计算规则（出厂默认）";
    }

    @Override
    public String getDescription() {
        return "耗时计算规则如下：<br>" +
                "1.正常一个步骤的处理耗时是完成时间减去激活时间。<br>" +
                "2.如果一个步骤处理过程有暂停操作，则从暂停到恢复之间的时间段不算耗时。<br>" +
                "3.如果一个步骤被多次重新激活，则每次处理时间段都累计为耗时。<br>" +
                "4.如果两个或两个以上步骤的处理时间段有重合部分的话，则重合部分去重，只取一份时间。";
    }

    @Override
    protected long[] myCalculateTimeCost(List<ProcessTaskStepTimeAuditVo> timeAuditList, long currentTimeMillis, String worktimeUuid) {
        long[] timeCostArray = new long[2];
        List<Map<String, Long>> timePeriodList = timeAuditListToTimePeriodList(timeAuditList, currentTimeMillis);
        long realTimeCost = getRealTimeCost(timePeriodList);
        long timeCost = realTimeCost;
        if (StringUtils.isNotBlank(worktimeUuid)) {// 如果有工作时间，则计算实际消耗的工作时间
            timeCost = getTimeCost(timePeriodList, worktimeUuid);
        }
        timeCostArray[0] = realTimeCost;
        timeCostArray[1] = timeCost;
        return timeCostArray;
    }

    /**
     * 计算出时效关联的步骤已经消耗的时长（直接计算）
     *
     * @param timePeriodList
     * @return
     */
    private static long getRealTimeCost(List<Map<String, Long>> timePeriodList) {
        long sum = 0;
        for (Map<String, Long> timeMap : timePeriodList) {
            sum += timeMap.get("e") - timeMap.get("s");
        }
        return sum;
    }

    /**
     * 计算出时效关联的步骤已经消耗的时长（根据工作日历计算）
     *
     * @param timePeriodList
     * @param worktimeUuid
     * @return
     */
    private static long getTimeCost(List<Map<String, Long>> timePeriodList, String worktimeUuid) {
        long sum = 0;
        for (Map<String, Long> timeMap : timePeriodList) {
            sum += worktimeMapper.calculateCostTime(worktimeUuid, timeMap.get("s"), timeMap.get("e"));
        }
        return sum;
    }

    /**
     * 将时效关联的步骤操作时间记录转换成时间段列表
     *
     * @param timeAuditList
     * @param currentTimeMillis
     * @return
     */
    private static List<Map<String, Long>> timeAuditListToTimePeriodList(List<ProcessTaskStepTimeAuditVo> timeAuditList, long currentTimeMillis) {
        List<Map<String, Long>> timeList = new ArrayList<>();
        for (ProcessTaskStepTimeAuditVo auditVo : timeAuditList) {
            Long startTime = null, endTime = null;
            if (auditVo.getActiveTimeLong() != null) {
                startTime = auditVo.getActiveTimeLong();
            } else if (auditVo.getStartTimeLong() != null) {
                startTime = auditVo.getStartTimeLong();
            }
            if (auditVo.getCompleteTimeLong() != null) {
                endTime = auditVo.getCompleteTimeLong();
            } else if (auditVo.getAbortTimeLong() != null) {
                endTime = auditVo.getAbortTimeLong();
            } else if (auditVo.getBackTimeLong() != null) {
                endTime = auditVo.getBackTimeLong();
            } else if (auditVo.getPauseTimeLong() != null) {
                endTime = auditVo.getPauseTimeLong();
            }
            if (startTime != null && endTime != null) {
                Map<String, Long> stimeMap = new HashMap<>();
                stimeMap.put("s", startTime);
                timeList.add(stimeMap);
                Map<String, Long> etimeMap = new HashMap<>();
                etimeMap.put("e", endTime);
                timeList.add(etimeMap);
            } else if (startTime != null) {
                Map<String, Long> stimeMap = new HashMap<>();
                stimeMap.put("s", startTime);
                timeList.add(stimeMap);
                Map<String, Long> etimeMap = new HashMap<>();
                etimeMap.put("e", currentTimeMillis);
                timeList.add(etimeMap);
            }
        }
        timeList.sort((o1, o2) -> {
            Long t1 = o1.get("s");
            if (t1 == null) {
                t1 = o1.get("e");
            }
            Long t2 = o2.get("s");
            if (t2 == null) {
                t2 = o2.get("e");
            }
            return t1.compareTo(t2);
        });
        Stack<Long> timeStack = new Stack<>();
        List<Map<String, Long>> timePeriodList = new ArrayList<>();
        for (Map<String, Long> timeMap : timeList) {
            Long s = timeMap.get("s");
            Long e = timeMap.get("e");
            if (s != null) {
                timeStack.push(s);
            } else if (e != null) {
                if (!timeStack.isEmpty()) {
                    Long currentStartTimeLong = timeStack.pop();
                    if (timeStack.isEmpty()) {// 栈被清空时计算时间段
                        Map<String, Long> newTimeMap = new HashMap<>();
                        newTimeMap.put("s", currentStartTimeLong);
                        newTimeMap.put("e", e);
                        timePeriodList.add(newTimeMap);
                    }
                }
            }
        }
        return timePeriodList;
    }
}
