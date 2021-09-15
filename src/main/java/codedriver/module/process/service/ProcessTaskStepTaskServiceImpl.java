/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.task.TaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepUnRunningException;
import codedriver.framework.process.exception.processtask.task.*;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
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

    /**
     * 创建任务
     *
     * @param processTaskStepTaskVo 任务参数
     */
    @Override
    public void saveTask(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo, boolean isCreate) {
        //获取流程步骤配置中的 任务策略和人员范围
        JSONObject taskConfig = getTaskConfig(processTaskStepVo.getConfigHash());
        if(MapUtils.isEmpty(taskConfig)){
            throw new TaskConfigException(processTaskStepVo.getName());
        }
        List<Long> taskConfigIdList = taskConfig.getJSONArray("idList").toJavaList(Long.class);
        if (!taskConfigIdList.contains(processTaskStepTaskVo.getTaskConfigId())) {
            throw new ProcessTaskStepTaskConfigIllegalException(processTaskStepTaskVo.getTaskConfigId().toString());
        }
        processTaskStepTaskVo.setTaskConfigId(processTaskStepTaskVo.getTaskConfigId());
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskVo.getContent());
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
        processTaskStepTaskVo.setContentHash(processTaskContentVo.getHash());
        JSONArray rangeList = taskConfig.getJSONArray("rangeList");
        if (isCreate) {
            processTaskStepTaskVo.setStatus(ProcessTaskStatus.PENDING.getValue());
            processTaskStepTaskMapper.insertTask(processTaskStepTaskVo);
        } else {
            processTaskStepTaskMapper.updateTask(processTaskStepTaskVo);
            //用户删除标记
            processTaskStepTaskMapper.updateDeleteTaskUserByUserListAndId(processTaskStepTaskVo.getUserList(), processTaskStepTaskVo.getId(), 1);
            //去掉用户删除标记
            processTaskStepTaskMapper.updateDeleteTaskUserByUserListAndId(processTaskStepTaskVo.getUserList(), processTaskStepTaskVo.getId(), 0);
        }
        if (CollectionUtils.isNotEmpty(rangeList)) {
            //校验用户是否在配置范围内
            checkUserIsLegal(processTaskStepTaskVo.getUserList().stream().map(Object::toString).collect(Collectors.toList()), rangeList.stream().map(Object::toString).collect(Collectors.toList()));
            processTaskStepTaskVo.getUserList().forEach(t -> {
                processTaskStepTaskMapper.insertIgnoreTaskUser(new ProcessTaskStepTaskUserVo(processTaskStepTaskVo.getId(), t.toString(), ProcessTaskStatus.PENDING.getValue()));
            });

        }
        refreshWorker(processTaskStepVo, processTaskStepTaskVo);

    }

    /**
     * 刷新worker
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    private void refreshWorker(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo) {
        //删除该step的所有minor工单步骤worker
        processTaskMapper.deleteProcessTaskStepWorkerMinorByProcessTaskStepId(processTaskStepTaskVo.getId());
        //重新更新每个模块的minor worker
        for (IProcessStepHandler handler : ProcessStepHandlerFactory.getHandlerList()) {
            handler.insertMinorWorkerList(processTaskStepVo);
        }
        //重新插入pending任务用户到 工单步骤worker
        List<ProcessTaskStepTaskUserVo> taskUserVoList = processTaskStepTaskMapper.getPendingStepTaskUserListByTaskId(processTaskStepTaskVo.getId());
        for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
            if (taskUserVo.getIsDelete() != 1) {
                processTaskMapper.insertIgnoreProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), ProcessUserType.MINOR.getValue(), taskUserVo.getUserUuid(), GroupSearch.USER.getValue()));
            }
        }
    }

    /**
     * 完成任务
     *
     * @param processTaskStepTaskUserVo 任务用户参数
     */
    @Override
    public Long completeTask(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo) {
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskById(processTaskStepTaskUserVo.getProcessTaskStepTaskId());
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(processTaskStepTaskUserVo.getProcessTaskStepTaskId().toString());
        }
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(stepTaskVo.getProcessTaskStepId());
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(stepTaskVo.getProcessTaskStepId().toString());
        }
        if (!Objects.equals(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo.getStatus())) {
            throw new ProcessTaskStepUnRunningException();
        }
        processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
        ProcessTaskStepTaskUserVo taskUserVo = processTaskStepTaskMapper.getStepTaskUserByTaskIdAndTaskUserIdAndUserUuid(processTaskStepTaskUserVo.getProcessTaskStepTaskId(), processTaskStepTaskUserVo.getId(), processTaskStepTaskUserVo.getUserUuid());
        if (taskUserVo == null) {
            throw new ProcessTaskStepTaskUserNotFoundException();
        }
        //update 更新内容
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskUserVo.getContent());
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
        processTaskStepTaskUserVo.setContentHash(processTaskContentVo.getHash());
        //新增回复
        if (processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId() == null) {

            processTaskStepTaskMapper.updateTaskUserByTaskIdAndUserUuid(ProcessTaskStatus.SUCCEED.getValue(), processTaskStepTaskUserVo.getProcessTaskStepTaskId(), processTaskStepTaskUserVo.getUserUuid());
            ProcessTaskStepTaskUserContentVo contentVo = new ProcessTaskStepTaskUserContentVo(processTaskStepTaskUserVo);
            processTaskStepTaskMapper.insertTaskUserContent(contentVo);
            //刷新worker
            refreshWorker(processTaskStepVo, new ProcessTaskStepTaskVo(processTaskStepTaskUserVo.getProcessTaskStepTaskId()));
            return contentVo.getId();
        } else {
            ProcessTaskStepTaskUserContentVo userContentVo = processTaskStepTaskMapper.getStepTaskUserContentById(processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId());
            if (userContentVo == null) {
                throw new ProcessTaskStepTaskUserContentNotFoundException();
            }
            processTaskStepTaskMapper.updateTaskUserContent(processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId(), processTaskStepTaskUserVo.getContentHash(), UserContext.get().getUserUuid());
            return processTaskStepTaskUserVo.getProcessTaskStepTaskUserContentId();
        }


    }

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
        List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskByProcessTaskStepId(processTaskStepVo.getId());
        List<ProcessTaskStepTaskUserVo> stepTaskUserVoList;
        List<ProcessTaskStepTaskUserContentVo> stepTaskUserContentVoList;
        //默认存在所有task 的tab
        JSONObject taskConfig = getTaskConfig(processTaskStepVo.getConfigHash());
        if(MapUtils.isNotEmpty(taskConfig)) {
            List<Long> taskConfigIdList = taskConfig.getJSONArray("idList").toJavaList(Long.class);
            List<TaskConfigVo> taskConfigVoList = taskMapper.getTaskConfigByIdList(JSONArray.parseArray(JSON.toJSONString(taskConfigIdList)));
            for (TaskConfigVo taskConfigVo : taskConfigVoList) {
                stepTaskVoMap.put(taskConfigVo.getName(), new ArrayList<>());
            }

            if (CollectionUtils.isNotEmpty(stepTaskVoList)) {
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
                        stepTaskUserVoMap.get(stu.getProcessTaskStepTaskId()).add(stu);
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
                if (com.alibaba.nacos.common.utils.MapUtils.isNotEmpty(stepTaskConfigJson)) {
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
