/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.sla.handler;

import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.constvalue.SlaStatus;
import neatlogic.framework.process.constvalue.SlaType;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeCostVo;
import neatlogic.framework.process.dto.ProcessTaskStepTimeAuditVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import neatlogic.framework.util.$;
/**
 * @author linbq
 * @since 2021/11/22 14:59
 **/
@Component
public class DefaultSlaCalculateHandler extends SlaCalculateHandlerBase {
    @Override
    public String getName() {
        return $.t("nmpsh.defaultslacalculatehandler.getname");
    }

    @Override
    public SlaType getType() {
        return SlaType.HANDLE;
    }

    @Override
    public String getDescription() {
        return $.t("nmpsh.defaultslacalculatehandler.description");
    }

    @Override
    public boolean getMultiple() {
        return true;
    }

    @Override
    public int isSum() {
        return 1;
    }

    @Override
    public SlaStatus getStatus(List<ProcessTaskStepVo> processTaskStepList) {
        int doing = 0;
        int pause = 0;
        int done = 0;
        int size = processTaskStepList.size();
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            String status = processTaskStepVo.getStatus();
            if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                // 未处理、处理中和挂起的步骤才需要计算SLA
                if (ProcessTaskStepStatus.PENDING.getValue().equals(status)) {
                    doing++;
                } else if (ProcessTaskStepStatus.RUNNING.getValue().equals(status)) {
                    doing++;
                } else if (ProcessTaskStepStatus.HANG.getValue().equals(status)) {
                    pause++;
                }
            } else if (Objects.equals(processTaskStepVo.getIsActive(), -1)) {
                pause++;
            } else if (ProcessTaskStepStatus.SUCCEED.getValue().equals(status)) {
                done++;
            }
        }
        if (doing > 0) {
            return SlaStatus.DOING;
        } else if (pause > 0) {
            return SlaStatus.PAUSE;
        } else if (done == size) {
            return SlaStatus.DONE;
        }
        return null;
    }

    @Override
    public boolean needDelete(List<ProcessTaskStepVo> processTaskStepList) {
        return false;
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
     */
    private static List<Map<String, Long>> timeAuditListToTimePeriodList(List<ProcessTaskStepTimeAuditVo> timeAuditList, long currentTimeMillis) {
        List<Map<String, Long>> timeList = new ArrayList<>();
        for (int i = 0; i < timeAuditList.size(); i++) {
            ProcessTaskStepTimeAuditVo auditVo = timeAuditList.get(i);
            Long startTime = null;
            Long endTime = null;
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
                if (i == timeAuditList.size() - 1) {
                    Map<String, Long> etimeMap = new HashMap<>();
                    etimeMap.put("e", currentTimeMillis);
                    timeList.add(etimeMap);
                }
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
                while (!timeStack.isEmpty()) {
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
