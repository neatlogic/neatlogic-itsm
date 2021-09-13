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
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepUnRunningException;
import codedriver.framework.process.exception.processtask.task.*;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.service.UserService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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

    /**
     * 创建任务
     *
     * @param processTaskStepTaskVo 任务参数
     */
    @Override
    public void saveTask(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo, boolean isCreate) {
        //获取流程步骤配置中的 任务策略和人员范围
        JSONObject taskConfig = getTaskConfig(processTaskStepTaskVo);
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
            processTaskStepTaskMapper.updateDeleteTaskUserByUserListAndId(processTaskStepTaskVo.getUserList(), processTaskStepTaskVo.getId(),1);
            //去掉用户删除标记
            processTaskStepTaskMapper.updateDeleteTaskUserByUserListAndId(processTaskStepTaskVo.getUserList(), processTaskStepTaskVo.getId(),0);
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
        List<ProcessTaskStepTaskUserVo> taskUserVoList = processTaskStepTaskMapper.getStepTaskUserListByTaskId(processTaskStepTaskVo.getId());
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
    public void completeTask(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo) {
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskById(processTaskStepTaskUserVo.getProcesstaskStepTaskId());
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(processTaskStepTaskUserVo.getProcesstaskStepTaskId().toString());
        }
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(stepTaskVo.getProcessTaskStepId());
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(stepTaskVo.getProcessTaskStepId().toString());
        }
        if (Objects.equals(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo.getStatus())) {
            throw new ProcessTaskStepUnRunningException();
        }
        processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
        ProcessTaskStepTaskUserVo taskUserVo = processTaskStepTaskMapper.getStepTaskUserByTaskIdAndUserUuid(processTaskStepTaskUserVo.getProcesstaskStepTaskId(), processTaskStepTaskUserVo.getUserUuid());
        if (taskUserVo == null) {
            throw new ProcessTaskStepTaskUserNotFoundException();
        }
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskUserVo.getContent());
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
        processTaskStepTaskMapper.updateTaskUserByTaskIdAndUserUuid(ProcessTaskStatus.SUCCEED.getValue(), processTaskStepTaskUserVo.getProcesstaskStepTaskId(), processTaskStepTaskUserVo.getUserUuid());
        processTaskStepTaskUserVo.setContentHash(processTaskContentVo.getHash());
        processTaskStepTaskMapper.insertTaskUserContent(processTaskStepTaskUserVo);
    }

    /**
     * 解析&校验 任务配置
     *
     * @param processTaskStepTaskVo 任务入参
     * @return 任务配置
     */
    private JSONObject getTaskConfig(ProcessTaskStepTaskVo processTaskStepTaskVo) {
        String stepConfigHash = processTaskStepTaskVo.getParamObj().getString("stepConfigHash");
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
        throw new TaskConfigException(processTaskStepTaskVo.getParamObj().getString("stepName"));
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
}
