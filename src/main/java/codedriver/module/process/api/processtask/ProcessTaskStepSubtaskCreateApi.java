package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.operationauth.core.ProcessOperateManager;
import codedriver.module.process.service.ProcessTaskStepSubtaskService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskStepSubtaskCreateApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;

    @Override
    public String getToken() {
        return "processtask/step/subtask/create";
    }

    @Override
    public String getName() {
        return "子任务创建接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
        @Param(name = "workerList", type = ApiParamType.STRING, isRequired = true,
            desc = "子任务处理人userUuid,单选,格式user#userUuid"),
        @Param(name = "targetTime", type = ApiParamType.LONG, desc = "期望完成时间"),
        @Param(name = "content", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "描述")})
    @Output({@Param(name = "Return", type = ApiParamType.LONG, desc = "子任务id")})
    @Description(desc = "子任务创建接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
        }
        Long processTaskId = processTaskStepVo.getProcessTaskId();
        try {
            new ProcessOperateManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.CREATESUBTASK)
                .build().checkAndNoPermissionThrowException();
        } catch (ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }
        ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
        processTaskStepSubtaskVo.setProcessTaskId(processTaskId);
        processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepSubtaskVo.setOwner(UserContext.get().getUserUuid(true));
        String workerList = jsonObj.getString("workerList");
        jsonObj.remove("workerList");
        String[] split = workerList.split("#");
        if (GroupSearch.USER.getValue().equals(split[0])) {
            UserVo userVo = userMapper.getUserBaseInfoByUuid(split[1]);
            if (userVo != null) {
                processTaskStepSubtaskVo.setUserUuid(userVo.getUuid());
                processTaskStepSubtaskVo.setUserName(userVo.getUserName());
            } else {
                throw new UserNotFoundException(split[1]);
            }
        } else {
            throw new ProcessTaskRuntimeException("子任务处理人不能为空");
        }
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskId);
        processTaskStepSubtaskVo.setParamObj(jsonObj);
        processTaskStepSubtaskService.createSubtask(processTaskStepSubtaskVo);
        return null;
    }

}
