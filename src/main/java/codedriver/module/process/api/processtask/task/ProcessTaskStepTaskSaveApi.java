/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.task;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepUnRunningException;
import codedriver.framework.process.exception.processtask.task.ProcessTaskStepTaskNotFoundException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskStepTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author lvzk
 * @since 2021/8/31 11:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskSaveApi extends PrivateApiComponentBase {
    @Resource
    ProcessTaskMapper processTaskMapper;
    @Resource
    ProcessTaskStepTaskMapper processTaskStepTaskMapper;
    @Resource
    ProcessTaskStepTaskService processTaskStepTaskService;

    @Override
    public String getToken() {
        return "processtask/step/task/save";
    }

    @Override
    public String getName() {
        return "保存任务";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskStepTaskId", type = ApiParamType.LONG, desc = "任务id，如果不为空则是编辑，为空则新增"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
            @Param(name = "userList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "任务处理人userUuid,格式user#userUuid"),
            @Param(name = "taskConfigId", type = ApiParamType.LONG, isRequired = true, desc = "任务策略id"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "描述")})
    @Output({@Param(name = "Return", type = ApiParamType.LONG, desc = "任务id")})
    @Description(desc = "保存任务接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskStepTaskId = jsonObj.getLong("processTaskStepTaskId");
        ProcessTaskStepTaskVo processTaskStepTaskVo = JSONObject.toJavaObject(jsonObj, ProcessTaskStepTaskVo.class);
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
        }
        if (!Objects.equals(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo.getStatus())) {
            throw new ProcessTaskStepUnRunningException();
        }
        Long processTaskId = processTaskStepVo.getProcessTaskId();
        //校验执行权限
        try {
            new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.TASK_CREATE).build().checkAndNoPermissionThrowException();
        } catch (ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }
        if (CollectionUtils.isEmpty(processTaskStepTaskVo.getUserList())) {
            throw new ParamIrregularException("userList");
        }
        processTaskStepTaskVo.setProcessTaskId(processTaskId);
        processTaskStepTaskVo.setOwnerVo(new UserVo(UserContext.get().getUserUuid(true)));
        processTaskStepTaskVo.getParamObj().put("stepConfigHash", processTaskStepVo.getConfigHash());
        processTaskStepTaskVo.getParamObj().put("stepName", processTaskStepVo.getName());
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskId);
        boolean isCreate = true;
        if (processTaskStepTaskId != null) {
            ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskById(processTaskStepTaskId);
            if(stepTaskVo == null){
                throw new ProcessTaskStepTaskNotFoundException(processTaskStepTaskId.toString());
            }
            isCreate = false;
            processTaskStepTaskVo.setId(processTaskStepTaskId);
        }
        processTaskStepTaskService.saveTask(processTaskStepVo,processTaskStepTaskVo, isCreate);
        //TODO 活动&通知
        return processTaskStepTaskVo.getId();
    }
}
