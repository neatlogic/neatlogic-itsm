/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.process.portal.widget.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.config.ConfigManager;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerBase;
import neatlogic.framework.process.constvalue.ItsmTenantConfig;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PersonalProcessTaskOverviewPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Override
    public String getHandler() {
        return "process.personalProcessTaskOverview";
    }

    @Override
    protected JSONObject getMyData(JSONObject paramObj) {
        Date startTime = paramObj.getDate("startTime");
        Date endTime = paramObj.getDate("endTime");
        if (startTime == null && endTime == null) {
            Integer timeRange = paramObj.getInteger("timeRange");
            String timeUnit = paramObj.getString("timeUnit");
            if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                startTime = TimeUtil.recentTimeTransfer(timeRange, timeUnit);
                endTime = new Date();
            }
        }
        int myTaskCount = 0;
        int todoCount = 0;
        int doingCount = 0;
        int riskCount = 0;
        int doneCount = 0;
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        List<Long> doingProcessTaskStepIdList = processTaskMapper.getProcessTaskWorkerProcessTaskStepIdListByAuthenticationInfoVoAndStartTimeAndEndTime(
                authenticationInfoVo,
                startTime,
                endTime
        );
        if (CollectionUtils.isNotEmpty(doingProcessTaskStepIdList)) {
            String slaTimeDisplayMode = ConfigManager.getConfig(ItsmTenantConfig.SLA_TIME_DISPLAY_MODE);
            int pageSize = 1000;
            for (int fromIndex = 0; fromIndex < doingProcessTaskStepIdList.size(); fromIndex += pageSize) {
                int toIndex = fromIndex + pageSize;
                List<Long> processTaskStepIdList = doingProcessTaskStepIdList.subList(fromIndex, Math.min(toIndex, doingProcessTaskStepIdList.size()));
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                Set<Long> processTaskIdSet = processTaskStepList.stream().map(ProcessTaskStepVo::getProcessTaskId).collect(Collectors.toSet());
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
                Map<Long, ProcessTaskVo> processTaskMap = processTaskList.stream().collect(Collectors.toMap(ProcessTaskVo::getId, e -> e));
                List<Long> timeoutProcessTaskStepIdList = processTaskSlaMapper.getTimeoutProcessTaskStepIdListByProcessTaskStepIdListAndSlaTimeDisplayMode(processTaskStepIdList, slaTimeDisplayMode);
                for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                    ProcessTaskVo processTaskVo = processTaskMap.get(processTaskStepVo.getProcessTaskId());
                    if (processTaskVo != null && Objects.equals(processTaskVo.getIsDeleted(), 0) && Objects.equals(processTaskVo.getStatus(), ProcessTaskStatus.RUNNING.getValue())) {
                        if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                            myTaskCount++;
                            if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.PENDING.getValue())) {
                                todoCount++;
                            } else if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())) {
                                doingCount++;
                            }
                            if (timeoutProcessTaskStepIdList.contains(processTaskStepVo.getId())) {
                                riskCount++;
                            }
                        }
                    }
                }
            }
        }
        List<Long> doneProcessTaskStepIdList = processTaskMapper.getProcessTaskStepUserProcessTaskStepIdListByUserUuidAndStatusAndStartTimeAndEndTime(
                UserContext.get().getUserUuid(),
                List.of("major", "minor"),
                List.of("done"),
                startTime,
                endTime
        );
        if (CollectionUtils.isNotEmpty(doneProcessTaskStepIdList)) {
            int pageSize = 1000;
            for (int fromIndex = 0; fromIndex < doneProcessTaskStepIdList.size(); fromIndex += pageSize) {
                int toIndex = fromIndex + pageSize;
                List<Long> processTaskStepIdList = doneProcessTaskStepIdList.subList(fromIndex, Math.min(toIndex, doneProcessTaskStepIdList.size()));
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                Set<Long> processTaskIdSet = processTaskStepList.stream().map(ProcessTaskStepVo::getProcessTaskId).collect(Collectors.toSet());
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
                Map<Long, ProcessTaskVo> processTaskMap = processTaskList.stream().collect(Collectors.toMap(ProcessTaskVo::getId, e -> e));
                for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                    ProcessTaskVo processTaskVo = processTaskMap.get(processTaskStepVo.getProcessTaskId());
                    if (processTaskVo != null && Objects.equals(processTaskVo.getIsDeleted(), 0)) {
                        if (Objects.equals(processTaskStepVo.getIsActive(), 2)) {
                            doneCount++;
                        }
                    }
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        // 我的待办
        resultObj.put("myTask", myTaskCount);
        // 可抢单
        resultObj.put("todo", todoCount);
        // 处理中
        resultObj.put("doing", doingCount);
        // 已超时
        resultObj.put("risk", riskCount);
        // 已完成
        resultObj.put("done", doneCount);
        return resultObj;
    }
}
