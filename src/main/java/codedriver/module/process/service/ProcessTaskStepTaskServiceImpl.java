/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.task.TaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepUnRunningException;
import codedriver.framework.process.exception.processtask.task.*;
import codedriver.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import codedriver.framework.process.service.ProcessTaskAgentService;
import codedriver.framework.process.service.ProcessTaskAgentServiceImpl;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.process.task.TaskConfigManager;
import codedriver.framework.service.AuthenticationInfoService;
import codedriver.framework.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lvzk
 * @since 2021/8/31 11:49
 **/
@Service
public class ProcessTaskStepTaskServiceImpl implements ProcessTaskStepTaskService {
    @Resource
    ProcessTaskMapper processTaskMapper;
    @Resource
    UserService userService;
    @Resource
    UserMapper userMapper;
    @Resource
    ProcessTaskStepTaskMapper processTaskStepTaskMapper;
    @Resource
    SelectContentByHashMapper selectContentByHashMapper;
    @Resource
    TaskMapper taskMapper;
    @Resource
    private IProcessStepHandlerUtil IProcessStepHandlerUtil;
    @Resource
    TaskConfigManager taskConfigManager;
    @Resource
    ProcessTaskService processTaskService;
    @Resource
    ProcessTaskAgentServiceImpl processTaskAgentServiceImpl;
    @Resource
    AuthenticationInfoService authenticationInfoService;
    @Resource
    ProcessTaskAgentService processTaskAgentService;
    /**
     * 创建任务
     *
     * @param processTaskStepTaskVo 任务参数
     */
    @Override
    public void saveTask(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo, boolean isCreate) {
        //获取流程步骤配置中的 任务策略和人员范围
        JSONObject taskConfig = getTaskConfig(processTaskStepVo.getConfigHash());
        if (MapUtils.isEmpty(taskConfig)) {
            throw new TaskConfigException(processTaskStepVo.getName());
        }
        List<Long> taskConfigIdList = taskConfig.getJSONArray("idList").toJavaList(Long.class);
        TaskConfigVo taskConfigVo = taskMapper.getTaskConfigById(processTaskStepTaskVo.getTaskConfigId());
        if (!taskConfigIdList.contains(processTaskStepTaskVo.getTaskConfigId()) || taskConfigVo == null) {
            throw new ProcessTaskStepTaskConfigIllegalException(processTaskStepTaskVo.getTaskConfigId().toString());
        }
        //判断人数是否合法
        if(processTaskStepTaskVo.getUserList() == null || ( taskConfigVo.getNum() != -1 && taskConfigVo.getNum() !=  processTaskStepTaskVo.getUserList().size())){
            throw new ProcessTaskStepTaskUserCountIllegalException(taskConfigVo.getName(),taskConfigVo.getNum());
        }
        processTaskStepTaskVo.setTaskConfigId(processTaskStepTaskVo.getTaskConfigId());
        //content
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskVo.getContent());
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
        processTaskStepTaskVo.setContentHash(processTaskContentVo.getHash());
        JSONArray rangeList = taskConfig.getJSONArray("rangeList");

        ProcessTaskAuditType auditType = ProcessTaskAuditType.CREATETASK;
        ProcessTaskStepTaskNotifyTriggerType triggerType = ProcessTaskStepTaskNotifyTriggerType.CREATETASK;
        if (isCreate) {
//            processTaskStepTaskVo.setStatus(ProcessTaskStatus.PENDING.getValue());
            processTaskStepTaskMapper.insertTask(processTaskStepTaskVo);
        } else {
            //processTaskStepTaskMapper.getStepTaskLockById(processTaskStepTaskVo.getId());
            processTaskStepTaskMapper.updateTask(processTaskStepTaskVo);
            //用户删除标记
            processTaskStepTaskMapper.updateDeleteTaskUserByUserListAndId(processTaskStepTaskVo.getUserList(), processTaskStepTaskVo.getId(), 1);
            //去掉用户删除标记
            processTaskStepTaskMapper.updateDeleteTaskUserByUserListAndId(processTaskStepTaskVo.getUserList(), processTaskStepTaskVo.getId(), 0);
            auditType = ProcessTaskAuditType.EDITTASK;
            triggerType = ProcessTaskStepTaskNotifyTriggerType.EDITTASK;
        }
        if (CollectionUtils.isNotEmpty(rangeList)) {
            //校验用户是否在配置范围内
            checkUserIsLegal(processTaskStepTaskVo.getUserList().stream().map(Object::toString).collect(Collectors.toList()), rangeList.stream().map(Object::toString).collect(Collectors.toList()));
        }
        processTaskStepTaskVo.getUserList().forEach(t -> {
            processTaskStepTaskMapper.insertIgnoreTaskUser(new ProcessTaskStepTaskUserVo(processTaskStepTaskVo.getId(), t, ProcessTaskStatus.PENDING.getValue()));
        });
//        processTaskService.refreshStepMinorWorker(processTaskStepVo, processTaskStepTaskVo);
//        processTaskService.refreshStepMinorUser(processTaskStepVo, processTaskStepTaskVo);
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        handler.updateProcessTaskStepUserAndWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());
        //活动参数
        JSONObject paramObj = new JSONObject();
        paramObj.put("replaceable_task", taskConfigVo.getName());
        processTaskStepVo.getParamObj().putAll(paramObj);
        processTaskStepTaskVo.setTaskConfigName(taskConfigVo.getName());
        processTaskStepVo.setProcessTaskStepTaskVo(processTaskStepTaskVo);
        processTaskStepTaskVo.setStepTaskUserVoList(processTaskStepTaskMapper.getStepTaskUserByStepTaskIdList(Collections.singletonList(processTaskStepTaskVo.getId())));
        IProcessStepHandlerUtil.audit(processTaskStepVo, auditType);
        IProcessStepHandlerUtil.notify(processTaskStepVo, triggerType);
        IProcessStepHandlerUtil.action(processTaskStepVo, triggerType);
    }



    /**
     * 完成任务
     *
     * @param processTaskStepTaskUserVo 任务用户参数
     */
    @Override
    public Long completeTask(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo) {
        Long id = processTaskStepTaskUserVo.getId();
        Long userContentId = processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId();
        String content = processTaskStepTaskUserVo.getContent();
        processTaskStepTaskUserVo = processTaskStepTaskMapper.getStepTaskUserById(processTaskStepTaskUserVo.getId());
        if (processTaskStepTaskUserVo == null) {
            throw new ProcessTaskStepTaskUserNotFoundException(id);
        }
        //processTaskStepTaskMapper.getStepTaskLockById(processTaskStepTaskUserVo.getProcessTaskStepTaskId());
        //回复的stepUserId 的用户得和 当前登录用户一致
        if (!Objects.equals(processTaskStepTaskUserVo.getUserUuid(), UserContext.get().getUserUuid())) {
            throw new ProcessTaskStepTaskUserException(processTaskStepTaskUserVo.getId());
        }
        processTaskStepTaskUserVo.setContent(content);
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskDetailById(processTaskStepTaskUserVo.getProcessTaskStepTaskId());
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(processTaskStepTaskUserVo.getProcessTaskStepTaskId().toString());
        }
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(stepTaskVo.getProcessTaskStepId());
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(stepTaskVo.getProcessTaskStepId().toString());
        }
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskStepVo.getProcessTaskId());
        if (!Objects.equals(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo.getStatus())) {
            throw new ProcessTaskStepUnRunningException();
        }
        processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
        //update 更新内容
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskUserVo.getContent());
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
        processTaskStepTaskUserVo.setContentHash(processTaskContentVo.getHash());

        //活动参数
        JSONObject paramObj = new JSONObject();
        paramObj.put("replaceable_task", stepTaskVo.getTaskConfigName());
        processTaskStepVo.getParamObj().putAll(paramObj);
        processTaskStepVo.setProcessTaskStepTaskVo(stepTaskVo);
        stepTaskVo.setStepTaskUserVoList(processTaskStepTaskMapper.getStepTaskUserByStepTaskIdListAndUserUuid(Collections.singletonList(stepTaskVo.getId()), UserContext.get().getUserUuid()));
        stepTaskVo.setTaskStepTaskUserContent(content);
        //判断满足任务流转条件，触发通知
        List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskWithUserByProcessTaskStepId(stepTaskVo.getProcessTaskStepId());
        if (CollectionUtils.isNotEmpty(stepTaskVoList)) {
            boolean isCanStepComplete = true;
            for (ProcessTaskStepTaskVo stepTask : stepTaskVoList) {
                TaskConfigManager.Action<ProcessTaskStepTaskVo> action = taskConfigManager.getConfigMap().get(stepTaskVo.getTaskConfigPolicy());
                if (action != null && !action.execute(stepTask)) {
                    isCanStepComplete = false;
                    break;
                }
            }
            if (isCanStepComplete) {
                IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETEALLTASK);
            }
        }
        IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.COMPLETETASK);
        IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK);
        IProcessStepHandlerUtil.action(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK);

        //新增回复
        Long processTaskStepTaskUserContentId;
        processTaskStepTaskMapper.updateTaskUserByTaskIdAndUserUuid(ProcessTaskStatus.SUCCEED.getValue(), processTaskStepTaskUserVo.getProcessTaskStepTaskId(), processTaskStepTaskUserVo.getUserUuid());
        if (userContentId == null) {
            ProcessTaskStepTaskUserContentVo contentVo = new ProcessTaskStepTaskUserContentVo(processTaskStepTaskUserVo);
            processTaskStepTaskMapper.insertTaskUserContent(contentVo);
            //刷新worker
            processTaskService.refreshStepMinorWorker(processTaskStepVo, new ProcessTaskStepTaskVo(processTaskStepTaskUserVo.getProcessTaskStepTaskId()));
            processTaskStepTaskUserContentId =  contentVo.getId();
        } else {//编辑回复
            ProcessTaskStepTaskUserContentVo userContentVo = processTaskStepTaskMapper.getStepTaskUserContentByIdAndUserUuid(userContentId, UserContext.get().getUserUuid());
            if (userContentVo == null) {
                throw new ProcessTaskStepTaskUserContentNotFoundException();
            }
            processTaskStepTaskMapper.updateTaskUserContent(userContentId, processTaskStepTaskUserVo.getContentHash(), UserContext.get().getUserUuid());
            processTaskStepTaskUserContentId =  processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId();
        }

        //跟新stepUser
        processTaskService.refreshStepMinorUser(processTaskStepVo,stepTaskVo);
        return processTaskStepTaskUserContentId;
    }

    /**
     * 完成任务
     *
     * @param processTaskStepTaskUserVo 任务用户参数
     */
//    @Override
    public Long completeTask2(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo) throws Exception {
        Long stepTaskUserId = processTaskStepTaskUserVo.getId();
        Long userContentId = processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId();
        String content = processTaskStepTaskUserVo.getContent();

        ProcessTaskStepTaskUserVo oldProcessTaskStepTaskUserVo = processTaskStepTaskMapper.getStepTaskUserById(stepTaskUserId);
        if (oldProcessTaskStepTaskUserVo == null) {
            throw new ProcessTaskStepTaskUserNotFoundException(stepTaskUserId);
        }
        //processTaskStepTaskMapper.getStepTaskLockById(processTaskStepTaskUserVo.getProcessTaskStepTaskId());
        //回复的stepUserId 的用户得和 当前登录用户一致
//        if (!Objects.equals(oldProcessTaskStepTaskUserVo.getUserUuid(), UserContext.get().getUserUuid())) {
//            throw new ProcessTaskStepTaskUserException(stepTaskUserId);
//        }
//        processTaskStepTaskUserVo.setContent(content);
        Long stepTaskId = oldProcessTaskStepTaskUserVo.getProcessTaskStepTaskId();
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskDetailById(stepTaskId);
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(stepTaskId.toString());
        }
        Long processTaskId = stepTaskVo.getProcessTaskId();
        Long processTaskStepId = stepTaskVo.getProcessTaskStepId();
//        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
//        if (processTaskStepVo == null) {
//            throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
//        }
//        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
//        if (processTaskVo == null) {
//            throw new ProcessTaskNotFoundException(processTaskId.toString());
//        }
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskId);
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();

        if (!Objects.equals(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo.getStatus())) {
            throw new ProcessTaskStepUnRunningException();
        }
        if (checkIsReplyable(processTaskVo.getChannelUuid(), oldProcessTaskStepTaskUserVo.getUserUuid()) == 0) {
            //TODO 没权限
        }
        processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
        //update 更新内容
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
        String contentHash = processTaskContentVo.getHash();
//        processTaskStepTaskUserVo.setContentHash(processTaskContentVo.getHash());

        //活动参数
        JSONObject paramObj = new JSONObject();
        paramObj.put("replaceable_task", stepTaskVo.getTaskConfigName());
        processTaskStepVo.getParamObj().putAll(paramObj);
        processTaskStepVo.setProcessTaskStepTaskVo(stepTaskVo);
        stepTaskVo.setStepTaskUserVoList(processTaskStepTaskMapper.getStepTaskUserByStepTaskIdListAndUserUuid(Collections.singletonList(stepTaskVo.getId()), UserContext.get().getUserUuid()));
        stepTaskVo.setTaskStepTaskUserContent(content);
        //判断满足任务流转条件，触发通知
        List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskWithUserByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(stepTaskVoList)) {
            boolean isCanStepComplete = true;
            for (ProcessTaskStepTaskVo stepTask : stepTaskVoList) {
                TaskConfigManager.Action<ProcessTaskStepTaskVo> action = taskConfigManager.getConfigMap().get(stepTaskVo.getTaskConfigPolicy());
                if (action != null && !action.execute(stepTask)) {
                    isCanStepComplete = false;
                    break;
                }
            }
            if (isCanStepComplete) {
                IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETEALLTASK);
            }
        }
        IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.COMPLETETASK);
        IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK);
        IProcessStepHandlerUtil.action(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK);

        //新增回复
        processTaskStepTaskUserVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
        processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
        processTaskStepTaskMapper.updateTaskUserById(processTaskStepTaskUserVo);
        if (userContentId == null) {
            ProcessTaskStepTaskUserContentVo contentVo = new ProcessTaskStepTaskUserContentVo();
            contentVo.setProcessTaskStepTaskId(stepTaskId);
            contentVo.setProcessTaskStepTaskUserId(stepTaskUserId);
            contentVo.setContentHash(contentHash);
            contentVo.setUserUuid(UserContext.get().getUserUuid());
            processTaskStepTaskMapper.insertTaskUserContent(contentVo);
            userContentId =  contentVo.getId();
            //刷新worker
//            processTaskService.refreshStepMinorWorker(processTaskStepVo, new ProcessTaskStepTaskVo(stepTaskId));
        } else {//编辑回复
            ProcessTaskStepTaskUserContentVo userContentVo = processTaskStepTaskMapper.getStepTaskUserContentById(userContentId);
            if (userContentVo == null) {
                throw new ProcessTaskStepTaskUserContentNotFoundException();
            }
            userContentVo.setContentHash(contentHash);
            userContentVo.setUserUuid(UserContext.get().getUserUuid());
            processTaskStepTaskMapper.updateTaskUserContentById(userContentVo);
        }

        //跟新stepUser
//        processTaskService.refreshStepMinorUser(processTaskStepVo,stepTaskVo);
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        handler.updateProcessTaskStepUserAndWorker(processTaskId, processTaskStepId);
        return userContentId;
    }
    /**
     * @param processTaskStepTaskUserVo
     * @return void
     * @Time:2020年9月30日
     * @Description: 步骤主处理人校正操作 判断当前用户是否是代办人，如果不是就什么都不做，如果是，进行下面3个操作 1.往processtask_step_agent表中插入一条数据，记录该步骤的原主处理人和代办人
     * 2.将processtask_step_worker表中该步骤的主处理人uuid改为代办人(当前用户)
     * 3.将processtask_step_user表中该步骤的主处理人user_uuid改为代办人(当前用户)
     */
//    private void stepMajorUserRegulate(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo) {
//        Long processTaskId = null;
//        Long processTaskStepId = null;
//        String currentUserUuid = UserContext.get().getUserUuid(true);
//        /* 能进入这个方法，说明当前用户有权限处理当前步骤，可能是三类处理人：第一处理人(A)、代办人(B)、代办人的代办人(C) 。其中A授权给B，B授权给C **/
//        ProcessTaskStepAgentVo processTaskStepAgentVo = processTaskMapper.getProcessTaskStepAgentByProcessTaskStepId(processTaskStepId);
//        if (processTaskStepAgentVo == null) {
//            // 代办人还没接管，当前用户可能是A和B
//            int flag = 0;
//            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
//            if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED.getValue())) {
//                ProcessTaskStepUserVo searchVo = new ProcessTaskStepUserVo(
//                        processTaskId,
//                        processTaskStepId,
//                        currentUserUuid,
//                        ProcessUserType.MAJOR.getValue()
//                );
//                flag = processTaskMapper.checkIsProcessTaskStepUser(searchVo);
//            } else {
//                AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(currentUserUuid);
//                flag = processTaskMapper.checkIsWorker(processTaskId, processTaskStepId, ProcessUserType.MAJOR.getValue(), authenticationInfoVo);
//            }
//
//            if (flag == 0) {
//                // 当用户是B
//                String userUuid = null;
//                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
//                List<String> fromUserUuidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(), processTaskVo.getChannelUuid());
//                for (String fromUserUuid : fromUserUuidList) {
//                    if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED.getValue())) {
//                        ProcessTaskStepUserVo searchVo = new ProcessTaskStepUserVo(
//                                processTaskId,
//                                processTaskStepId,
//                                fromUserUuid,
//                                ProcessUserType.MAJOR.getValue()
//                        );
//                        if (processTaskMapper.checkIsProcessTaskStepUser(searchVo) > 0) {
//                            userUuid = fromUserUuid;
//                            break;
//                        }
//                    } else {
//                        AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(fromUserUuid);
//                        if (processTaskMapper.checkIsWorker(processTaskId, processTaskStepId, ProcessUserType.MAJOR.getValue(), authenticationInfoVo) > 0) {
//                            userUuid = fromUserUuid;
//                            break;
//                        }
//                    }
//                }
////                String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(), "processTask");
//                if (StringUtils.isNotBlank(userUuid)) {
//                    ProcessTaskStepAgentVo processTaskStepAgent = new ProcessTaskStepAgentVo(
//                            processTaskId,
//                            processTaskStepId,
//                            userUuid,
//                            currentUserUuid
//                    );
//                    processTaskMapper.replaceProcessTaskStepAgent(processTaskStepAgent);
//                    ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo(
//                            processTaskId,
//                            processTaskStepId,
//                            GroupSearch.USER.getValue(),
//                            userUuid,
//                            ProcessUserType.MAJOR.getValue(),
//                            currentUserUuid
//                    );
//                    processTaskMapper.updateProcessTaskStepWorkerUuid(processTaskStepWorkerVo);
//                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(
//                            processTaskId,
//                            processTaskStepId,
//                            userUuid,
//                            ProcessUserType.MAJOR.getValue(),
//                            currentUserUuid
//                    );
//                    processTaskMapper.updateProcessTaskStepUserUserUuid(processTaskStepUserVo);
//                    currentProcessTaskStepVo.setOriginalUser(userUuid);
//                }
//            }
//        } else {
//            // 代办人接管过了，当前用户可能是A、B、C
//            if (currentUserUuid.equals(processTaskStepAgentVo.getUserUuid())) {
//                // 当前用户是A
//                processTaskMapper.deleteProcessTaskStepAgentByProcessTaskStepId(processTaskStepId);
//                ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo(
//                        processTaskId,
//                        processTaskStepId,
//                        GroupSearch.USER.getValue(),
//                        processTaskStepAgentVo.getAgentUuid(),
//                        ProcessUserType.MAJOR.getValue(),
//                        currentUserUuid
//                );
//                processTaskMapper.updateProcessTaskStepWorkerUuid(processTaskStepWorkerVo);
//                ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(
//                        processTaskId,
//                        processTaskStepId,
//                        processTaskStepAgentVo.getAgentUuid(),
//                        ProcessUserType.MAJOR.getValue(),
//                        currentUserUuid
//                );
//                processTaskMapper.updateProcessTaskStepUserUserUuid(processTaskStepUserVo);
//            } else if (currentUserUuid.equals(processTaskStepAgentVo.getAgentUuid())) {
//                // 当前用户是B
//                currentProcessTaskStepVo.setOriginalUser(processTaskStepAgentVo.getUserUuid());
//            } else {
//                // 当前用户是C
//                ProcessTaskStepAgentVo processTaskStepAgent = new ProcessTaskStepAgentVo(
//                        processTaskId,
//                        processTaskStepId,
//                        processTaskStepAgentVo.getAgentUuid(),
//                        currentUserUuid
//                );
//                processTaskMapper.replaceProcessTaskStepAgent(processTaskStepAgent);
//                ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo(
//                        processTaskId,
//                        processTaskStepId,
//                        GroupSearch.USER.getValue(),
//                        processTaskStepAgentVo.getAgentUuid(),
//                        ProcessUserType.MAJOR.getValue(),
//                        currentUserUuid
//                );
//                processTaskMapper.updateProcessTaskStepWorkerUuid(processTaskStepWorkerVo);
//                ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(
//                        processTaskId,
//                        processTaskStepId,
//                        processTaskStepAgentVo.getAgentUuid(),
//                        ProcessUserType.MAJOR.getValue(),
//                        currentUserUuid
//                );
//                processTaskMapper.updateProcessTaskStepUserUserUuid(processTaskStepUserVo);
//                currentProcessTaskStepVo.setOriginalUser(processTaskStepAgentVo.getAgentUuid());
//            }
//        }
//
//    }
    /**
     * 解析&校验 任务配置
     *
     * @param stepConfigHash 步骤配置hash
     * @return 任务配置
     */
    private JSONObject getTaskConfig(String stepConfigHash) {
        if (StringUtils.isNotBlank(stepConfigHash)) {
            String stepConfigStr = selectContentByHashMapper.getProcessTaskStepConfigByHash(stepConfigHash);
            if (StringUtils.isNotBlank(stepConfigStr)) {
                JSONObject stepConfig = JSONObject.parseObject(stepConfigStr);
                if (MapUtils.isNotEmpty(stepConfig)) {
                    JSONObject taskConfig = stepConfig.getJSONObject("taskConfig");
                    if (MapUtils.isNotEmpty(taskConfig)) {
                        return taskConfig;
                    }
                }
            }
        }
        return null;
    }


    /**
     * 检查用户是否合法
     *
     * @param userUuidList 用户uuidList
     * @param rangeList    用户范围
     */
    private void checkUserIsLegal(List<String> userUuidList, List<String> rangeList) {
        UserVo userVo = new UserVo();
        userVo.setCurrentPage(1);
        userVo.setIsDelete(0);
        userVo.setIsActive(1);
        userService.getUserByRangeList(userVo, rangeList);
        List<String> legalUserUuidList = userMapper.checkUserInRangeList(userUuidList, userVo);
        if (legalUserUuidList.size() != userUuidList.size()) {
            userUuidList.removeAll(legalUserUuidList);
            throw new TaskUserIllegalException(String.join(",", userUuidList));
        }
    }

    /**
     * 获取工单任务信息
     *
     * @param processTaskStepVo 步骤vo
     */
    @Override
    public void getProcessTaskStepTask(ProcessTaskStepVo processTaskStepVo) {
        //任务列表
        Map<String, List<ProcessTaskStepTaskVo>> stepTaskVoMap = new HashMap<>();
        Map<Long, List<ProcessTaskStepTaskUserVo>> stepTaskUserVoMap = new HashMap<>();
        Map<Long, List<ProcessTaskStepTaskUserContentVo>> stepTaskUserContentVoMap = new HashMap<>();
        List<ProcessTaskStepTaskUserVo> stepTaskUserVoList;
        List<ProcessTaskStepTaskUserContentVo> stepTaskUserContentVoList;
        //默认存在所有task 的tab
        JSONObject taskConfig = getTaskConfig(processTaskStepVo.getConfigHash());
        if (MapUtils.isNotEmpty(taskConfig)) {
            List<Long> taskConfigIdList = taskConfig.getJSONArray("idList").toJavaList(Long.class);
            if (CollectionUtils.isNotEmpty(taskConfigIdList)) {
                List<TaskConfigVo> taskConfigVoList = taskMapper.getTaskConfigByIdList(JSONArray.parseArray(JSON.toJSONString(taskConfigIdList)));
                for (TaskConfigVo taskConfigVo : taskConfigVoList) {
                    stepTaskVoMap.put(taskConfigVo.getName(), new ArrayList<>());
                }

                List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskByProcessTaskStepId(processTaskStepVo.getId());
                if (MapUtils.isNotEmpty(stepTaskVoMap) && CollectionUtils.isNotEmpty(stepTaskVoList)) {
                    stepTaskUserVoList = processTaskStepTaskMapper.getStepTaskUserByStepTaskIdList(stepTaskVoList.stream().map(ProcessTaskStepTaskVo::getId).collect(Collectors.toList()));
                    if (CollectionUtils.isNotEmpty(stepTaskUserVoList)) {
                        stepTaskUserContentVoList = processTaskStepTaskMapper.getStepTaskUserContentByStepTaskUserIdList(stepTaskUserVoList.stream().map(ProcessTaskStepTaskUserVo::getId).collect(Collectors.toList()));
                        //任务用户回复
                        stepTaskUserContentVoList.forEach(stuc -> {
                            if (!stepTaskUserContentVoMap.containsKey(stuc.getProcessTaskStepTaskUserId())) {
                                stepTaskUserContentVoMap.put(stuc.getProcessTaskStepTaskUserId(), new ArrayList<>());
                            }
                            stepTaskUserContentVoMap.get(stuc.getProcessTaskStepTaskUserId()).add(stuc);
                        });
                        //任务用户
                        stepTaskUserVoList.forEach(stu -> {
                            if (!stepTaskUserVoMap.containsKey(stu.getProcessTaskStepTaskId())) {
                                stepTaskUserVoMap.put(stu.getProcessTaskStepTaskId(), new ArrayList<>());
                            }
                            stu.setStepTaskUserContentVoList(stepTaskUserContentVoMap.get(stu.getId()));
                            //仅回显需要回复的 或 已经回复过的用户
                            if (stu.getIsDelete() == 0 || CollectionUtils.isNotEmpty(stu.getStepTaskUserContentVoList())) {
                                stepTaskUserVoMap.get(stu.getProcessTaskStepTaskId()).add(stu);
                            }
                        });
                        //任务
                        stepTaskVoList.forEach(st -> {
                            st.setStepTaskUserVoList(stepTaskUserVoMap.get(st.getId()));
                            stepTaskVoMap.get(st.getTaskConfigName()).add(st);
                        });
                    }
                }
                JSONObject stepTaskJson = new JSONObject() {{
                    put("taskTabList", JSONObject.parseObject(JSONObject.toJSONString(stepTaskVoMap)));
                    String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
                    JSONObject stepConfigJson = JSONObject.parseObject(stepConfig);
                    JSONObject stepTaskConfigJson = stepConfigJson.getJSONObject("taskConfig");
                    if (MapUtils.isNotEmpty(stepTaskConfigJson)) {
                        JSONArray stepTaskIdList = stepTaskConfigJson.getJSONArray("idList");
                        if (CollectionUtils.isNotEmpty(stepTaskIdList)) {
                            List<TaskConfigVo> taskConfigVoList = taskMapper.getTaskConfigByIdList(stepTaskIdList);
                            if (taskConfigVoList.size() != stepTaskIdList.size()) {
                                throw new TaskConfigException(processTaskStepVo.getName());
                            }
                            //todo 控制权限，目前仅允许处理人创建策略
                            if (processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getStartProcessTaskStepId(), UserContext.get().getUserUuid(true))) > 0) {
                                put("taskActionList", taskConfigVoList);
                            }
                            put("rangeList", stepTaskConfigJson.getJSONArray("rangeList"));
                        }
                    }
                }};
                processTaskStepVo.setProcessTaskStepTask(stepTaskJson);
            }
        }
    }

    /**
     * 获取步骤的任务策略列表及其任务列表
     * @param processTaskStepVo 步骤信息
     * @return
     */
    @Override
    public List<TaskConfigVo> getTaskConfigList(ProcessTaskStepVo processTaskStepVo) {
        JSONObject taskConfig = getTaskConfig(processTaskStepVo.getConfigHash());
        if (MapUtils.isEmpty(taskConfig)) {
            return null;
        }
        JSONArray idArray = taskConfig.getJSONArray("idList");
        if (CollectionUtils.isEmpty(idArray)) {
            return null;
        }
        List<TaskConfigVo> taskConfigList = taskMapper.getTaskConfigByIdList(idArray);
        if (CollectionUtils.isEmpty(taskConfigList)) {
            return null;
        }
        taskConfigList.sort(Comparator.comparingInt(e -> idArray.indexOf(e.getId())));
        List<ProcessTaskStepTaskVo> processTaskStepTaskList = processTaskStepTaskMapper.getStepTaskByProcessTaskStepId(processTaskStepVo.getId());
        if (CollectionUtils.isEmpty(processTaskStepTaskList)) {
            return taskConfigList;
        }
        Map<Long, List<ProcessTaskStepTaskUserVo>> stepTaskUserMap = new HashMap<>();
        List<Long> stepTaskIdList= processTaskStepTaskList.stream().map(ProcessTaskStepTaskVo::getId).collect(Collectors.toList());
        List<ProcessTaskStepTaskUserVo> stepTaskUserList = processTaskStepTaskMapper.getStepTaskUserByStepTaskIdList(stepTaskIdList);
        if (CollectionUtils.isNotEmpty(stepTaskUserList)) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
            List<Long> stepTaskUserIdList = stepTaskUserList.stream().map(ProcessTaskStepTaskUserVo::getId).collect(Collectors.toList());
            List<ProcessTaskStepTaskUserContentVo> stepTaskUserContentList = processTaskStepTaskMapper.getStepTaskUserContentByStepTaskUserIdList(stepTaskUserIdList);
            Map<Long, ProcessTaskStepTaskUserContentVo> stepTaskUserContentMap = new HashMap<>();
            for (ProcessTaskStepTaskUserContentVo stepTaskUserContentVo : stepTaskUserContentList) {
                if (stepTaskUserContentMap.containsKey(stepTaskUserContentVo.getProcessTaskStepTaskUserId())) {
                    continue;
                }
                stepTaskUserContentMap.put(stepTaskUserContentVo.getProcessTaskStepTaskUserId(), stepTaskUserContentVo);
            }
            for (ProcessTaskStepTaskUserVo stepTaskUserVo : stepTaskUserList) {
                stepTaskUserVo.setIsReplyable(checkIsReplyable(processTaskVo.getChannelUuid(), stepTaskUserVo.getUserUuid()));
                ProcessTaskStepTaskUserContentVo stepTaskUserContentVo = stepTaskUserContentMap.get(stepTaskUserVo.getId());
                if (stepTaskUserContentVo != null) {
                    stepTaskUserVo.setContent(stepTaskUserContentVo.getContent());
                }
                stepTaskUserMap.computeIfAbsent(stepTaskUserVo.getProcessTaskStepTaskId(), key -> new ArrayList<>()).add(stepTaskUserVo);
            }
        }
        Map<Long, List<ProcessTaskStepTaskVo>> stepTaskMap = new HashMap<>();
        for (ProcessTaskStepTaskVo stepTaskVo : processTaskStepTaskList) {
            List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = stepTaskUserMap.get(stepTaskVo.getId());
            stepTaskVo.setStepTaskUserVoList(processTaskStepTaskUserList);
            stepTaskMap.computeIfAbsent(stepTaskVo.getTaskConfigId(), key -> new ArrayList<>()).add(stepTaskVo);
        }
        JSONArray rangeArray = taskConfig.getJSONArray("rangeList");
        for (TaskConfigVo taskConfigVo : taskConfigList) {
            List<ProcessTaskStepTaskVo> stepTaskList = stepTaskMap.get(taskConfigVo.getId());
            taskConfigVo.setProcessTaskStepTaskList(stepTaskList);
            if (CollectionUtils.isNotEmpty(rangeArray)) {
                taskConfigVo.setRangeList(rangeArray.toJavaList(String.class));
            }
        }
        return taskConfigList;
    }

    /**
     * 判断当前用户是否可以处理任务
     * @param channelUuid 服务uuid
     * @param stepTaskUserUuid 任务处理人uuid
     * @return
     */
    private int checkIsReplyable(String channelUuid, String stepTaskUserUuid) {
        if (Objects.equals(stepTaskUserUuid, UserContext.get().getUserUuid(true))) {
            return 1;
        }
        List<String> fromUuidList = processTaskAgentServiceImpl.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), channelUuid);
        if (fromUuidList.contains(stepTaskUserUuid)) {
            return 1;
        }
        return 0;
    }
}
