package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskRetreatApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/retreat";
	}

	@Override
	public String getName() {
		return "上一步发起的撤回动作接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "描述"),
	})
	@Description(desc = "上一步发起的撤回动作接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.RETREAT)) {
			throw new ProcessTaskRuntimeException("您没有权限执行此操作");
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		processTaskStepVo.setParamObj(jsonObj);
		handler.retreat(processTaskStepVo);
		return null;
	}

}
