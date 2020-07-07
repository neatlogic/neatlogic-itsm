package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessTaskTransferApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
			@Param(name = "workerList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "新可处理对象列表，[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "原因")
	})
	@Output({})
	@Description(desc = "工单转交接口")
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
			processTaskStepVo.setParamObj(jsonObj);		
			List<ProcessTaskStepWorkerVo> processTaskStepWorkerList =  new ArrayList<ProcessTaskStepWorkerVo>();
			List<String> workerList = JSON.parseArray(jsonObj.getString("workerList"), String.class);
			for(String worker : workerList) {	
				String[] split = worker.split("#");
				if(GroupSearch.getValue(split[0]) != null) {
					processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, split[0], split[1], ProcessUserType.MAJOR.getValue()));
				}
			}
			handler.transfer(processTaskStepVo,processTaskStepWorkerList);
		}else {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		return null;
	}

}
