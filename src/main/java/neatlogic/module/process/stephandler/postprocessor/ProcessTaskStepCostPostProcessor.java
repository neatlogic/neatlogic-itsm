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

package neatlogic.module.process.stephandler.postprocessor;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.stephandler.core.IProcessTaskOperatePostProcessor;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTimeAuditMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class ProcessTaskStepCostPostProcessor implements IProcessTaskOperatePostProcessor {

    @Resource
    private ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public void postProcessAfterProcessTaskStepOperate(ProcessTaskStepVo currentProcessTaskStepVo, IOperationType operationType) {
        Long processTaskId = currentProcessTaskStepVo.getProcessTaskId();
        Long processTaskStepId = currentProcessTaskStepVo.getId();
        JSONObject otherParam = currentProcessTaskStepVo.getParamObj();
        List<IOperationType> list = new ArrayList<>();
        list.add(ProcessTaskStepOperationType.STEP_ACTIVE);
        list.add(ProcessTaskStepOperationType.STEP_ACCEPT);
        list.add(ProcessTaskStepOperationType.STEP_START);
        list.add(ProcessTaskStepOperationType.STEP_COMPLETE);
        list.add(ProcessTaskStepOperationType.STEP_BACK);
        list.add(ProcessTaskStepOperationType.STEP_PAUSE);
        list.add(ProcessTaskStepOperationType.STEP_TRANSFER);
        list.add(ProcessTaskStepOperationType.STEP_RECOVER);
        list.add(ProcessTaskStepOperationType.STEP_RETREAT);
        list.add(ProcessTaskStepOperationType.STEP_REDO);
        list.add(ProcessTaskStepOperationType.STEP_REAPPROVAL);
        if (list.contains(operationType)) {
            Date operateTime = otherParam.getDate("operateTime");
            if (operateTime == null) {
                operateTime = new Date();
            }
            String stepStatus = currentProcessTaskStepVo.getStatus();
            if (ProcessTaskStepOperationType.STEP_COMPLETE == operationType || ProcessTaskStepOperationType.STEP_BACK == operationType) {
                List<ProcessTaskStepCostWorkerVo> workerList = new ArrayList<>();
                List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
                if (CollectionUtils.isNotEmpty(processTaskStepUserList)) {
                    for (ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepUserList) {
                        ProcessTaskStepCostWorkerVo workerVo = new ProcessTaskStepCostWorkerVo();
                        workerVo.setType(GroupSearch.USER.getValue());
                        workerVo.setUuid(processTaskStepUserVo.getUserUuid());
                        workerList.add(workerVo);
                    }
                }
                saveProcessTaskStepCost(processTaskId, processTaskStepId, operationType, operateTime, stepStatus, workerList);
            } else {
                List<ProcessTaskStepCostWorkerVo> workerList = new ArrayList<>();
                List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerListByProcessTaskStepIdListAndUserType(Collections.singletonList(processTaskStepId), ProcessUserType.MAJOR.getValue());
                if (CollectionUtils.isNotEmpty(processTaskStepWorkerList)) {
                    for (ProcessTaskStepWorkerVo processTaskStepWorkerVo : processTaskStepWorkerList) {
                        ProcessTaskStepCostWorkerVo workerVo = new ProcessTaskStepCostWorkerVo();
                        workerVo.setType(processTaskStepWorkerVo.getType());
                        workerVo.setUuid(processTaskStepWorkerVo.getUuid());
                        workerList.add(workerVo);
                    }
                }
                saveProcessTaskStepCost(processTaskId, processTaskStepId, operationType, operateTime, stepStatus, workerList);
            }
        }
    }

    private void saveProcessTaskStepCost(Long processTaskId, Long processTaskStepId, IOperationType operationType, Date operateTime, String stepStatus, List<ProcessTaskStepCostWorkerVo> workerList) {
        String startUserUuid = UserContext.get().getUserUuid();
        ProcessTaskStepCostVo lastProcessTaskStepCostVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepCostByProcessTaskStepId(processTaskStepId);
        if (lastProcessTaskStepCostVo != null) {
            lastProcessTaskStepCostVo.setEndOperate(operationType.getValue());
            lastProcessTaskStepCostVo.setEndStatus(stepStatus);
            lastProcessTaskStepCostVo.setEndTime(operateTime);
            lastProcessTaskStepCostVo.setEndUserUuid(startUserUuid);
            long realtimeCost = operateTime.getTime() - lastProcessTaskStepCostVo.getStartTime().getTime();
            long timeCost = realtimeCost;
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
            if (processTaskVo.getWorktimeUuid() != null) {
                timeCost = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(), lastProcessTaskStepCostVo.getStartTime().getTime(), operateTime.getTime());
            }
            lastProcessTaskStepCostVo.setTimeCost(timeCost);
            lastProcessTaskStepCostVo.setRealTimeCost(realtimeCost);
            doSaveProcessTaskStepCost(lastProcessTaskStepCostVo, workerList);
        }
        ProcessTaskStepCostVo processTaskStepCostVo = new ProcessTaskStepCostVo();
        processTaskStepCostVo.setProcessTaskId(processTaskId);
        processTaskStepCostVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepCostVo.setStartOperate(operationType.getValue());
        processTaskStepCostVo.setStartStatus(stepStatus);
        processTaskStepCostVo.setStartTime(operateTime);
        processTaskStepCostVo.setStartUserUuid(startUserUuid);
        doSaveProcessTaskStepCost(processTaskStepCostVo, workerList);
    }

    private void doSaveProcessTaskStepCost(ProcessTaskStepCostVo processTaskStepCostVo, List<ProcessTaskStepCostWorkerVo> workderList) {
        String operateType = "end";
        Long id = processTaskStepCostVo.getId();
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
            operateType = "start";
            processTaskStepCostVo.setId(id);
            processTaskStepTimeAuditMapper.insertProcessTaskStepCost(processTaskStepCostVo);
        } else {
            processTaskStepTimeAuditMapper.updateProcessTaskStepCost(processTaskStepCostVo);
        }
        if (CollectionUtils.isNotEmpty(workderList)) {
            for (ProcessTaskStepCostWorkerVo processTaskStepCostWorkerVo : workderList) {
                processTaskStepCostWorkerVo.setId(SnowflakeUtil.uniqueLong());
                processTaskStepCostWorkerVo.setCostId(id);
                processTaskStepCostWorkerVo.setOperateType(operateType);
                processTaskStepTimeAuditMapper.insertProcessTaskStepCostWorker(processTaskStepCostWorkerVo);
            }
        }
    }
}
