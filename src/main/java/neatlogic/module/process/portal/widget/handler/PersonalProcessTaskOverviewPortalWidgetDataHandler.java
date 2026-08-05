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
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerBase;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import neatlogic.module.process.service.ProcessTaskService;
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

    @Resource
    private ProcessTaskService processTaskService;

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
        {
            AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
//            long startTimeA = System.currentTimeMillis();
            List<Long> allProcessTaskStepIdList = processTaskMapper.getProcessTaskWorkerProcessTaskStepIdListByAuthenticationInfoVo(authenticationInfoVo);
//            System.out.println("costTimeA = " + (System.currentTimeMillis() - startTimeA));
//            System.out.println("processTaskStepIdList.size() = " + processTaskStepIdList.size());
            if (CollectionUtils.isNotEmpty(allProcessTaskStepIdList)) {
                int count = 0;
                Map<Long, ProcessTaskVo> processTaskMap = new HashMap<>();
                List<Long> allHasSlaProcessTaskStepIdList = new ArrayList<>();
                List<ProcessTaskStepVo> allProcessTaskStepList = new ArrayList<>();
                int pageSize = 1000;
                for (int fromIndex = 0; fromIndex < allProcessTaskStepIdList.size(); fromIndex += pageSize) {
//                    long startTimeB = System.currentTimeMillis();
                    int toIndex = fromIndex + pageSize;
                    List<Long> processTaskStepIdList = allProcessTaskStepIdList.subList(fromIndex, Math.min(toIndex, allProcessTaskStepIdList.size()));
                    List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
                    allProcessTaskStepList.addAll(processTaskStepList);
//                    System.out.println("costTimeB = " + (System.currentTimeMillis() - startTimeB));
//                    System.out.println("processTaskStepList.size() = " + processTaskStepList.size());
                    Set<Long> processTaskIdSet = processTaskStepList.stream().map(ProcessTaskStepVo::getProcessTaskId).collect(Collectors.toSet());
//                    long startTimeC = System.currentTimeMillis();
                    List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
//                    System.out.println("costTimeC = " + (System.currentTimeMillis() - startTimeC));
                    for (ProcessTaskVo processTaskVo : processTaskList) {
                        processTaskMap.put(processTaskVo.getId(), processTaskVo);
                    }
                    List<Long> hasSlaProcessTaskStepIdList = processTaskSlaMapper.getHasSlaProcessTaskStepIdListByProcessTaskStepIdList(processTaskStepIdList);
                    allHasSlaProcessTaskStepIdList.addAll(hasSlaProcessTaskStepIdList);
                }

                for (ProcessTaskStepVo processTaskStepVo : allProcessTaskStepList) {
                    ProcessTaskVo processTaskVo = processTaskMap.get(processTaskStepVo.getProcessTaskId());
                    if (processTaskVo != null && Objects.equals(processTaskVo.getIsDeleted(), 0) && Objects.equals(processTaskVo.getStatus(), ProcessTaskStatus.RUNNING.getValue())) {
                        if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                            myTaskCount++;
                            if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.PENDING.getValue())) {
                                todoCount++;
                            } else if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())) {
                                doingCount++;
                            }
                            if (count > 100) {
                                continue;
                            }
                            count++;
                            if (allHasSlaProcessTaskStepIdList.contains(processTaskStepVo.getId())) {
//                            long startTimeD = System.currentTimeMillis();
                                List<ProcessTaskSlaTimeVo> slaTimeList = processTaskService.getSlaTimeListByProcessTaskStepId(processTaskStepVo.getId());
//                            System.out.println("costTimeD = " + (System.currentTimeMillis() - startTimeD));
                                if (CollectionUtils.isNotEmpty(slaTimeList)) {
                                    for (ProcessTaskSlaTimeVo processTaskSlaTimeVo : slaTimeList) {
                                        if (Objects.equals(processTaskSlaTimeVo.getSlaTimeDisplayMode(), "naturalTime")) {
                                            if (processTaskSlaTimeVo.getRealTimeLeft() < 0) {
                                                riskCount++;
                                            }
                                        } else if (Objects.equals(processTaskSlaTimeVo.getSlaTimeDisplayMode(), "naturalTime")) {
                                            if (processTaskSlaTimeVo.getTimeLeft() < 0) {
                                                riskCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        {
//            long startTimeE = System.currentTimeMillis();
            List<Long> processTaskStepIdList = processTaskMapper.getProcessTaskStepUserProcessTaskStepIdListByUserUuidAndStatusAndStartTimeAndEndTime(
                    UserContext.get().getUserUuid(),
                    List.of("major", "minor"),
                    List.of("done"),
                    startTime,
                    endTime
            );
//            System.out.println("costTimeE = " + (System.currentTimeMillis() - startTimeE));
            if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
//                long startTimeF = System.currentTimeMillis();
                List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
//                System.out.println("costTimeF = " + (System.currentTimeMillis() - startTimeF));
                Set<Long> processTaskIdSet = processTaskStepList.stream().map(ProcessTaskStepVo::getProcessTaskId).collect(Collectors.toSet());
//                long startTimeG = System.currentTimeMillis();
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
//                System.out.println("costTimeG = " + (System.currentTimeMillis() - startTimeG));
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
