/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.task.TaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.operationauth.ProcessTaskHiddenException;
import codedriver.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import codedriver.framework.process.exception.operationauth.ProcessTaskStepNotActiveException;
import codedriver.framework.process.exception.operationauth.ProcessTaskStepNotMinorUserException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepUnRunningException;
import codedriver.framework.process.exception.processtask.task.*;
import codedriver.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import codedriver.framework.process.service.ProcessTaskAgentServiceImpl;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.process.task.TaskConfigManager;
import codedriver.framework.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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

//    /**
//     * 完成任务
//     *
//     * @param processTaskStepTaskUserVo 任务用户参数
//     */
//    @Override
//    public Long completeTask(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo) {
//        Long id = processTaskStepTaskUserVo.getId();
//        Long userContentId = processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId();
//        String content = processTaskStepTaskUserVo.getContent();
//        processTaskStepTaskUserVo = processTaskStepTaskMapper.getStepTaskUserById(processTaskStepTaskUserVo.getId());
//        if (processTaskStepTaskUserVo == null) {
//            throw new ProcessTaskStepTaskUserNotFoundException(id);
//        }
//        //processTaskStepTaskMapper.getStepTaskLockById(processTaskStepTaskUserVo.getProcessTaskStepTaskId());
//        //回复的stepUserId 的用户得和 当前登录用户一致
//        if (!Objects.equals(processTaskStepTaskUserVo.getUserUuid(), UserContext.get().getUserUuid())) {
//            throw new ProcessTaskStepTaskUserException(processTaskStepTaskUserVo.getId());
//        }
//        processTaskStepTaskUserVo.setContent(content);
//        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskDetailById(processTaskStepTaskUserVo.getProcessTaskStepTaskId());
//        if (stepTaskVo == null) {
//            throw new ProcessTaskStepTaskNotFoundException(processTaskStepTaskUserVo.getProcessTaskStepTaskId().toString());
//        }
//        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(stepTaskVo.getProcessTaskStepId());
//        if (processTaskStepVo == null) {
//            throw new ProcessTaskStepNotFoundException(stepTaskVo.getProcessTaskStepId().toString());
//        }
//        // 锁定当前流程
//        processTaskMapper.getProcessTaskLockById(processTaskStepVo.getProcessTaskId());
//        if (!Objects.equals(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo.getStatus())) {
//            throw new ProcessTaskStepUnRunningException();
//        }
//        processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
//        //update 更新内容
//        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskUserVo.getContent());
//        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
//        processTaskStepTaskUserVo.setContentHash(processTaskContentVo.getHash());
//
//        //活动参数
//        JSONObject paramObj = new JSONObject();
//        paramObj.put("replaceable_task", stepTaskVo.getTaskConfigName());
//        processTaskStepVo.getParamObj().putAll(paramObj);
//        processTaskStepVo.setProcessTaskStepTaskVo(stepTaskVo);
//        stepTaskVo.setStepTaskUserVoList(processTaskStepTaskMapper.getStepTaskUserByStepTaskIdListAndUserUuid(Collections.singletonList(stepTaskVo.getId()), UserContext.get().getUserUuid()));
//        stepTaskVo.setTaskStepTaskUserContent(content);
//        //判断满足任务流转条件，触发通知
//        List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskWithUserByProcessTaskStepId(stepTaskVo.getProcessTaskStepId());
//        if (CollectionUtils.isNotEmpty(stepTaskVoList)) {
//            boolean isCanStepComplete = true;
//            for (ProcessTaskStepTaskVo stepTask : stepTaskVoList) {
//                TaskConfigManager.Action<ProcessTaskStepTaskVo> action = taskConfigManager.getConfigMap().get(stepTaskVo.getTaskConfigPolicy());
//                if (action != null && !action.execute(stepTask)) {
//                    isCanStepComplete = false;
//                    break;
//                }
//            }
//            if (isCanStepComplete) {
//                IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETEALLTASK);
//            }
//        }
//        IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.COMPLETETASK);
//        IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK);
//        IProcessStepHandlerUtil.action(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK);
//
//        //新增回复
//        Long processTaskStepTaskUserContentId;
//        processTaskStepTaskMapper.updateTaskUserByTaskIdAndUserUuid(ProcessTaskStatus.SUCCEED.getValue(), processTaskStepTaskUserVo.getProcessTaskStepTaskId(), processTaskStepTaskUserVo.getUserUuid());
//        if (userContentId == null) {
//            ProcessTaskStepTaskUserContentVo contentVo = new ProcessTaskStepTaskUserContentVo(processTaskStepTaskUserVo);
//            processTaskStepTaskMapper.insertTaskUserContent(contentVo);
//            //刷新worker
//            processTaskService.refreshStepMinorWorker(processTaskStepVo, new ProcessTaskStepTaskVo(processTaskStepTaskUserVo.getProcessTaskStepTaskId()));
//            processTaskStepTaskUserContentId =  contentVo.getId();
//        } else {//编辑回复
//            ProcessTaskStepTaskUserContentVo userContentVo = processTaskStepTaskMapper.getStepTaskUserContentByIdAndUserUuid(userContentId, UserContext.get().getUserUuid());
//            if (userContentVo == null) {
//                throw new ProcessTaskStepTaskUserContentNotFoundException();
//            }
//            processTaskStepTaskMapper.updateTaskUserContent(userContentId, processTaskStepTaskUserVo.getContentHash(), UserContext.get().getUserUuid());
//            processTaskStepTaskUserContentId =  processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId();
//        }
//
//        //跟新stepUser
//        processTaskService.refreshStepMinorUser(processTaskStepVo,stepTaskVo);
//        return processTaskStepTaskUserContentId;
//    }


    /**
     * 完成任务
     *
     * @param id 任务id
     * @param content 回复内容
     */
    @Override
    public Long completeTask(Long id, String content) throws Exception {
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskDetailById(id);
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(id.toString());
        }
        Long processTaskId = stepTaskVo.getProcessTaskId();
        Long processTaskStepId = stepTaskVo.getProcessTaskStepId();
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskId);
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();

        //update 更新内容
        boolean isChange = false;
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
        String contentHash = processTaskContentVo.getHash();
        List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = processTaskStepTaskMapper.getStepTaskUserListByStepTaskId(id);
        for (ProcessTaskStepTaskUserVo oldProcessTaskStepTaskUserVo : processTaskStepTaskUserList) {
            if (Objects.equals(oldProcessTaskStepTaskUserVo.getIsDelete(), 1)) {
                continue;
            }
            try {
                Long stepTaskUserId = oldProcessTaskStepTaskUserVo.getId();
                checkIsReplyable(processTaskVo, processTaskStepVo, oldProcessTaskStepTaskUserVo.getUserUuid(), stepTaskUserId);
                stepMinorUserRegulate(oldProcessTaskStepTaskUserVo);

                //新增回复
                boolean isChangeContent = false;
                ProcessTaskStepTaskUserContentVo userContentVo = processTaskStepTaskMapper.getStepTaskUserContentByStepTaskUserId(stepTaskUserId);
                if (userContentVo == null) {
                    ProcessTaskStepTaskUserContentVo contentVo = new ProcessTaskStepTaskUserContentVo();
                    contentVo.setProcessTaskStepTaskId(id);
                    contentVo.setProcessTaskStepTaskUserId(stepTaskUserId);
                    contentVo.setContentHash(contentHash);
                    contentVo.setUserUuid(UserContext.get().getUserUuid());
                    processTaskStepTaskMapper.insertTaskUserContent(contentVo);
                    isChangeContent = true;
                } else {//编辑回复
                    if (!Objects.equals(userContentVo.getContentHash(), contentHash)) {
                        userContentVo.setContentHash(contentHash);
                        userContentVo.setUserUuid(UserContext.get().getUserUuid());
                        processTaskStepTaskMapper.updateTaskUserContentById(userContentVo);
                        isChangeContent = true;
                    }
                }
                if (isChangeContent || Objects.equals(oldProcessTaskStepTaskUserVo.getStatus(), ProcessTaskStatus.PENDING) || !Objects.equals(oldProcessTaskStepTaskUserVo.getUserUuid(), UserContext.get().getUserUuid())) {
                    ProcessTaskStepTaskUserVo processTaskStepTaskUserVo = new ProcessTaskStepTaskUserVo();
                    processTaskStepTaskUserVo.setId(stepTaskUserId);
                    processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
                    processTaskStepTaskUserVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
                    processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
                    processTaskStepTaskMapper.updateTaskUserById(processTaskStepTaskUserVo);
                    isChange = true;
                }
            } catch (ProcessTaskPermissionDeniedException processTaskPermissionDeniedException) {
                System.out.println(1);
            }
        }
        if (isChange) {
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

            IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (handler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            handler.updateProcessTaskStepUserAndWorker(processTaskId, processTaskStepId);
        }

        return id;
    }
    /**
     * @param processTaskStepTaskUserVo
     * @return void
     * @Time:2020年9月30日
     * @Description: 步骤主处理人校正操作 判断当前用户是否是代办人，如果不是就什么都不做，如果是，进行下面3个操作 1.往processtask_step_agent表中插入一条数据，记录该步骤的原主处理人和代办人
     * 2.将processtask_step_worker表中该步骤的主处理人uuid改为代办人(当前用户)
     * 3.将processtask_step_user表中该步骤的主处理人user_uuid改为代办人(当前用户)
     */
    private void stepMinorUserRegulate(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo) {
        Long stepTaskUserId = processTaskStepTaskUserVo.getId();
        Long stepTaskId = processTaskStepTaskUserVo.getProcessTaskStepTaskId();
        String currentUserUuid = UserContext.get().getUserUuid(true);
        /* 能进入这个方法，说明当前用户有权限处理当前步骤，可能是三类处理人：第一处理人(A)、代办人(B)、代办人的代办人(C) 。其中A授权给B，B授权给C **/
        ProcessTaskStepTaskUserAgentVo processTaskStepTaskUserAgentVo = processTaskStepTaskMapper.getProcessTaskStepTaskUserAgentByStepTaskUserId(stepTaskUserId);
        if (processTaskStepTaskUserAgentVo == null) {
            // 代办人还没接管，当前用户可能是A和B
            if (!Objects.equals(processTaskStepTaskUserVo.getUserUuid(), currentUserUuid)) {
                // 当用户是B
                ProcessTaskStepTaskUserAgentVo processTaskStepTaskUserAgent = new ProcessTaskStepTaskUserAgentVo();
                processTaskStepTaskUserAgent.setProcessTaskStepTaskUserId(stepTaskUserId);
                processTaskStepTaskUserAgent.setProcessTaskStepTaskId(stepTaskId);
                processTaskStepTaskUserAgent.setUserUuid(processTaskStepTaskUserVo.getUserUuid());
                processTaskStepTaskUserAgent.setAgentUuid(currentUserUuid);
                processTaskStepTaskMapper.insertProcessTaskStepTaskUserAgent(processTaskStepTaskUserAgent);
            }
        } else {
            // 代办人接管过了，当前用户可能是A、B、C
            if (currentUserUuid.equals(processTaskStepTaskUserAgentVo.getUserUuid())) {
                // 当前用户是A
                processTaskStepTaskMapper.deleteProcessTaskStepTaskUserAgentByStepTaskUserId(stepTaskUserId);
            } else if (currentUserUuid.equals(processTaskStepTaskUserAgentVo.getAgentUuid())) {
                // 当前用户是B
//                currentProcessTaskStepVo.setOriginalUser(processTaskStepAgentVo.getUserUuid());
            } else {
                // 当前用户是C
                ProcessTaskStepTaskUserAgentVo processTaskStepTaskUserAgent = new ProcessTaskStepTaskUserAgentVo();
                processTaskStepTaskUserAgent.setProcessTaskStepTaskUserId(stepTaskUserId);
                processTaskStepTaskUserAgent.setProcessTaskStepTaskId(stepTaskId);
                processTaskStepTaskUserAgent.setUserUuid(processTaskStepTaskUserAgentVo.getAgentUuid());
                processTaskStepTaskUserAgent.setAgentUuid(currentUserUuid);
                processTaskStepTaskMapper.insertProcessTaskStepTaskUserAgent(processTaskStepTaskUserAgent);
//                currentProcessTaskStepVo.setOriginalUser(processTaskStepAgentVo.getAgentUuid());
            }
        }
    }
    /**
     * 解析&校验 任务配置
     *
     * @param stepConfigHash 步骤配置hash
     * @return 任务配置
     */
    @Override
    public JSONObject getTaskConfig(String stepConfigHash) {
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

        JSONArray rangeArray = taskConfig.getJSONArray("rangeList");
        for (TaskConfigVo taskConfigVo : taskConfigList) {
            if (CollectionUtils.isNotEmpty(rangeArray)) {
                taskConfigVo.setRangeList(rangeArray.toJavaList(String.class));
            }
        }
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
            List<ProcessTaskStepTaskUserAgentVo> stepTaskUserAgentList = processTaskStepTaskMapper.getProcessTaskStepTaskUserAgentListByStepTaskUserIdList(stepTaskUserIdList);
            Map<Long, String> stepTaskUserAgentMap = stepTaskUserAgentList.stream().collect(Collectors.toMap(e -> e.getProcessTaskStepTaskUserId(), e -> e.getUserUuid()));
            Map<String, UserVo> userMap = new HashMap<>();
            List<String> userUuidList = new ArrayList<>(stepTaskUserAgentMap.values());
            if (CollectionUtils.isNotEmpty(userUuidList)) {
                List<UserVo> userList = userMapper.getUserByUserUuidList(userUuidList);
                userMap = userList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
            }
            List<ProcessTaskStepTaskUserContentVo> stepTaskUserContentList = processTaskStepTaskMapper.getStepTaskUserContentByStepTaskUserIdList(stepTaskUserIdList);
            Map<Long, ProcessTaskStepTaskUserContentVo> stepTaskUserContentMap = new HashMap<>();
            for (ProcessTaskStepTaskUserContentVo stepTaskUserContentVo : stepTaskUserContentList) {
                if (stepTaskUserContentMap.containsKey(stepTaskUserContentVo.getProcessTaskStepTaskUserId())) {
                    continue;
                }
                stepTaskUserContentMap.put(stepTaskUserContentVo.getProcessTaskStepTaskUserId(), stepTaskUserContentVo);
            }
            for (ProcessTaskStepTaskUserVo stepTaskUserVo : stepTaskUserList) {
                if (stepTaskUserVo.getEndTime() == null && stepTaskUserVo.getIsDelete() == 1) {
                    continue;
                }
                int isReplyable = 0;
                try {
                    isReplyable = checkIsReplyable(processTaskVo, processTaskStepVo, stepTaskUserVo.getUserUuid(), stepTaskUserVo.getId());
                } catch (ProcessTaskPermissionDeniedException processTaskPermissionDeniedException) {
                    isReplyable = 0;
                }
                stepTaskUserVo.setIsReplyable(isReplyable);
                String originalUserUuid = stepTaskUserAgentMap.get(stepTaskUserVo.getId());
                if (StringUtils.isNotBlank(originalUserUuid)) {
                    stepTaskUserVo.setOriginalUserUuid(originalUserUuid);
                    UserVo userVo = userMap.get(originalUserUuid);
                    if (userVo != null) {
                        UserVo originalUserVo = new UserVo();
                        BeanUtils.copyProperties(userVo, originalUserVo);
                        stepTaskUserVo.setOriginalUserVo(originalUserVo);
                    }
                }
                ProcessTaskStepTaskUserContentVo stepTaskUserContentVo = stepTaskUserContentMap.get(stepTaskUserVo.getId());
                if (stepTaskUserContentVo != null) {
                    stepTaskUserVo.setContent(stepTaskUserContentVo.getContent());
                    stepTaskUserVo.setProcessTaskStepTaskUserContentId(stepTaskUserContentVo.getId());
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
        for (TaskConfigVo taskConfigVo : taskConfigList) {
            List<ProcessTaskStepTaskVo> stepTaskList = stepTaskMap.get(taskConfigVo.getId());
            taskConfigVo.setProcessTaskStepTaskList(stepTaskList);
        }
        return taskConfigList;
    }

    /**
     * 判断当前用户是否可以处理任务
     * @param processTaskVo 工单信息
     * @param processTaskStepVo 步骤信息
     * @param stepTaskUserUuid 任务处理人uuid
     * @param stepTaskUserUuid 任务处理人id
     * @return
     */
    private int checkIsReplyable(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo, String stepTaskUserUuid, Long stepTaskUserId) throws ProcessTaskPermissionDeniedException {
        Long id = processTaskStepVo.getId();
        ProcessTaskOperationType operationType = ProcessTaskOperationType.TASK_COMPLETE;
        //1.判断工单是否被隐藏，如果isShow=0，则提示“工单已隐藏”；
        if (processTaskVo.getIsShow() == 0) {
            throw new ProcessTaskHiddenException();
        }
        //2.判断工单状态是否是“未提交”，如果是，则提示“工单未提交”；
        //3.判断工单状态是否是“已完成”，如果是，则提示“工单已完成”；
        //4.判断工单状态是否是“已取消”，如果是，则提示“工单已取消”；
        //5.判断工单状态是否是“异常”，如果是，则提示“工单异常”；
        //6.判断工单状态是否是“已挂起”，如果是，则提示“工单已挂起”；
        //7.判断工单状态是否是“已评分”，如果是，则提示“工单已评分”；
        ProcessTaskPermissionDeniedException exception = processTaskService.checkProcessTaskStatus(processTaskVo.getStatus(),
                ProcessTaskStatus.DRAFT,
                ProcessTaskStatus.SUCCEED,
                ProcessTaskStatus.ABORTED,
                ProcessTaskStatus.FAILED,
                ProcessTaskStatus.HANG,
                ProcessTaskStatus.SCORED);
        if (exception != null) {
            throw exception;
        }
        //8.判断步骤是否未激活，如果isActive=0，则提示“步骤未激活”；
        if (processTaskStepVo.getIsActive() == 0) {
            throw new ProcessTaskStepNotActiveException();
        }
        //9.判断步骤状态是否是“已完成”，如果是，则提示“步骤已完成”；
        //10.判断步骤状态是否是“异常”，如果是，则提示“步骤异常”；
        //11.判断步骤状态是否是“已挂起”，如果是，则提示“步骤已挂起”；
        //12.判断步骤状态是否是“待处理”，如果是，则提示“步骤未开始”；
        exception = processTaskService.checkProcessTaskStepStatus(processTaskStepVo.getStatus(), ProcessTaskStatus.SUCCEED,
                ProcessTaskStatus.FAILED,
                ProcessTaskStatus.HANG,
                ProcessTaskStatus.PENDING);
        if (exception != null) {
            throw exception;
        }

        //系统用户默认拥有权限
        if (SystemUser.SYSTEM.getUserUuid().equals(UserContext.get().getUserUuid(true))) {
            return 1;
        }

        if (Objects.equals(stepTaskUserUuid, UserContext.get().getUserUuid(true))) {
            return 1;
        }
        ProcessTaskStepTaskUserAgentVo processTaskStepTaskUserAgentVo = processTaskStepTaskMapper.getProcessTaskStepTaskUserAgentByStepTaskUserId(stepTaskUserId);
        if (processTaskStepTaskUserAgentVo != null) {
            if (Objects.equals(processTaskStepTaskUserAgentVo.getUserUuid(), UserContext.get().getUserUuid(true))) {
                return 1;
            }
        }
        List<String> fromUuidList = processTaskAgentServiceImpl.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
        if (fromUuidList.contains(stepTaskUserUuid)) {
            return 1;
        }
        throw new ProcessTaskStepNotMinorUserException();
    }
}
