package neatlogic.module.process.stephandler.utilhandler;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.TransactionSynchronizationPool;
import neatlogic.framework.common.RootComponent;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormAttributeRequiredException;
import neatlogic.framework.form.service.IFormCrossoverService;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.audithandler.core.IProcessTaskAuditType;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dao.mapper.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.processtask.*;
import neatlogic.framework.process.stephandler.core.IProcessStepHandlerUtil;
import neatlogic.framework.process.stepremind.core.IProcessTaskStepRemindType;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import neatlogic.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import neatlogic.module.process.thread.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: ProcessStepHandlerUtilServiceImpl
 * @Package neatlogic.module.process.stephandler.service
 * @Description: TODO
 * @Author: linbq
 * @Date: 2021/1/20 16:26
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **/
@RootComponent
public class ProcessStepHandlerUtil implements IProcessStepHandlerUtil {
    @Resource
    private ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;
    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ProcessTagMapper processTagMapper;

    /**
     * @param currentProcessTaskStepVo
     * @param trigger
     * @Description: 触发动作
     * @Author: linbq
     * @Date: 2021/1/20 16:15
     * @Params:[currentProcessTaskStepVo, trigger]
     * @Returns:void
     */
    @Override
    public void action(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger) {
        TransactionSynchronizationPool.execute(new ProcessTaskActionThread(currentProcessTaskStepVo, trigger));
    }

    /**
     * @param currentProcessTaskStepVo
     * @param trigger
     * @Description: 触发通知
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskStepVo, trigger]
     * @Returns:void
     */
    @Override
    public void notify(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger) {
        TransactionSynchronizationPool.execute(new ProcessTaskNotifyThread(currentProcessTaskStepVo, trigger));
    }

    /**
     * @param currentProcessTaskVo
     * @param isAsync
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo, boolean isAsync) {
        calculateSla(currentProcessTaskVo, null, isAsync);
    }

    /**
     * @param currentProcessTaskVo
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo) {
        calculateSla(currentProcessTaskVo, null, true);
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskStepVo currentProcessTaskStepVo) {
        calculateSla(null, currentProcessTaskStepVo, true);
    }

    /**
     * @param currentProcessTaskVo
     * @param currentProcessTaskStepVo
     * @param isAsync
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo, ProcessTaskStepVo currentProcessTaskStepVo, boolean isAsync) {
        if (!isAsync) {
            new ProcessTaskSlaThread(currentProcessTaskVo, currentProcessTaskStepVo).execute();
        } else {
            TransactionSynchronizationPool.execute(new ProcessTaskSlaThread(currentProcessTaskVo, currentProcessTaskStepVo));
        }
    }

    /**
     * @param currentProcessTaskStepVo
     * @param action
     * @Description: 记录操作时间
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     */
    @Override
    public void timeAudit(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskOperationType action) {
        ProcessTaskStepTimeAuditVo lastTimeAuditVo = processTaskStepTimeAuditMapper
                .getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
        ProcessTaskStepTimeAuditVo newAuditVo = new ProcessTaskStepTimeAuditVo();
        newAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
        switch (action) {
            case STEP_ACTIVE:
                newAuditVo.setActiveTime("now");
                processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                break;
            case STEP_START:
                newAuditVo.setStartTime("now");
                if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getStartTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(lastTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
                    newAuditVo.setId(lastTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case STEP_COMPLETE:
                /** 如果找不到审计记录并且completetime不为空，则新建审计记录 **/
                newAuditVo.setCompleteTime("now");
                if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getCompleteTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(lastTimeAuditVo.getCompleteTime())) {// 如果completetime为空，则更新completetime
                    newAuditVo.setId(lastTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case PROCESSTASK_ABORT:
                /** 如果找不到审计记录并且aborttime不为空，则新建审计记录 **/
                newAuditVo.setAbortTime("now");
                if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getAbortTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(lastTimeAuditVo.getAbortTime())) {// 如果aborttime为空，则更新aborttime
                    newAuditVo.setId(lastTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case STEP_BACK:
                /** 如果找不到审计记录并且backtime不为空，则新建审计记录 **/
                newAuditVo.setBackTime("now");
                if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getBackTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(lastTimeAuditVo.getBackTime())) {// 如果backtime为空，则更新backtime
                    newAuditVo.setId(lastTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case PROCESSTASK_RECOVER:
                if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())) {
                    newAuditVo.setActiveTime("now");
                    if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getActiveTime())) {
                        processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                    }
                } else if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())) {
                    newAuditVo.setStartTime("now");
                    if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getStartTime())) {
                        processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                    } else if (StringUtils.isBlank(lastTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
                        newAuditVo.setId(lastTimeAuditVo.getId());
                        processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                    }
                }
                break;
            case STEP_PAUSE:
                /** 如果找不到审计记录并且pausetime不为空，则新建审计记录 **/
                newAuditVo.setPauseTime("now");
                if (lastTimeAuditVo == null || StringUtils.isNotBlank(lastTimeAuditVo.getPauseTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(lastTimeAuditVo.getPauseTime())) {// 如果pausetime为空，则更新pausetime
                    newAuditVo.setId(lastTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
        }
    }

    /**
     * @param currentProcessTaskStepVo
     * @param action
     * @Description: 记录操作活动
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     */
    @Override
    public void audit(ProcessTaskStepVo currentProcessTaskStepVo, IProcessTaskAuditType action) {
        TransactionSynchronizationPool.execute(new ProcessTaskAuditThread(currentProcessTaskStepVo, action));
    }

    /**
     * @param currentProcessTaskVo
     * @Description: 自动评分
     * @Author: linbq
     * @Date: 2021/1/20 16:22
     * @Params:[currentProcessTaskVo]
     * @Returns:void
     */
    @Override
    public void autoScore(ProcessTaskVo currentProcessTaskVo) {
        TransactionSynchronizationPool.execute(new ProcessTaskAutoScoreThread(currentProcessTaskVo));
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 获取验证基本信息数据是否合法，并验证
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
     */
    @Override
    public boolean baseInfoValidFromDb(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskVo processTaskVo =
                processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
        baseInfoValid(currentProcessTaskStepVo, processTaskVo);
        return true;
    }

    /**
     * @param currentProcessTaskStepVo
     * @param processTaskVo
     * @Description: 验证基本信息数据是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo, processTaskVo]
     * @Returns:boolean
     */
    @Override
    public boolean baseInfoValid(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskVo processTaskVo) {
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
        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(processTaskVo.getChannelUuid());
        if (StringUtils.isBlank(processTaskVo.getPriorityUuid())) {
            //且存在channelPriorityList,则优先级必填
            if (CollectionUtils.isNotEmpty(channelPriorityList)) {
                throw new ProcessTaskPriorityIsEmptyException();
            }
        }
        if (CollectionUtils.isNotEmpty(channelPriorityList) && channelPriorityList.stream().noneMatch(o -> o.getPriorityUuid().equals(processTaskVo.getPriorityUuid()))) {
            throw new ProcessTaskPriorityNotMatchException();
        }

        paramObj.put(ProcessTaskAuditDetailType.PRIORITY.getParamName(), processTaskVo.getPriorityUuid());

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
     * @param currentProcessTaskStepVo
     * @Description: 验证前置步骤指派处理人是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
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
        int isOnlyOnceExecute = 0;
        IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(WorkerPolicy.PRESTEPASSIGN.getValue());
        if (workerPolicyHandler == null) {
            isOnlyOnceExecute = workerPolicyHandler.isOnlyOnceExecute();
        }
        for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
            if (!WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                continue;
            }
            JSONObject configObj = workerPolicyVo.getConfigObj();
            if (MapUtils.isEmpty(configObj)) {
                continue;
            }
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
        return true;
    }

    /**
     * 找出流转到哪些步骤时，需要指定targetStepId步骤的处理人
     * @param processTaskId 工单id
     * @param currentStepId 当前流转步骤id
     * @param targetStepId 配置了由当前步骤处理人指定处理人的步骤id
     * @return
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

    /**
     * @param currentProcessTaskStepVo
     * @param targerProcessTaskStepId
     * @param reason
     * @param ation
     * @Description: 保存步骤提醒
     * @Author: linbq
     * @Date: 2021/1/21 11:30
     * @Params:[currentProcessTaskStepVo, targerProcessTaskStepId, reason, ation]
     * @Returns:int
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
     * @param currentProcessTaskStepVo
     * @param action
     * @Description: 保存描述内容和附件
     * @Author: linbq
     * @Date: 2021/1/27 11:41
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     */
    @Override
    public void saveContentAndFile(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskOperationType action) {
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        String content = paramObj.getString("content");
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(paramObj.getJSONArray("fileIdList")), Long.class);
//        if (StringUtils.isBlank(content) && CollectionUtils.isEmpty(fileIdList)) {
//            return;
//        }
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

        /** 保存附件uuid **/
        if (CollectionUtils.isNotEmpty(fileIdList)) {
            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
            processTaskStepFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
            processTaskStepFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
            processTaskStepFileVo.setContentId(processTaskStepContentVo.getId());
            for (Long fileId : fileIdList) {
                processTaskStepFileVo.setFileId(fileId);
                processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
            }
        } else {
            paramObj.remove("fileIdList");
        }
    }

    @Override
    public void chechContentIsRequired(ProcessTaskStepVo currentProcessTaskStepVo) {
        if (Objects.equals(currentProcessTaskStepVo.getIsNeedContent(), 0)) {
            return;
        }
        if(Objects.equals(currentProcessTaskStepVo.getIsRequired(), 1)) {
            JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
            String content = paramObj.getString("content");
            if (StringUtils.isBlank(content)) {
                List<ProcessTaskStepContentVo> contentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(currentProcessTaskStepVo.getId());
                if (CollectionUtils.isEmpty(contentList)) {
                    throw new ProcessTaskStepContentIsEmptyException();
                }
                Date startTime = currentProcessTaskStepVo.getStartTime();
                if (startTime != null) {
                    for (ProcessTaskStepContentVo contentVo : contentList) {
                        if (startTime.before(contentVo.getLcd())) {
                            return;
                        }
                    }
                    throw new ProcessTaskStepContentIsEmptyException();
                }
            }
        }
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 保存标签列表
     * @Author: linbq
     * @Date: 2021/1/27 11:42
     * @Params:[currentProcessTaskStepVo]
     * @Returns:void
     */
    @Override
    public void saveTagList(ProcessTaskStepVo currentProcessTaskStepVo) {
        processTaskMapper.deleteProcessTaskTagByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        JSONArray tagArray = paramObj.getJSONArray("tagList");
        if (CollectionUtils.isNotEmpty(tagArray)) {
            List<String> tagNameList = JSONObject.parseArray(tagArray.toJSONString(), String.class);
            List<ProcessTagVo> existTagList = processTagMapper.getProcessTagByNameList(tagNameList);
            List<String> existTagNameList = existTagList.stream().map(ProcessTagVo::getName).collect(Collectors.toList());
            List<String> notExistTagList = ListUtils.removeAll(tagNameList, existTagNameList);
            for (String tagName : notExistTagList) {
                ProcessTagVo tagVo = new ProcessTagVo(tagName);
                processTagMapper.insertProcessTag(tagVo);
                existTagList.add(tagVo);
            }
            List<ProcessTaskTagVo> processTaskTagVoList = new ArrayList<ProcessTaskTagVo>();
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

    /**
     * @param currentProcessTaskStepVo
     * @Description: 保存表单属性值
     * @Author: linbq
     * @Date: 2021/1/27 11:42
     * @Params:[currentProcessTaskStepVo]
     * @Returns:void
     */
    @Override
    public void saveForm(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        if (processTaskFormVo != null) {
            JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
            JSONArray formAttributeDataList = paramObj.getJSONArray("formAttributeDataList");
            if (formAttributeDataList == null) {
                /** 如果参数中没有formAttributeDataList字段，则不改变表单属性值，这样可以兼容自动节点自动完成场景 **/
                return;
            }
            Map<String, Object> formAttributeDataMap = new HashMap<>();
            for (int i = 0; i < formAttributeDataList.size(); i++) {
                JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                formAttributeDataMap.put(formAttributeDataObj.getString("attributeUuid"), formAttributeDataObj.get("dataList"));
            }
            /** 隐藏的属性uuid列表 **/
            List<String> hidecomponentList = new ArrayList<>();
            JSONArray hidecomponentArray = paramObj.getJSONArray("hidecomponentList");
            if (CollectionUtils.isNotEmpty(hidecomponentArray)) {
                hidecomponentList = hidecomponentArray.toJavaList(String.class);
            }
            /** 只读的属性uuid列表 **/
            List<String> readcomponentList = new ArrayList<>();
            JSONArray readcomponentArray = paramObj.getJSONArray("readcomponentList");
            if (CollectionUtils.isNotEmpty(readcomponentArray)) {
                readcomponentList = readcomponentArray.toJavaList(String.class);
            }

            /** 校验表单属性是否合法 **/
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            FormVersionVo formVersionVo = new FormVersionVo();
            formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
            formVersionVo.setFormName(processTaskFormVo.getFormName());
            formVersionVo.setFormConfig(JSONObject.parseObject(formContent));
            if (StringUtils.isBlank(currentProcessTaskStepVo.getConfigHash())) {
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                currentProcessTaskStepVo.setConfigHash(stepVo.getConfigHash());
            }
            formVersionVo.setSceneUuid(currentProcessTaskStepVo.getFormSceneUuid());
            List<FormAttributeVo> formAttributeVoList = formVersionVo.getFormAttributeList();
//            FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processTaskFormVo.getFormUuid());
            Map<String, String> attributeLabelMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(formAttributeVoList)) {
                Map<String, FormAttributeVo> formAttributeMap = new HashMap<>();
                for (FormAttributeVo formAttributeVo : formAttributeVoList) {
                    formAttributeMap.put(formAttributeVo.getUuid(), formAttributeVo);
                    attributeLabelMap.put(formAttributeVo.getUuid(), formAttributeVo.getLabel());
                    if (formAttributeVo.isRequired()) {
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
                // 对表格输入组件中密码password类型的单元格数据进行加密
                IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
                for (int i = 0; i < formAttributeDataList.size(); i++) {
                    JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                    String attributeUuid = formAttributeDataObj.getString("attributeUuid");
                    FormAttributeVo formAttributeVo = formAttributeMap.get(attributeUuid);
                    if (formAttributeVo != null) {
                        if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMTABLEINPUTER.getHandler())) {
                            JSONArray dataList = formAttributeDataObj.getJSONArray("dataList");
                            formCrossoverService.staticListPasswordEncrypt(dataList, formAttributeVo.getConfigObj());
                        }
                    }
                }
            }
            /** 获取旧表单数据 **/
            List<ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
            Iterator<ProcessTaskFormAttributeDataVo> iterator = oldProcessTaskFormAttributeDataList.iterator();
            while (iterator.hasNext()) {
                ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo = iterator.next();
                String attributeUuid = processTaskFormAttributeDataVo.getAttributeUuid();
                Object dataList = formAttributeDataMap.get(attributeUuid);
                if (dataList != null && Objects.equals(dataList, processTaskFormAttributeDataVo.getDataObj())) {
                    /** 如果新表单属性值与旧表单属性值相同，就不用replace更新数据了 **/
                    formAttributeDataMap.remove(attributeUuid);
                    iterator.remove();
                }
                /*else if (hidecomponentList.contains(attributeUuid)) {
                    iterator.remove();
                }*/
            }
            if (CollectionUtils.isNotEmpty(oldProcessTaskFormAttributeDataList)) {
                oldProcessTaskFormAttributeDataList.sort(ProcessTaskFormAttributeDataVo::compareTo);
                paramObj.put(ProcessTaskAuditDetailType.FORM.getOldDataParamName(), JSON.toJSONString(oldProcessTaskFormAttributeDataList));
            }

            /** 写入当前步骤的表单属性值 **/
            if (CollectionUtils.isNotEmpty(formAttributeDataList) && MapUtils.isNotEmpty(formAttributeDataMap)) {
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = new ArrayList<>(formAttributeDataList.size());
                for (int i = 0; i < formAttributeDataList.size(); i++) {
                    JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                    String attributeUuid = formAttributeDataObj.getString("attributeUuid");
                    if (formAttributeDataMap.containsKey(attributeUuid)) {
//                        // 对于隐藏的属性，当前用户不能修改，不更新数据库中的值，不进行修改前后对比
//                        if (hidecomponentList.contains(attributeUuid)) {
//                            continue;
//                        }
                        ProcessTaskFormAttributeDataVo attributeData = new ProcessTaskFormAttributeDataVo();
                        attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                        attributeData.setData(formAttributeDataObj.getString("dataList"));
                        attributeData.setAttributeUuid(attributeUuid);
                        String attributeLabel = attributeLabelMap.get(attributeUuid);
                        attributeData.setAttributeLabel(attributeLabel);
                        attributeData.setType(formAttributeDataObj.getString("handler"));
                        attributeData.setSort(i);
                        processTaskFormAttributeDataList.add(attributeData);
                        processTaskMapper.insertProcessTaskFormAttributeData(attributeData);
                    }
                }
                if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
                    processTaskFormAttributeDataList.sort(ProcessTaskFormAttributeDataVo::compareTo);
                    paramObj.put(ProcessTaskAuditDetailType.FORM.getParamName(), JSON.toJSONString(processTaskFormAttributeDataList));
                }
            }
        }
    }
}
