package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskStepSubtaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepSubtaskVo;
import neatlogic.framework.process.exception.core.ProcessTaskRuntimeException;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepSubtaskNotFoundException;
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
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepSubtaskEditApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;
	
	@Autowired
	private UserMapper userMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;

	@Override
	public String getToken() {
		return "processtask/step/subtask/edit";
	}

	@Override
	public String getName() {
		return "子任务编辑接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskStepSubtaskId", type = ApiParamType.LONG, isRequired = true, desc = "子任务id"),
		@Param(name = "workerList", type = ApiParamType.STRING, isRequired = true, desc = "子任务处理人userUuid,单选,格式user#userUuid"),
		@Param(name = "targetTime", type = ApiParamType.LONG, desc = "期望完成时间"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "描述")
	})
	@Output({})
	@Description(desc = "子任务编辑接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskStepSubtaskId = jsonObj.getLong("processTaskStepSubtaskId");
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskById(processTaskStepSubtaskId);
		if(processTaskStepSubtaskVo == null) {
			throw new ProcessTaskStepSubtaskNotFoundException(processTaskStepSubtaskId.toString());
		}
		if(processTaskStepSubtaskVo.getIsEditable().intValue() == 1) {
			String workerList = jsonObj.getString("workerList");
			if(workerList.startsWith(GroupSearch.USER.getValuePlugin())) {
				String[] split = workerList.split("#");
				UserVo userVo = userMapper.getUserBaseInfoByUuid(split[1]);
				if(userVo == null) {
					throw new UserNotFoundException(split[1]);
				}
			}else {
				throw new ProcessTaskRuntimeException("子任务处理人不能为空");
			}
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepSubtaskVo.setParamObj(jsonObj);
			processTaskStepSubtaskService.editSubtask(processTaskStepSubtaskVo);
		}else {
            throw new PermissionDeniedException();
		}
		return null;
	}

}
