package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepSubtaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskStepSubtaskNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskStepSubtaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
//@Service
@Deprecated
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepSubtaskCompleteApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;

	@Override
	public String getToken() {
		return "processtask/step/subtask/complete";
	}

	@Override
	public String getName() {
		return "子任务完成接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskStepSubtaskId", type = ApiParamType.LONG, isRequired = true, desc = "子任务id"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "描述")
	})
	@Output({})
	@Description(desc = "子任务完成接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskStepSubtaskId = jsonObj.getLong("processTaskStepSubtaskId");
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskById(processTaskStepSubtaskId);
		if(processTaskStepSubtaskVo == null) {
			throw new ProcessTaskStepSubtaskNotFoundException(processTaskStepSubtaskId.toString());
		}
		if(processTaskStepSubtaskVo.getIsCompletable().intValue() == 1) {
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepSubtaskVo.setParamObj(jsonObj);
			processTaskStepSubtaskService.completeSubtask(processTaskStepSubtaskVo);
		}else {
            throw new PermissionDeniedException();
		}
		return null;
	}

}
