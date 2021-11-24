/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sla.handler;

import codedriver.framework.process.dto.ProcessTaskSlaTimeCostVo;
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
public class SzbankSingleStepResponseSlaCalculateHandler extends SlaCalculateHandlerBase {
    @Override
    public String getName() {
        return "单步骤响应时效计算规则（苏州银行）";
    }

    @Override
    public String getDescription() {
        return "耗时计算规则如下：<br>" +
                "1.正常一个步骤的响应耗时是开始时间减去激活时间。<br>" +
                "2.如果一个步骤被多次重新激活，则每次激活后响应耗时重新统计。";
    }

    @Override
    protected ProcessTaskSlaTimeCostVo myCalculateTimeCost(List<ProcessTaskStepTimeAuditVo> timeAuditList, long currentTimeMillis, String worktimeUuid) {
        List<Map<String, Long>> timePeriodList = timeAuditListToTimePeriodList(timeAuditList, currentTimeMillis);
        long realTimeCost = getRealTimeCost(timePeriodList);
        long timeCost = realTimeCost;
        if (StringUtils.isNotBlank(worktimeUuid)) {// 如果有工作时间，则计算实际消耗的工作时间
            timeCost = getTimeCost(timePeriodList, worktimeUuid);
        }
        ProcessTaskSlaTimeCostVo timeCostVo = new ProcessTaskSlaTimeCostVo();
        timeCostVo.setRealTimeCost(realTimeCost);
        timeCostVo.setTimeCost(timeCost);
        return timeCostVo;
    }

    /**
     * 将时效关联的步骤操作时间记录转换成时间段列表
     *
     * @param timeAuditList
     * @param currentTimeMillis
     * @return
     */
    private static List<Map<String, Long>> timeAuditListToTimePeriodList(List<ProcessTaskStepTimeAuditVo> timeAuditList, long currentTimeMillis) {
        /** 筛选出最后一次激活后的操作时间列表 **/
        List<ProcessTaskStepTimeAuditVo> lastTimeAuditList = new ArrayList<>();
        int size = timeAuditList.size();
        for (int i = size - 1; i >= 0; i--) {
            ProcessTaskStepTimeAuditVo timeAuditVo = timeAuditList.get(i);
            lastTimeAuditList.add(timeAuditVo);
            if (timeAuditVo.getActiveTimeLong() != null) {
                break;
            }
        }
        lastTimeAuditList.sort(Comparator.comparing(ProcessTaskStepTimeAuditVo::getId));

        Long startTime = null, endTime = null;
        for (ProcessTaskStepTimeAuditVo auditVo : lastTimeAuditList) {
            if (startTime == null && auditVo.getActiveTimeLong() != null) {
                startTime = auditVo.getActiveTimeLong();
            }
            if (endTime == null && auditVo.getStartTimeLong() != null) {
                endTime = auditVo.getStartTimeLong();
            }
            if (startTime != null && endTime != null) {
                break;
            }
        }
        if (endTime == null ) {
            endTime = currentTimeMillis;
        }
        if (startTime == null) {
            startTime = endTime;
        }

        Map<String, Long> newTimeMap = new HashMap<>();
        newTimeMap.put("s", startTime);
        newTimeMap.put("e", endTime);
        List<Map<String, Long>> timePeriodList = new ArrayList<>();
        timePeriodList.add(newTimeMap);
        return timePeriodList;
    }
}
