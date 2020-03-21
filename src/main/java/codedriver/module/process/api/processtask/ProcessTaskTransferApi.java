package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
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
			@Param(name = "workerList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "新可处理对象列表，[\"user#userId\",\"team#teamUuid\",\"role#roleName\"]"),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "原因")
	})
	@Output({})
	@Description(desc = "工单转交接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.TRANSFER)) {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.TRANSFER.getText());
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		processTaskStepVo.setParamObj(jsonObj);
		
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList =  new ArrayList<ProcessTaskStepWorkerVo>();
		List<String> workerList = JSON.parseArray(jsonObj.getString("workerList"), String.class);
		for(String worker : workerList) {	
			String[] split = worker.split("#");
			if(GroupSearch.USER.getValue().equals(split[0])) {
				processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, split[1], ProcessTaskStepWorkerAction.HANDLE.getValue()));
			}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
				processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, null, split[1], ProcessTaskStepWorkerAction.HANDLE.getValue()));
			}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
				processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, null, null, split[1], ProcessTaskStepWorkerAction.HANDLE.getValue()));
			}
		}

		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler != null) {
			handler.transfer(processTaskStepVo,processTaskStepWorkerList);
		}else {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		return null;
	}

}
