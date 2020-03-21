package codedriver.module.process.api.processtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskStartProcessApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/startprocess";
	}

	@Override
	public String getName() {
		return "工单上报提交接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true)
	})
	@Description(desc = "工单上报提交接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.STARTPROCESS)) {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.STARTPROCESS.getText());
		}
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		
		ProcessTaskStepVo startTaskStep = new ProcessTaskStepVo();
		startTaskStep.setHandler(ProcessStepHandler.START.getHandler());
		startTaskStep.setId(processTaskStepList.get(0).getId());
		startTaskStep.setProcessTaskId(processTaskId);
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(ProcessStepHandler.START.getHandler());
		if(handler != null) {
			handler.startProcess(startTaskStep);
		}else {
			throw new ProcessStepHandlerNotFoundException(ProcessStepHandler.START.getHandler());
		}
		return null;
	}

}
