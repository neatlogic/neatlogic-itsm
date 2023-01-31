package neatlogic.module.process.api.processtask;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepSubtaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.core.ProcessTaskRuntimeException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskStepSubtaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

//@Service
@Deprecated
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
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
//            new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.SUBTASK_CREATE)
//                .build().checkAndNoPermissionThrowException();
        } catch (ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }
        ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
        processTaskStepSubtaskVo.setProcessTaskId(processTaskId);
        processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepSubtaskVo.setOwnerVo(new UserVo(UserContext.get().getUserUuid(true)));
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
