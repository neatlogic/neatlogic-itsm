package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

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
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.ProcessTaskStepWorkerAction;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.service.ProcessTaskService;

@Service
public class ProcessTaskTransferApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/transfer";
	}

	@Override
	public String getName() {
		return "工单转交接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
			@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤Id"),
			@Param(name = "userId", type = ApiParamType.STRING, isRequired = true, desc = "新处理人userId"),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "原因")
	})
	@Output({})
	@Description(desc = "工单转交接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.TRANSFER)) {
			throw new ProcessTaskRuntimeException("您没有权限执行此操作");
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		processTaskStepVo.setParamObj(jsonObj);
		String userId = jsonObj.getString("userId");
		List<ProcessTaskStepWorkerVo> workerList =  new ArrayList<ProcessTaskStepWorkerVo>();
		workerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, userId, ProcessTaskStepWorkerAction.HANDLE.getValue()));
		handler.transfer(processTaskStepVo,workerList);		
		return null;
	}

}
