/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
    public SlaType getType() {
        return SlaType.HANDLE;
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
            if (Objects.equals(processTaskStepVo.getIsActive(),1)) {
                // 未处理、处理中和挂起的步骤才需要计算SLA
                if (ProcessTaskStepStatus.PENDING.getValue().equals(status)) {
                    doing++;
                    continue;
                } else if (ProcessTaskStepStatus.RUNNING.getValue().equals(status)) {
                    doing++;
                    continue;
                } else if (ProcessTaskStepStatus.HANG.getValue().equals(status)) {
                    pause++;
                    continue;
                }
            } else if (Objects.equals(processTaskStepVo.getIsActive(),-1)) {
                pause++;
            } else if (ProcessTaskStepStatus.SUCCEED.getValue().equals(status)) {
                done++;
            }
        }
        if (doing > 0) {
            return SlaStatus.DOING;
        } else if (pause > 0) {
            return SlaStatus.PAUSE;
        } else if (done == size){
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
