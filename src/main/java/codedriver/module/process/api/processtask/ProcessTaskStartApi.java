package codedriver.module.process.api.processtask;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessTaskStartApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/start";
	}

	@Override
	public String getName() {
		return "工单步骤开始接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="processTaskId", type = ApiParamType.LONG, isRequired = true, desc="工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤Id"),
		@Param(name = "action", type = ApiParamType.ENUM, rule = "accept,start", desc = "操作类型")
	})
	@Description(desc = "工单步骤开始接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if(processTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
			throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
		}
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler != null) {
			String action = jsonObj.getString("action");
			if(ProcessTaskStepAction.ACCEPT.getValue().equals(action)) {
				handler.accept(processTaskStepVo);
			}
			handler.start(processTaskStepVo);
		}else {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		return null;
	}

}
