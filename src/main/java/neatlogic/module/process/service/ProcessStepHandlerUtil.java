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

package neatlogic.module.process.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.TransactionSynchronizationPool;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.config.ConfigManager;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleTeamVo;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.AttributeDataVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormAttributeDataException;
import neatlogic.framework.form.exception.FormAttributeRequiredException;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.audithandler.core.IProcessTaskAuditType;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.crossover.IProcessStepHandlerCrossoverUtil;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.processtask.*;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.stepremind.core.IProcessTaskStepRemindType;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import neatlogic.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import neatlogic.framework.util.FormUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTimeAuditMapper;
import neatlogic.module.process.thread.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessStepHandlerUtil implements IProcessStepHandlerUtil, IProcessStepHandlerCrossoverUtil {
    private final Logger logger = LoggerFactory.getLogger(ProcessStepHandlerUtil.class);
    @Resource
    private ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;
    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ProcessTagMapper processTagMapper;
    @Resource
    private FormMapper formMapper;

    /**
     * @Description: 触发动作
     * @Author: linbq
     * @Date: 2021/1/20 16:15
     * @Returns:void
     */
    @Override
    public void action(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger) {
        TransactionSynchronizationPool.execute(new ProcessTaskActionThread(currentProcessTaskStepVo, trigger));
    }

    /**
     * @Description: 触发通知
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Returns:void
     */
    @Override
    public void notify(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger) {
        TransactionSynchronizationPool.execute(new ProcessTaskNotifyThread(currentProcessTaskStepVo, trigger));
    }

    /**
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo, boolean isAsync) {
        calculateSla(currentProcessTaskVo, null, isAsync);
    }

    /**
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo) {
        calculateSla(currentProcessTaskVo, null, true);
    }

    /**
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskStepVo currentProcessTaskStepVo) {
        calculateSla(null, currentProcessTaskStepVo, true);
    }

    /**
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Returns:void
     */
    private void calculateSla(ProcessTaskVo currentProcessTaskVo, ProcessTaskStepVo currentProcessTaskStepVo, boolean isAsync) {
        if (!isAsync) {
            new ProcessTaskSlaThread(currentProcessTaskVo, currentProcessTaskStepVo).execute();
        } else {
            TransactionSynchronizationPool.execute(new ProcessTaskSlaThread(currentProcessTaskVo, currentProcessTaskStepVo));
        }
    }

    /**
     * @Description: 记录操作时间
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Returns:void
     */
    @Override
    public void timeAudit(ProcessTaskStepVo currentProcessTaskStepVo, IOperationType action) {
        ProcessTaskStepTimeAuditVo lastTimeAuditVo = processTaskStepTimeAuditMapper
                .getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
        ProcessTaskStepTimeAuditVo newAuditVo = new ProcessTaskStepTimeAuditVo();
        newAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
        if (action == ProcessTaskStepOperationType.STEP_ACTIVE) {
            newAuditVo.setActiveTime("now");
            processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
        } else if (action == ProcessTaskStepOperationType.STEP_START) {
            newAuditVo.setStartTime("now");
            if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getStartTime())) {
                processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
            } else if (StringUtils.isBlank(lastTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
                newAuditVo.setId(lastTimeAuditVo.getId());
                processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
            }
        } else if (action == ProcessTaskStepOperationType.STEP_COMPLETE) {
            /* 如果找不到审计记录并且completetime不为空，则新建审计记录 **/
            newAuditVo.setCompleteTime("now");
            if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getCompleteTime())) {
                processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
            } else if (StringUtils.isBlank(lastTimeAuditVo.getCompleteTime())) {// 如果completetime为空，则更新completetime
                newAuditVo.setId(lastTimeAuditVo.getId());
                processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
            }
        } else if (action == ProcessTaskOperationType.PROCESSTASK_ABORT) {
            /* 如果找不到审计记录并且aborttime不为空，则新建审计记录 **/
            newAuditVo.setAbortTime("now");
            if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getAbortTime())) {
                processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
            } else if (StringUtils.isBlank(lastTimeAuditVo.getAbortTime())) {// 如果aborttime为空，则更新aborttime
                newAuditVo.setId(lastTimeAuditVo.getId());
                processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
            }
        } else if (action == ProcessTaskStepOperationType.STEP_BACK) {
            /* 如果找不到审计记录并且backtime不为空，则新建审计记录 **/
            newAuditVo.setBackTime("now");
            if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getBackTime())) {
                processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
            } else if (StringUtils.isBlank(lastTimeAuditVo.getBackTime())) {// 如果backtime为空，则更新backtime
                newAuditVo.setId(lastTimeAuditVo.getId());
                processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
            }
        } else if (action == ProcessTaskOperationType.PROCESSTASK_RECOVER) {
            if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStepStatus.PENDING.getValue())) {
                newAuditVo.setActiveTime("now");
                if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getActiveTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                }
            } else if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStepStatus.RUNNING.getValue())) {
                newAuditVo.setStartTime("now");
                if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getStartTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(lastTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
                    newAuditVo.setId(lastTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
            }
        } else if (action == ProcessTaskStepOperationType.STEP_PAUSE) {
            /* 如果找不到审计记录并且pausetime不为空，则新建审计记录 **/
            newAuditVo.setPauseTime("now");
            if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getPauseTime())) {
                processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
            } else if (StringUtils.isBlank(lastTimeAuditVo.getPauseTime())) {// 如果pausetime为空，则更新pausetime
                newAuditVo.setId(lastTimeAuditVo.getId());
                processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
            }
        }
    }

    /**
     * @Description: 记录操作活动
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Returns:void
     */
    @Override
    public void audit(ProcessTaskStepVo currentProcessTaskStepVo, IProcessTaskAuditType action) {
        TransactionSynchronizationPool.execute(new ProcessTaskAuditThread(currentProcessTaskStepVo, action));
    }

    /**
     * @Description: 自动评分
     * @Author: linbq
     * @Date: 2021/1/20 16:22
     * @Returns:void
     */
    @Override
    public void autoScore(ProcessTaskVo currentProcessTaskVo) {
        TransactionSynchronizationPool.execute(new ProcessTaskAutoScoreThread(currentProcessTaskVo));
    }

    /**
     * @Description: 获取验证基本信息数据是否合法，并验证
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     */
    @Override
    public boolean baseInfoValidFromDb(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskVo processTaskVo =
                processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
        baseInfoValid(currentProcessTaskStepVo, processTaskVo);
        return true;
    }

    /**
     * @Description: 验证基本信息数据是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     */
    private boolean baseInfoValid(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskVo processTaskVo) {
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        if (processTaskVo.getTitle() == null) {
            throw new ProcessTaskTitleIsEmptyException();
        }
        /* 标题不限制输入
         * Pattern titlePattern = Pattern.compile("^[A-Za-z_\\d\\u4e00-\\u9fa5]+$"); if
         * (!titlePattern.matcher(processTaskVo.getTitle()).matches()) { throw new
         * ProcessTaskRuntimeException("工单标题格式不对"); }
         */
        paramObj.put(ProcessTaskAuditDetailType.TITLE.getParamName(), processTaskVo.getTitle());
        if (StringUtils.isBlank(processTaskVo.getOwner())) {
            throw new ProcessTaskOwnerIsEmptyException();
        }
        if (userMapper.getUserBaseInfoByUuid(processTaskVo.getOwner()) == null) {
            throw new UserNotFoundException(processTaskVo.getOwner());
        }
        if (StringUtils.isNotBlank(processTaskVo.getReporter())) {
            if (userMapper.getUserBaseInfoByUuid(processTaskVo.getReporter()) == null) {
                throw new UserNotFoundException(processTaskVo.getReporter());
            }
        }
        ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
        if (Objects.equals(channelVo.getIsActivePriority(), 1)) {
            List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(processTaskVo.getChannelUuid());
            if (StringUtils.isBlank(processTaskVo.getPriorityUuid())) {
                throw new ProcessTaskPriorityIsEmptyException();
            }
            if (channelPriorityList.stream().noneMatch(o -> o.getPriorityUuid().equals(processTaskVo.getPriorityUuid()))) {
                throw new ProcessTaskPriorityNotMatchException();
            }
            paramObj.put(ProcessTaskAuditDetailType.PRIORITY.getParamName(), processTaskVo.getPriorityUuid());
        } else {
            processTaskVo.setPriorityUuid(null);
        }


        // 获取上报描述内容
//        List<Long> fileIdList = new ArrayList<>();
//        List<ProcessTaskStepContentVo> processTaskStepContentList =
//                processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(currentProcessTaskStepVo.getId());
//        for (ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
//            if (ProcessTaskOperationType.TASK_START.getValue().equals(processTaskStepContent.getType())) {
//                paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), selectContentByHashMapper
//                        .getProcessTaskContentStringByHash(processTaskStepContent.getContentHash()));
//                fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepContent.getId());
//                break;
//            }
//        }
//
//        if (CollectionUtils.isNotEmpty(fileIdList)) {
//            for (Long fileId : fileIdList) {
//                if (fileMapper.getFileById(fileId) == null) {
//                    throw new FileNotFoundException(fileId);
//                }
//            }
//            paramObj.put(ProcessTaskAuditDetailType.FILE.getParamName(), JSON.toJSONString(fileIdList));
//        }
//        currentProcessTaskStepVo.setParamObj(paramObj);
        return true;
    }

    /**
     * @Description: 验证前置步骤指派处理人是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     */
    @Override
    public boolean assignWorkerValid(ProcessTaskStepVo currentProcessTaskStepVo) {
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        // 前置步骤指派处理人
        // "assignWorkerList": [
        // {
        // "processTaskStepId": 1,
        // "processStepUuid": "abc",
        // "workerList": [
        // "user#xxx",
        // "team#xxx",
        // "role#xxx"
        // ]
        // }
        // ]
        Map<Long, List<String>> assignWorkerMap = new HashMap<>();
        Long processTaskId = currentProcessTaskStepVo.getProcessTaskId();
        Long nextStepId = paramObj.getLong("nextStepId");
        JSONArray assignWorkerList = paramObj.getJSONArray("assignWorkerList");
        if (CollectionUtils.isNotEmpty(assignWorkerList)) {
            for (int i = 0; i < assignWorkerList.size(); i++) {
                JSONObject assignWorker = assignWorkerList.getJSONObject(i);
                Long processTaskStepId = assignWorker.getLong("processTaskStepId");
                if (processTaskStepId == null) {
                    String processStepUuid = assignWorker.getString("processStepUuid");
                    if (processStepUuid != null) {
                        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskIdAndProcessStepUuid(processTaskId, processStepUuid);
                        if (processTaskStepVo != null) {
                            processTaskStepId = processTaskStepVo.getId();
                            assignWorker.put("processTaskStepId", processTaskStepId);
                        }
                    }
                }
                if (processTaskStepId != null) {
                    assignWorkerMap.put(processTaskStepId,
                            JSON.parseArray(assignWorker.getString("workerList"), String.class));
                }
            }
        }

        // 获取可分配处理人的步骤列表
        ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        processTaskStepWorkerPolicyVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
        if (CollectionUtils.isEmpty(processTaskStepWorkerPolicyList)) {
            return true;
        }
        int isOnlyOnceExecute = Integer.parseInt(ConfigManager.getConfig(ItsmTenantConfig.PROCESSTASK_WORKERPOLICY_ISONLYONCEEXECUTE));
//        IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(WorkerPolicy.PRESTEPASSIGN.getValue());
//        if (workerPolicyHandler == null) {
//            isOnlyOnceExecute = workerPolicyHandler.isOnlyOnceExecute();
//        }
        for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
            if (!WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                continue;
            }
            JSONObject configObj = workerPolicyVo.getConfigObj();
            if (MapUtils.isEmpty(configObj)) {
                continue;
            }

            JSONArray processStepList = configObj.getJSONArray("processStepList");
            if (CollectionUtils.isNotEmpty(processStepList)) {
                for (int i = 0; i < processStepList.size(); i++) {
                    JSONObject processStepObj = processStepList.getJSONObject(i);
                    String processStepUuid = processStepObj.getString("uuid");
                    if (!Objects.equals(currentProcessTaskStepVo.getProcessStepUuid(), processStepUuid)) {
                        continue;
                    }
                    List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                    if (CollectionUtils.isNotEmpty(majorList) && isOnlyOnceExecute == 1) {
                        break;
                    }
                    List<String> workerList = assignWorkerMap.get(workerPolicyVo.getProcessTaskStepId());
                    if (CollectionUtils.isEmpty(workerList)) {
                        Integer isRequired = configObj.getInteger("isRequired");
                        if (!Objects.equals(isRequired, 1)) {
                            break;
                        }
                        List<String> nextStepUuidList = new ArrayList<>();
                        JSONArray nextStepUuidArray = processStepObj.getJSONArray("condition");
                        if (CollectionUtils.isNotEmpty(nextStepUuidArray)) {
                            nextStepUuidList = nextStepUuidArray.toJavaList(String.class);
                        }
                        List<Long> nextStepIdList = getNextStepIdList(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), nextStepUuidList);
                        if (CollectionUtils.isEmpty(nextStepIdList)) {
                            break;
                        }
                        if (nextStepId != null && !nextStepIdList.contains(nextStepId)) {
                            break;
                        }
                        ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                        throw new ProcessTaskMustBeAssignWorkerException(assignableWorkerStep.getName());
                    }
                    ProcessTaskAssignWorkerVo assignWorkerVo = new ProcessTaskAssignWorkerVo();
                    assignWorkerVo.setProcessTaskId(workerPolicyVo.getProcessTaskId());
                    assignWorkerVo.setProcessTaskStepId(workerPolicyVo.getProcessTaskStepId());
                    assignWorkerVo.setFromProcessTaskStepId(currentProcessTaskStepVo.getId());
                    assignWorkerVo.setFromProcessStepUuid(currentProcessTaskStepVo.getProcessStepUuid());
                    processTaskMapper.deleteProcessTaskAssignWorker(assignWorkerVo);
                    for (String worker : workerList) {
                        assignWorkerVo.setType(GroupSearch.getPrefix(worker));
                        assignWorkerVo.setUuid(GroupSearch.removePrefix(worker));
                        processTaskMapper.insertProcessTaskAssignWorker(assignWorkerVo);
                    }
                }
            } else {
                JSONArray processStepUuidList = configObj.getJSONArray("processStepUuidList");
                if (CollectionUtils.isEmpty(processStepUuidList)) {
                    continue;
                }
                for (String processStepUuid : processStepUuidList.toJavaList(String.class)) {
                    if (!currentProcessTaskStepVo.getProcessStepUuid().equals(processStepUuid)) {
                        continue;
                    }
                    List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                    if (CollectionUtils.isNotEmpty(majorList) && isOnlyOnceExecute == 1) {
                        break;
                    }
                    List<String> workerList = assignWorkerMap.get(workerPolicyVo.getProcessTaskStepId());
                    if (CollectionUtils.isEmpty(workerList)) {
                        Integer isRequired = configObj.getInteger("isRequired");
                        if (!Objects.equals(isRequired, 1)) {
                            break;
                        }
                        List<Long> nextStepIdList = getNextStepIdList(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), workerPolicyVo.getProcessTaskStepId());
                        if (CollectionUtils.isEmpty(nextStepIdList)) {
                            break;
                        }
                        if (nextStepId != null && !nextStepIdList.contains(nextStepId)) {
                            break;
                        }
                        ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                        throw new ProcessTaskMustBeAssignWorkerException(assignableWorkerStep.getName());
                    }
                    ProcessTaskAssignWorkerVo assignWorkerVo = new ProcessTaskAssignWorkerVo();
                    assignWorkerVo.setProcessTaskId(workerPolicyVo.getProcessTaskId());
                    assignWorkerVo.setProcessTaskStepId(workerPolicyVo.getProcessTaskStepId());
                    assignWorkerVo.setFromProcessTaskStepId(currentProcessTaskStepVo.getId());
                    assignWorkerVo.setFromProcessStepUuid(currentProcessTaskStepVo.getProcessStepUuid());
                    processTaskMapper.deleteProcessTaskAssignWorker(assignWorkerVo);
                    for (String worker : workerList) {
                        assignWorkerVo.setType(GroupSearch.getPrefix(worker));
                        assignWorkerVo.setUuid(GroupSearch.removePrefix(worker));
                        processTaskMapper.insertProcessTaskAssignWorker(assignWorkerVo);
                    }
                }
            }
        }
        return true;
    }

    /**
     * 找出流转到哪些步骤时，需要指定targetStepId步骤的处理人
     *
     * @param processTaskId 工单id
     * @param currentStepId 当前流转步骤id
     * @param targetStepId  配置了由当前步骤处理人指定处理人的步骤id
     */
    @Override
    public List<Long> getNextStepIdList(Long processTaskId, Long currentStepId, Long targetStepId) {
        Map<Long, List<Long>> fromId2ToIdListMap = new HashMap<>();
        List<ProcessTaskStepRelVo> processTaskStepRelList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
        for (ProcessTaskStepRelVo relVo : processTaskStepRelList) {
            if (Objects.equals(relVo.getType(), ProcessFlowDirection.FORWARD.getValue())) {
                Long fromId = relVo.getFromProcessTaskStepId();
                fromId2ToIdListMap.computeIfAbsent(fromId, key -> new ArrayList<>()).add(relVo.getToProcessTaskStepId());
            }
        }
        List<Long> resultList = new ArrayList<>();
        List<Long> currentStepNextStepIdList = fromId2ToIdListMap.get(currentStepId);
        if (CollectionUtils.isEmpty(currentStepNextStepIdList)) {
            return resultList;
        }
        for (Long nextStepId : currentStepNextStepIdList) {
            boolean flag = false;
            List<Long> stepIdList = new ArrayList<>();
            stepIdList.add(nextStepId);
            while (CollectionUtils.isNotEmpty(stepIdList)) {
                if (stepIdList.contains(targetStepId)) {
                    flag = true;
                    break;
                }
                List<Long> newStepIdList = new ArrayList<>();
                for (Long stepId : stepIdList) {
                    List<Long> toIdList = fromId2ToIdListMap.get(stepId);
                    if (CollectionUtils.isNotEmpty(toIdList)) {
                        newStepIdList.addAll(toIdList);
                    }
                }
                if (flag) {
                    break;
                }
                stepIdList = newStepIdList;
            }
            if (flag) {
                resultList.add(nextStepId);
            }
        }
        return resultList;
    }

    @Override
    public List<Long> getNextStepIdList(Long processTaskId, Long currentStepId, List<String> conditionStepUuidList) {
        List<Long> resultList = new ArrayList<>();
        List<ProcessTaskStepRelVo> processTaskStepRelList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
        for (ProcessTaskStepRelVo relVo : processTaskStepRelList) {
            if (Objects.equals(relVo.getType(), ProcessFlowDirection.FORWARD.getValue())) {
                Long fromId = relVo.getFromProcessTaskStepId();
                if (Objects.equals(fromId, currentStepId)
                        && (CollectionUtils.isEmpty(conditionStepUuidList) || conditionStepUuidList.contains(relVo.getToProcessStepUuid()))) {
                    resultList.add(relVo.getToProcessTaskStepId());
                }
            }
        }
        return resultList;
    }

    /**
     * @Description: 保存步骤提醒
     * @Author: linbq
     * @Date: 2021/1/21 11:30
     */
    @Override
    public int saveStepRemind(ProcessTaskStepVo currentProcessTaskStepVo, Long targerProcessTaskStepId, String reason, IProcessTaskStepRemindType ation) {
        ProcessTaskStepRemindVo processTaskStepRemindVo = new ProcessTaskStepRemindVo();
        processTaskStepRemindVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        processTaskStepRemindVo.setProcessTaskStepId(targerProcessTaskStepId);
        processTaskStepRemindVo.setAction(ation.getValue());
        processTaskStepRemindVo.setFcu(UserContext.get().getUserUuid(true));
        String title = ation.getTitle();
        String processTaskStepName = currentProcessTaskStepVo.getName();
        if (StringUtils.isBlank(processTaskStepName)) {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
            if (processTaskStepVo != null) {
                processTaskStepName = processTaskStepVo.getName();
            }
        }
        title = title.replace("processTaskStepName", processTaskStepName);
        processTaskStepRemindVo.setTitle(title);
        if (StringUtils.isNotBlank(reason)) {
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(reason);
            processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
            processTaskStepRemindVo.setContentHash(contentVo.getHash());
        }
        return processTaskMapper.insertProcessTaskStepRemind(processTaskStepRemindVo);
    }

    /**
     * @Description: 保存描述内容和附件
     * @Author: linbq
     * @Date: 2021/1/27 11:41
     * @Returns:void
     */
    @Override
    public void saveContentAndFile(ProcessTaskStepVo currentProcessTaskStepVo, IOperationType action) {
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        String content = paramObj.getString("content");
        JSONArray fileIdArray = paramObj.getJSONArray("fileIdList");
        if (content == null) {
            content = "";
        } else if (StringUtils.isBlank(content)) {
            paramObj.remove("content");
        }
        ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo();
        processTaskStepContentVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        processTaskStepContentVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
        processTaskStepContentVo.setType(action.getValue());
        ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
        processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
        processTaskStepContentVo.setContentHash(contentVo.getHash());
        String source = paramObj.getString("source");
        if (StringUtils.isNotBlank(source)) {
            processTaskStepContentVo.setSource(source);
        }
        processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);

        /* 保存附件uuid **/
        if (CollectionUtils.isNotEmpty(fileIdArray)) {
            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
            processTaskStepFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
            processTaskStepFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
            processTaskStepFileVo.setContentId(processTaskStepContentVo.getId());
            List<Long> fileIdList = fileIdArray.toJavaList(Long.class);
            Set<Long> fileIdSet = new HashSet<>(fileIdList);
            for (Long fileId : fileIdSet) {
                processTaskStepFileVo.setFileId(fileId);
                processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
            }
        } else {
            paramObj.remove("fileIdList");
        }

        JSONArray contentTargetList = paramObj.getJSONArray("contentTargetList");
        if (CollectionUtils.isNotEmpty(contentTargetList)) {
            for (int i = 0; i < contentTargetList.size(); i++) {
                JSONObject contentTargetObj = contentTargetList.getJSONObject(i);
                String type = contentTargetObj.getString("type");
                String uuid = contentTargetObj.getString("uuid");
                processTaskMapper.insertProcessTaskStepContentTarget(processTaskStepContentVo.getId(), type, uuid);
            }
        }
    }

    @Override
    public void saveProcessTaskOperationContent(ProcessTaskVo currentProcessTaskVo, IOperationType action) {
        JSONObject paramObj = currentProcessTaskVo.getParamObj();
        String content = paramObj.getString("content");
        if (content == null) {
            return;
        } else if (StringUtils.isBlank(content)) {
            paramObj.remove("content");
            return;
        }
        ProcessTaskOperationContentVo processTaskOperationContentVo = new ProcessTaskOperationContentVo();
        processTaskOperationContentVo.setProcessTaskId(currentProcessTaskVo.getId());
        processTaskOperationContentVo.setType(action.getValue());
        ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
        processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
        processTaskOperationContentVo.setContentHash(contentVo.getHash());
        String source = paramObj.getString("source");
        if (StringUtils.isNotBlank(source)) {
            processTaskOperationContentVo.setSource(source);
        }
        processTaskMapper.insertProcessTaskOperationContent(processTaskOperationContentVo);
    }

    @Override
    public void checkContentIsRequired(ProcessTaskStepVo currentProcessTaskStepVo, IOperationType operationType) {
        if (Objects.equals(currentProcessTaskStepVo.getIsNeedContent(), 0)) {
            return;
        }
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        String content = paramObj.getString("content");
        if (StringUtils.isBlank(content)) {
            if (operationType == ProcessTaskStepOperationType.STEP_BACK || Objects.equals(currentProcessTaskStepVo.getIsRequired(), 1)) {
                throw new ProcessTaskStepContentIsEmptyException();
            }
        }

//        if (Objects.equals(currentProcessTaskStepVo.getIsRequired(), 1)) {
//            JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
//            String content = paramObj.getString("content");
//            if (StringUtils.isBlank(content)) {
//                List<ProcessTaskStepContentVo> contentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(currentProcessTaskStepVo.getId());
//                if (CollectionUtils.isEmpty(contentList)) {
//                    throw new ProcessTaskStepContentIsEmptyException();
//                }
//                Date startTime = currentProcessTaskStepVo.getStartTime();
//                if (startTime != null) {
//                    for (ProcessTaskStepContentVo contentVo : contentList) {
//                        if (startTime.before(contentVo.getLcd())) {
//                            return;
//                        }
//                    }
//                    throw new ProcessTaskStepContentIsEmptyException();
//                }
//            }
//        }
    }

    /**
     * @Description: 保存标签列表
     * @Author: linbq
     * @Date: 2021/1/27 11:42
     * @Returns:void
     */
    @Override
    public void saveTagList(ProcessTaskStepVo currentProcessTaskStepVo) {
        processTaskMapper.deleteProcessTaskTagByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        JSONArray tagArray = paramObj.getJSONArray("tagList");
        if (CollectionUtils.isNotEmpty(tagArray)) {
            List<String> tagNameList = JSON.parseArray(tagArray.toJSONString(), String.class);
            List<ProcessTagVo> existTagList = processTagMapper.getProcessTagByNameList(tagNameList);
            List<String> existTagNameList = existTagList.stream().map(ProcessTagVo::getName).collect(Collectors.toList());
            List<String> notExistTagList = ListUtils.removeAll(tagNameList, existTagNameList);
            for (String tagName : notExistTagList) {
                ProcessTagVo tagVo = new ProcessTagVo(tagName);
                processTagMapper.insertProcessTag(tagVo);
                existTagList.add(tagVo);
            }
            List<ProcessTaskTagVo> processTaskTagVoList = new ArrayList<>();
            for (ProcessTagVo processTagVo : existTagList) {
                processTaskTagVoList.add(new ProcessTaskTagVo(currentProcessTaskStepVo.getProcessTaskId(), processTagVo.getId()));
            }
            processTaskMapper.insertProcessTaskTag(processTaskTagVoList);
            paramObj.put(ProcessTaskAuditDetailType.TAGLIST.getParamName(), String.join(",", tagNameList));
        } else {
            paramObj.remove("tagList");
        }
    }

    /***
     * @Description: 保存工单关注人
     * @Author: laiwt
     * @Date: 2021/2/19 11:20
     * @Params: [currentProcessTaskStepVo]
     * @Returns: void
     **/
    @Override
    public void saveFocusUserList(ProcessTaskStepVo currentProcessTaskStepVo) {
        processTaskMapper.deleteProcessTaskFocusByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        JSONArray focusUserUuidList = paramObj.getJSONArray("focusUserUuidList");
        if (CollectionUtils.isNotEmpty(focusUserUuidList)) {
            for (int i = 0; i < focusUserUuidList.size(); i++) {
                String useUuid = focusUserUuidList.getString(i).split("#")[1];
                processTaskMapper.insertProcessTaskFocus(currentProcessTaskStepVo.getProcessTaskId(), useUuid);
            }
            paramObj.put(ProcessTaskAuditDetailType.FOCUSUSER.getParamName(), focusUserUuidList.toJSONString());
        } else {
            paramObj.remove("focusUserUuidList");
        }
    }

//    /**
//     * @param currentProcessTaskStepVo
//     * @Description: 保存表单属性值
//     * @Author: linbq
//     * @Date: 2021/1/27 11:42
//     * @Params:[currentProcessTaskStepVo]
//     * @Returns:void
//     */
//    @Override
//    public void saveForm(ProcessTaskStepVo currentProcessTaskStepVo) {
//        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
//        if (processTaskFormVo != null) {
//            JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
//            JSONArray formAttributeDataList = paramObj.getJSONArray("formAttributeDataList");
//            if (formAttributeDataList == null) {
//                /** 如果参数中没有formAttributeDataList字段，则不改变表单属性值，这样可以兼容自动节点自动完成场景 **/
//                return;
//            }
//            /** 隐藏的属性uuid列表 **/
//            List<String> hidecomponentList = new ArrayList<>();
//            JSONArray hidecomponentArray = paramObj.getJSONArray("hidecomponentList");
//            if (CollectionUtils.isNotEmpty(hidecomponentArray)) {
//                hidecomponentList = hidecomponentArray.toJavaList(String.class);
//            }
//            /** 只读的属性uuid列表 **/
//            List<String> readcomponentList = new ArrayList<>();
//            JSONArray readcomponentArray = paramObj.getJSONArray("readcomponentList");
//            if (CollectionUtils.isNotEmpty(readcomponentArray)) {
//                readcomponentList = readcomponentArray.toJavaList(String.class);
//            }
//
//            /** 校验表单属性是否合法 **/
//            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
//            FormVersionVo formVersionVo = new FormVersionVo();
//            formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
//            formVersionVo.setFormName(processTaskFormVo.getFormName());
//            formVersionVo.setFormConfig(JSONObject.parseObject(formContent));
//            if (StringUtils.isBlank(currentProcessTaskStepVo.getConfigHash())) {
//                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
//                currentProcessTaskStepVo.setConfigHash(stepVo.getConfigHash());
//            }
//            formVersionVo.setSceneUuid(currentProcessTaskStepVo.getFormSceneUuid());
//            IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
//            formCrossoverService.formAttributeValueValid(formVersionVo, formAttributeDataList);
//            List<FormAttributeVo> formAttributeVoList = formVersionVo.getFormAttributeList();
//
//            Map<String, Object> formAttributeDataMap = new HashMap<>();
//            for (int i = 0; i < formAttributeDataList.size(); i++) {
//                JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
//                formAttributeDataMap.put(formAttributeDataObj.getString("attributeUuid"), formAttributeDataObj.get("dataList"));
//            }
////            FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processTaskFormVo.getFormUuid());
//            Map<String, String> attributeLabelMap = new HashMap<>();
//            if (CollectionUtils.isNotEmpty(formAttributeVoList)) {
//                Map<String, FormAttributeVo> formAttributeMap = new HashMap<>();
//                for (FormAttributeVo formAttributeVo : formAttributeVoList) {
//                    formAttributeMap.put(formAttributeVo.getUuid(), formAttributeVo);
//                    attributeLabelMap.put(formAttributeVo.getUuid(), formAttributeVo.getLabel());
//                    if (formAttributeVo.isRequired()) {
//                        if (hidecomponentList.contains(formAttributeVo.getUuid())) {
//                            continue;
//                        }
//                        if (readcomponentList.contains(formAttributeVo.getUuid())) {
//                            continue;
//                        }
//                        Object data = formAttributeDataMap.get(formAttributeVo.getUuid());
//                        if (data != null) {
//                            if (data instanceof String) {
//                                if (StringUtils.isBlank(data.toString())) {
//                                    throw new FormAttributeRequiredException(formAttributeVo.getLabel());
//                                }
//                            } else if (data instanceof JSONArray) {
//                                if (CollectionUtils.isEmpty((JSONArray) data)) {
//                                    throw new FormAttributeRequiredException(formAttributeVo.getLabel());
//                                }
//                            } else if (data instanceof JSONObject) {
//                                if (MapUtils.isEmpty((JSONObject) data)) {
//                                    throw new FormAttributeRequiredException(formAttributeVo.getLabel());
//                                }
//                            }
//                        } else {
//                            throw new FormAttributeRequiredException(formAttributeVo.getLabel());
//                        }
//                    }
//                }
//                // 对表格输入组件中密码password类型的单元格数据进行加密
////                IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
////                for (int i = 0; i < formAttributeDataList.size(); i++) {
////                    JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
////                    String attributeUuid = formAttributeDataObj.getString("attributeUuid");
////                    FormAttributeVo formAttributeVo = formAttributeMap.get(attributeUuid);
////                    if (formAttributeVo != null) {
////                        if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMTABLEINPUTER.getHandler())) {
////                            JSONArray dataList = formAttributeDataObj.getJSONArray("dataList");
////                            formCrossoverService.staticListPasswordEncrypt(dataList, formAttributeVo.getConfigObj());
////                        } else if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMPASSWORD.getHandler())) {
////                            String dataList = formAttributeDataObj.getString("dataList");
////                            if (StringUtils.isNotBlank(dataList)) {
////                                dataList = RC4Util.encrypt(dataList);
////                                formAttributeDataObj.put("dataList", dataList);
////                                formAttributeDataMap.put(attributeUuid, dataList);
////                            }
////                        }
////                    }
////                }
//            }
//            /** 获取旧表单数据 **/
//            List<ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
//            Iterator<ProcessTaskFormAttributeDataVo> iterator = oldProcessTaskFormAttributeDataList.iterator();
//            while (iterator.hasNext()) {
//                ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo = iterator.next();
//                String attributeUuid = processTaskFormAttributeDataVo.getAttributeUuid();
//                Object dataList = formAttributeDataMap.get(attributeUuid);
//                if (dataList != null && Objects.equals(dataList, processTaskFormAttributeDataVo.getDataObj())) {
//                    /** 如果新表单属性值与旧表单属性值相同，就不用replace更新数据了 **/
//                    formAttributeDataMap.remove(attributeUuid);
//                    iterator.remove();
//                }
//                /*else if (hidecomponentList.contains(attributeUuid)) {
//                    iterator.remove();
//                }*/
//            }
//            if (CollectionUtils.isNotEmpty(oldProcessTaskFormAttributeDataList)) {
//                oldProcessTaskFormAttributeDataList.sort(ProcessTaskFormAttributeDataVo::compareTo);
//                paramObj.put(ProcessTaskAuditDetailType.FORM.getOldDataParamName(), JSON.toJSONString(oldProcessTaskFormAttributeDataList));
//            }
//
//            /** 写入当前步骤的表单属性值 **/
//            if (CollectionUtils.isNotEmpty(formAttributeDataList) && MapUtils.isNotEmpty(formAttributeDataMap)) {
//                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = new ArrayList<>(formAttributeDataList.size());
//                for (int i = 0; i < formAttributeDataList.size(); i++) {
//                    JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
//                    String attributeUuid = formAttributeDataObj.getString("attributeUuid");
//                    if (formAttributeDataMap.containsKey(attributeUuid)) {
////                        // 对于隐藏的属性，当前用户不能修改，不更新数据库中的值，不进行修改前后对比
////                        if (hidecomponentList.contains(attributeUuid)) {
////                            continue;
////                        }
//                        ProcessTaskFormAttributeDataVo attributeData = new ProcessTaskFormAttributeDataVo();
//                        attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
//                        attributeData.setData(formAttributeDataObj.getString("dataList"));
//                        attributeData.setAttributeUuid(attributeUuid);
//                        String attributeLabel = attributeLabelMap.get(attributeUuid);
//                        attributeData.setAttributeLabel(attributeLabel);
//                        attributeData.setType(formAttributeDataObj.getString("handler"));
//                        attributeData.setSort(i);
//                        processTaskFormAttributeDataList.add(attributeData);
//                        processTaskMapper.insertProcessTaskFormAttributeData(attributeData);
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
//                    processTaskFormAttributeDataList.sort(ProcessTaskFormAttributeDataVo::compareTo);
//                    paramObj.put(ProcessTaskAuditDetailType.FORM.getParamName(), JSON.toJSONString(processTaskFormAttributeDataList));
//                }
//            }
//        }
//    }

    /**
     * @Description: 保存表单属性值
     * @Author: linbq
     * @Date: 2021/1/27 11:42
     * @Returns:void
     */
    @Override
    public void saveForm(ProcessTaskStepVo currentProcessTaskStepVo) {
        Long processTaskId = currentProcessTaskStepVo.getProcessTaskId();
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContent())) {
            // 工单没有表单直接返回
            return;
        }
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        JSONArray formAttributeDataList = paramObj.getJSONArray("formAttributeDataList");
        if (formAttributeDataList == null) {
            /* 如果参数中没有formAttributeDataList字段，则不改变表单属性值，这样可以兼容自动节点自动完成场景 **/
            return;
        }

        /* 隐藏的属性uuid列表 **/
        List<String> hidecomponentList = new ArrayList<>();
        JSONArray hidecomponentArray = paramObj.getJSONArray("hidecomponentList");
        if (CollectionUtils.isNotEmpty(hidecomponentArray)) {
            hidecomponentList = hidecomponentArray.toJavaList(String.class);
        }
        /* 只读的属性uuid列表 **/
        List<String> readcomponentList = new ArrayList<>();
        JSONArray readcomponentArray = paramObj.getJSONArray("readcomponentList");
        if (CollectionUtils.isNotEmpty(readcomponentArray)) {
            readcomponentList = readcomponentArray.toJavaList(String.class);
        }

        Boolean needVerifyIsRequired = paramObj.getBoolean("needVerifyIsRequired");
        if (needVerifyIsRequired == null) {
            needVerifyIsRequired = true;
        }
        Map<String, FormAttributeVo> formExtendAttributeMap = new HashMap<>();
        Map<String, FormAttributeVo> formCustomExtendAttributeMap = new HashMap<>();
        List<FormAttributeVo> mainSceneFormAttributeList;
//        String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
        JSONObject formConfig = JSON.parseObject(processTaskFormVo.getFormContent());
        String mainSceneUuid = formConfig.getString("uuid");
        {
            // 主场景的表单
            FormVersionVo formVersionVo = new FormVersionVo();
            formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
            formVersionVo.setFormName(processTaskFormVo.getFormName());
            formVersionVo.setFormConfig(formConfig);
            formVersionVo.setSceneUuid(mainSceneUuid);
            /* 校验表单属性是否合法 **/
            FormUtil.formAttributeValueValid(formVersionVo, formAttributeDataList);
            mainSceneFormAttributeList = formVersionVo.getFormAttributeList();
            List<FormAttributeVo> formExtendAttributeList = formVersionVo.getFormExtendAttributeList();
            if (CollectionUtils.isNotEmpty(formExtendAttributeList)) {
                formExtendAttributeMap = formExtendAttributeList.stream().collect(Collectors.toMap(e -> e.getParentUuid() + "#" + e.getTag() + "#" + e.getKey(), e -> e));
            }
            List<FormAttributeVo> formCustomExtendAttributeList = formVersionVo.getFormCustomExtendAttributeList();
            if (CollectionUtils.isNotEmpty(formCustomExtendAttributeList)) {
                formCustomExtendAttributeMap = formCustomExtendAttributeList.stream().collect(Collectors.toMap(e -> mainSceneUuid + "#" + e.getTag() + "#" + e.getKey(), e -> e));
            }
        }
        if (CollectionUtils.isEmpty(mainSceneFormAttributeList)) {
            return;
        }
        Map<String, Object> formAttributeDataMap = new HashMap<>();
        for (int i = 0; i < formAttributeDataList.size(); i++) {
            JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
            String attributeUuid = formAttributeDataObj.getString("attributeUuid");
            if (StringUtils.isBlank(attributeUuid)) {
                String attributeKey = formAttributeDataObj.getString("key");
                if (StringUtils.isNotBlank(attributeKey)) {
                    Optional<FormAttributeVo> first = mainSceneFormAttributeList.stream().filter(e -> Objects.equals(e.getKey(), attributeKey)).findFirst();
                    if (first.isPresent()) {
                        attributeUuid = first.get().getUuid();
                    }
                }
            }
            Object data = formAttributeDataObj.get("dataList");
            formAttributeDataMap.put(attributeUuid, data);
        }

        // 工单步骤指定场景的表单
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
        formVersionVo.setFormName(processTaskFormVo.getFormName());
        formVersionVo.setFormConfig(JSON.parseObject(processTaskFormVo.getFormContent()));
        Long processTaskStepId = currentProcessTaskStepVo.getId();
        if (currentProcessTaskStepVo.getId() != null) {
            if (StringUtils.isBlank(currentProcessTaskStepVo.getConfigHash())) {
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
                currentProcessTaskStepVo.setConfigHash(stepVo.getConfigHash());
            }
            formVersionVo.setSceneUuid(currentProcessTaskStepVo.getFormSceneUuid());
        } else {
            formVersionVo.setSceneUuid(mainSceneUuid);
        }

        List<FormAttributeVo> formAttributeVoList = formVersionVo.getFormAttributeList();

//        Map<String, String> attributeLabelMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(formAttributeVoList)) {
            for (FormAttributeVo formAttributeVo : formAttributeVoList) {
//                attributeLabelMap.put(formAttributeVo.getUuid(), formAttributeVo.getLabel());
                if (formAttributeVo.isRequired() && needVerifyIsRequired) {
                    if (hidecomponentList.contains(formAttributeVo.getUuid())) {
                        continue;
                    }
                    if (readcomponentList.contains(formAttributeVo.getUuid())) {
                        continue;
                    }
                    Object data = formAttributeDataMap.get(formAttributeVo.getUuid());
                    if (data != null) {
                        if (data instanceof String) {
                            if (StringUtils.isBlank(data.toString())) {
                                throw new FormAttributeRequiredException(formAttributeVo.getLabel());
                            }
                        } else if (data instanceof JSONArray) {
                            if (CollectionUtils.isEmpty((JSONArray) data)) {
                                throw new FormAttributeRequiredException(formAttributeVo.getLabel());
                            }
                        } else if (data instanceof JSONObject) {
                            if (MapUtils.isEmpty((JSONObject) data)) {
                                throw new FormAttributeRequiredException(formAttributeVo.getLabel());
                            }
                        }
                    } else {
                        throw new FormAttributeRequiredException(formAttributeVo.getLabel());
                    }
                }
            }
        }

        /* 获取旧表单数据 **/
        List<ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataList = new ArrayList<>();
        List<Long> formAttributeDataIdList = processTaskMapper.getProcessTaskFormAttributeDataIdListByProcessTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(formAttributeDataIdList)) {
            List<AttributeDataVo> attributeDataList = formMapper.getFormAttributeDataListByIdList(formAttributeDataIdList);
            if (CollectionUtils.isNotEmpty(attributeDataList)) {
                for (AttributeDataVo attributeDataVo : attributeDataList) {
                    oldProcessTaskFormAttributeDataList.add(new ProcessTaskFormAttributeDataVo(processTaskId, attributeDataVo));
                }
            }
        }

        Map<String, ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataMap = oldProcessTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));

        List<ProcessTaskFormAttributeDataVo> newProcessTaskFormAttributeDataList = new ArrayList<>();
        int i = 0;
        for (FormAttributeVo formAttributeVo : mainSceneFormAttributeList) {
            String attributeUuid = formAttributeVo.getUuid();
            if (formAttributeDataMap.containsKey(attributeUuid)) {
                ProcessTaskFormAttributeDataVo oldProcessTaskFormAttributeData = oldProcessTaskFormAttributeDataMap.get(attributeUuid);
                Object data = formAttributeDataMap.get(attributeUuid);
                IFormAttributeDataConversionHandler formAttributeDataConversionHandler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (formAttributeDataConversionHandler != null) {
//                    Object oldData = null;
//                    if (oldProcessTaskFormAttributeData != null) {
//                        oldData = oldProcessTaskFormAttributeData.getDataObj();
//                    }
                    data = formAttributeDataConversionHandler.passwordEncryption(data, formAttributeVo.getConfig());//, oldData
                }

                ProcessTaskFormAttributeDataVo formAttributeDataVo = new ProcessTaskFormAttributeDataVo();
                if (oldProcessTaskFormAttributeData != null) {
                    formAttributeDataVo.setId(oldProcessTaskFormAttributeData.getId());
                }
                formAttributeDataVo.setProcessTaskId(processTaskId);
                formAttributeDataVo.setAttributeUuid(attributeUuid);
                formAttributeDataVo.setAttributeKey(formAttributeVo.getKey());
                formAttributeDataVo.setAttributeLabel(formAttributeVo.getLabel());
                formAttributeDataVo.setHandler(formAttributeVo.getHandler());
                formAttributeDataVo.setDataObj(data);
                formAttributeDataVo.setFormUuid(formAttributeVo.getFormUuid());
//                formAttributeDataVo.setSort(i);
                newProcessTaskFormAttributeDataList.add(formAttributeDataVo);
            }
            i++;
        }
        // 判断是否修改了表单数据，如果是，则记录活动
        if (FormUtil.isModifiedFormData(mainSceneFormAttributeList, newProcessTaskFormAttributeDataList, oldProcessTaskFormAttributeDataList)) {
            paramObj.put(ProcessTaskAuditDetailType.FORM.getOldDataParamName(), JSON.toJSONString(oldProcessTaskFormAttributeDataList));
            paramObj.put(ProcessTaskAuditDetailType.FORM.getParamName(), JSON.toJSONString(newProcessTaskFormAttributeDataList));
        }

        // 写入当前工单的表单属性值
        List<ProcessTaskFormAttributeDataVo> needSaveProcessTaskFormAttributeDataList = new ArrayList<>();
        for (ProcessTaskFormAttributeDataVo dataVo : newProcessTaskFormAttributeDataList) {
            String attributeUuid = dataVo.getAttributeUuid();
            ProcessTaskFormAttributeDataVo oldProcessTaskFormAttributeDataVo = oldProcessTaskFormAttributeDataMap.get(attributeUuid);
            if (oldProcessTaskFormAttributeDataVo != null) {
                if (Objects.equals(oldProcessTaskFormAttributeDataVo.getDataObj(), dataVo.getDataObj())) {
                    continue;
                }
            }
            /*Object dataObj = dataVo.getDataObj();
            if (dataObj != null) {
                if (dataObj instanceof JSONObject || dataObj instanceof JSONArray) {
                    dataVo.setData(JSON.toJSONString(dataObj));
                } else if (dataObj instanceof String) {
                    dataVo.setData((String) dataObj);
                } else {
                    dataVo.setData(dataObj.toString());
                }
            }*/
//            formMapper.insertFormAttributeData(dataVo);
//            processTaskMapper.insertProcessTaskFormAttribute(dataVo);
            needSaveProcessTaskFormAttributeDataList.add(dataVo);
        }
        if (CollectionUtils.isNotEmpty(needSaveProcessTaskFormAttributeDataList)) {
            formMapper.insertFormAttributeDataList(needSaveProcessTaskFormAttributeDataList);
            processTaskMapper.insertProcessTaskFormAttributeList(needSaveProcessTaskFormAttributeDataList);
        }
        // 保存表单扩展组件值
        JSONArray formExtendAttributeDataList = paramObj.getJSONArray("formExtendAttributeDataList");
        if (CollectionUtils.isNotEmpty(formExtendAttributeDataList)) {
            Map<String, List<String>> tag2KeyListMap = new HashMap<>();
            for (int j = 0; j < formExtendAttributeDataList.size(); j++) {
                JSONObject formExtendAttributeDataObj = formExtendAttributeDataList.getJSONObject(j);
                if (MapUtils.isEmpty(formExtendAttributeDataObj)) {
                    continue;
                }
                String tag = formExtendAttributeDataObj.getString("tag");
                String key = formExtendAttributeDataObj.getString("key");
                List<String> keyList = tag2KeyListMap.computeIfAbsent(tag, k -> new ArrayList<>());
                if (keyList.contains(key)) {
                    throw new FormAttributeDataException("formExtendAttributeDataList", tag, key);
                }
                keyList.add(key);
            }
            List<AttributeDataVo> oldExtendAttributeDataList = processTaskMapper.getProcessTaskExtendFormAttributeDataListByProcessTaskId(processTaskId, null);
            Map<String, AttributeDataVo> oldExtendAttributeDataMap = oldExtendAttributeDataList.stream().collect(Collectors.toMap(AttributeDataVo::getAttributeUuid, e -> e));
            List<ProcessTaskFormAttributeDataVo> needSaveProcessTaskFormExtendAttributeDataList = new ArrayList<>();
            for (int j = 0; j < formExtendAttributeDataList.size(); j++) {
                JSONObject formExtendAttributeDataObj = formExtendAttributeDataList.getJSONObject(j);
                if (MapUtils.isEmpty(formExtendAttributeDataObj)) {
                    continue;
                }
                String parentUuid = formExtendAttributeDataObj.getString("parentUuid");
                String tag = formExtendAttributeDataObj.getString("tag");
                String key = formExtendAttributeDataObj.getString("key");
                FormAttributeVo formAttributeVo = null;
                if (StringUtils.isNotBlank(parentUuid)) {
                    formAttributeVo = formExtendAttributeMap.get(parentUuid + "#" + tag + "#" + key);
                } else {
                    formAttributeVo = formCustomExtendAttributeMap.get(mainSceneUuid + "#" + tag + "#" + key);
                }
                if (formAttributeVo == null) {
                    continue;
                }
                AttributeDataVo oldAttributeDataVo = oldExtendAttributeDataMap.get(formAttributeVo.getUuid());
                Object data = formExtendAttributeDataObj.get("dataList");
                IFormAttributeDataConversionHandler formAttributeDataConversionHandler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (formAttributeDataConversionHandler != null) {
//                    Object oldData = null;
//                    if (oldAttributeDataVo != null) {
//                        oldData = oldAttributeDataVo.getDataObj();
//                    }
                    data = formAttributeDataConversionHandler.passwordEncryption(data, formAttributeVo.getConfig());// , oldData
                }

                ProcessTaskFormAttributeDataVo processTaskExtendFormAttributeDataVo = new ProcessTaskFormAttributeDataVo();
                if (oldAttributeDataVo != null) {
                    processTaskExtendFormAttributeDataVo.setId(oldAttributeDataVo.getId());
                }
                processTaskExtendFormAttributeDataVo.setFormUuid(formAttributeVo.getFormUuid());
                processTaskExtendFormAttributeDataVo.setHandler(formAttributeVo.getHandler());
                processTaskExtendFormAttributeDataVo.setTag(tag);
                processTaskExtendFormAttributeDataVo.setAttributeUuid(formAttributeVo.getUuid());
                processTaskExtendFormAttributeDataVo.setAttributeKey(formAttributeVo.getKey());
                processTaskExtendFormAttributeDataVo.setAttributeLabel(formAttributeVo.getLabel());
                processTaskExtendFormAttributeDataVo.setDataObj(data);
//                formMapper.insertFormExtendAttributeData(processTaskExtendFormAttributeDataVo);
                processTaskExtendFormAttributeDataVo.setProcessTaskId(processTaskId);
//                processTaskMapper.insertProcessTaskExtendFormAttribute(processTaskExtendFormAttributeDataVo);
                needSaveProcessTaskFormExtendAttributeDataList.add(processTaskExtendFormAttributeDataVo);
            }
            if (CollectionUtils.isNotEmpty(needSaveProcessTaskFormExtendAttributeDataList)) {
                formMapper.insertFormExtendAttributeDataList(needSaveProcessTaskFormExtendAttributeDataList);
                processTaskMapper.insertProcessTaskExtendFormAttributeList(needSaveProcessTaskFormExtendAttributeDataList);
            }
        }
    }

    @Override
    public ProcessTaskStepAssignVo analysisAssignConfig(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        ProcessTaskStepAssignVo processTaskStepAssignVo = new ProcessTaskStepAssignVo();
        List<ProcessTaskStepWorkerVo> stepWorkerList = new ArrayList<>();
        /* 获取步骤配置信息 **/
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        Integer autoStart = (Integer) JSONPath.read(stepConfig, "autoStart");
        autoStart = autoStart != null ? autoStart : 1;
        processTaskStepAssignVo.setAutoStart(autoStart);
        /* 如果已经存在过处理人，分配策略设置只分配一次，则继续使用旧处理人，否则启用分派 **/
        List<ProcessTaskStepUserVo> oldUserList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
        if (CollectionUtils.isNotEmpty(oldUserList)) {
            ProcessTaskStepUserVo oldUserVo = oldUserList.get(0);
            processTaskStepAssignVo.setOldStepUser(oldUserVo);
            int isOnlyOnceExecute = Integer.parseInt(ConfigManager.getConfig(ItsmTenantConfig.PROCESSTASK_WORKERPOLICY_ISONLYONCEEXECUTE));
            processTaskStepAssignVo.setIsOnlyOnceExecute(isOnlyOnceExecute);
            if (Objects.equals(isOnlyOnceExecute, 1)) {
                ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo(
                        currentProcessTaskStepVo.getProcessTaskId(),
                        currentProcessTaskStepVo.getId(),
                        GroupSearch.USER.getValue(),
                        oldUserVo.getUserUuid(),
                        ProcessUserType.MAJOR.getValue()
                );
                stepWorkerList.add(processTaskStepWorkerVo);
            }
        }
        try {
            // 如果没有或不用旧处理人，启用分派
            if (CollectionUtils.isEmpty(stepWorkerList)) {
                /* 分配处理人 **/
                ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
                processTaskStepWorkerPolicyVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
                if (CollectionUtils.isNotEmpty(workerPolicyList)) {
                    String executeMode = (String) JSONPath.read(stepConfig, "workerPolicyConfig.executeMode");
                    for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
                        IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
                        if (workerPolicyHandler != null) {
                            List<ProcessTaskStepWorkerVo> tmpWorkerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
                            if (CollectionUtils.isNotEmpty(tmpWorkerList)) {
                                /* 删除不存在的用户、组、角色 **/
                                Iterator<ProcessTaskStepWorkerVo> iterator = tmpWorkerList.iterator();
                                while (iterator.hasNext()) {
                                    ProcessTaskStepWorkerVo workerVo = iterator.next();
                                    if (Objects.equals(workerVo.getType(), GroupSearch.USER.getValue())) {
                                        UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                                        if (userVo == null || userVo.getIsActive() == 0) {
                                            iterator.remove();
                                        }
                                    } else if (Objects.equals(workerVo.getType(), GroupSearch.TEAM.getValue())) {
                                        if (teamMapper.checkTeamIsExists(workerVo.getUuid()) == 0) {
                                            iterator.remove();
                                        }
                                    } else if (Objects.equals(workerVo.getType(), GroupSearch.ROLE.getValue())) {
                                        if (roleMapper.checkRoleIsExists(workerVo.getUuid()) == 0) {
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                            if (CollectionUtils.isNotEmpty(tmpWorkerList)) {
                                /* 顺序分配处理人 **/
                                if ("sort".equals(executeMode)) {
                                    // 找到处理人，则退出
                                    stepWorkerList.addAll(tmpWorkerList);
                                    break;
                                } else if ("batch".equals(executeMode)) {
                                    stepWorkerList.addAll(tmpWorkerList);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            processTaskStepAssignVo.setMessage(e.getMessage());
        }

        // 如果分派器没有得到处理人，找到默认处理人
        if (CollectionUtils.isEmpty(stepWorkerList)) {
            processTaskStepAssignVo.setIsAssignException(true);
            String defaultWorker = (String) JSONPath.read(stepConfig, "workerPolicyConfig.defaultWorker");
            String[] split = defaultWorker.split("#");
            ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo(
                    currentProcessTaskStepVo.getProcessTaskId(),
                    currentProcessTaskStepVo.getId(),
                    split[0],
                    split[1],
                    ProcessUserType.MAJOR.getValue()
            );
            stepWorkerList.add(processTaskStepWorkerVo);
            String defaultWorkerName = StringUtils.EMPTY;
            if (Objects.equals(split[0], GroupSearch.USER.getValue())) {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(split[1]);
                if (userVo != null) {
                    defaultWorkerName = userVo.getUserName();
                }
            } else if (Objects.equals(split[0], GroupSearch.TEAM.getValue())) {
                TeamVo teamVo = teamMapper.getTeamByUuid(split[1]);
                if (teamVo != null) {
                    defaultWorkerName = teamVo.getName();
                }
            } else if (Objects.equals(split[0], GroupSearch.ROLE.getValue())) {
                RoleVo roleVo = roleMapper.getRoleByUuid(split[1]);
                if (roleVo != null) {
                    defaultWorkerName = roleVo.getName();
                }
            }
            processTaskStepAssignVo.setDefaultWorkerName(defaultWorkerName);
        } else {
            processTaskStepAssignVo.setIsAssignException(false);
        }
        List<ProcessTaskStepWorkerVo> finalStepWorkerList = new ArrayList<>();
        /* 当只分配到一个用户时，自动设置为处理人，不需要抢单 **/
        if (Objects.equals(autoStart, 1)) {
            if (CollectionUtils.isNotEmpty(stepWorkerList)) {
                Set<String> userUuidSet = new HashSet<>();
                for (ProcessTaskStepWorkerVo workerVo : stepWorkerList) {
                    if (GroupSearch.TEAM.getValue().equals(workerVo.getType())) {
                        List<String> userUuidList = userMapper.getUserUuidListByTeamUuid(workerVo.getUuid());
                        userUuidSet.addAll(userUuidList);
                    } else if (GroupSearch.ROLE.getValue().equals(workerVo.getType())) {
                        List<String> userUuidList = userMapper.getUserUuidListByRoleUuid(workerVo.getUuid());
                        userUuidSet.addAll(userUuidList);
                        List<RoleTeamVo> roleTeamList = roleMapper.getRoleTeamListByRoleUuid(workerVo.getUuid());
                        if (CollectionUtils.isNotEmpty(roleTeamList)) {
                            List<String> checkedChildrenteamUuidList = new ArrayList<>();
                            List<String> teamUuidList = new ArrayList<>();
                            for (RoleTeamVo roleTeamVo : roleTeamList) {
                                if (Objects.equals(roleTeamVo.getCheckedChildren(), 1)) {
                                    checkedChildrenteamUuidList.add(roleTeamVo.getTeamUuid());
                                } else {
                                    teamUuidList.add(roleTeamVo.getTeamUuid());
                                }
                            }
                            if (CollectionUtils.isNotEmpty(checkedChildrenteamUuidList)) {
                                List<TeamVo> teamList = teamMapper.getTeamByUuidList(checkedChildrenteamUuidList);
                                teamList.sort(Comparator.comparing(TeamVo::getLft));
                                for (TeamVo teamVo : teamList) {
                                    if (!teamUuidList.contains(teamVo.getUuid())) {
                                        teamUuidList.add(teamVo.getUuid());
                                        List<String> childrenUuidList = teamMapper.getChildrenUuidListByLeftRightCode(teamVo.getLft(), teamVo.getRht());
                                        teamUuidList.addAll(childrenUuidList);
                                    }
                                }
                            }
                            userUuidSet.addAll(userMapper.getUserUuidListByTeamUuidListLimitTwo(teamUuidList));
                        }
                    } else if (GroupSearch.USER.getValue().equals(workerVo.getType())) {
                        userUuidSet.add(workerVo.getUuid());
                    }
                }
                if (CollectionUtils.isNotEmpty(userUuidSet)) {
                    List<String> allUserUuidList = new ArrayList<>();
                    Iterator<String> iterator = userUuidSet.iterator();
                    while (iterator.hasNext()) {
                        String userUuid = iterator.next();
                        UserVo userVo = SystemUserFactory.getUserVoByUser(userUuid);
                        if (userVo != null) {
                            allUserUuidList.add(userUuid);
                            iterator.remove();
                        }
                    }
                    List<String> userUuidList = userMapper.getUserUuidListByUuidListAndIsActive(new ArrayList<>(userUuidSet), 1);
                    allUserUuidList.addAll(userUuidList);
                    if (allUserUuidList.size() == 1) {
                        processTaskStepAssignVo.setStepStatus(ProcessTaskStepStatus.RUNNING.getValue());
                        ProcessTaskStepUserVo processTaskStepUser = new ProcessTaskStepUserVo(
                                currentProcessTaskStepVo.getProcessTaskId(),
                                currentProcessTaskStepVo.getId(),
                                allUserUuidList.get(0),
                                ProcessUserType.MAJOR.getValue()
                        );
                        processTaskStepAssignVo.setStepUser(processTaskStepUser);
                        ProcessTaskStepWorkerVo worker = new ProcessTaskStepWorkerVo(
                                currentProcessTaskStepVo.getProcessTaskId(),
                                currentProcessTaskStepVo.getId(),
                                GroupSearch.USER.getValue(),
                                allUserUuidList.get(0),
                                ProcessUserType.MAJOR.getValue()
                        );
                        finalStepWorkerList.add(worker);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(finalStepWorkerList)) {
            Set<String> uuidSet = new HashSet<>();
            for (ProcessTaskStepWorkerVo processTaskStepWorkerVo : stepWorkerList) {
                if (uuidSet.contains(processTaskStepWorkerVo.getUuid())) {
                    continue;
                }
                finalStepWorkerList.add(processTaskStepWorkerVo);
                uuidSet.add(processTaskStepWorkerVo.getUuid());
            }
        }
        processTaskStepAssignVo.setStepWorkerList(stepWorkerList);
        processTaskStepAssignVo.setFinalStepWorkerList(finalStepWorkerList);
        return processTaskStepAssignVo;
    }
}
