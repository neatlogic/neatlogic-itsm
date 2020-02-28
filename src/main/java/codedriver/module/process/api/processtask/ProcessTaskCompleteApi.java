package codedriver.module.process.api.processtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Service
@AuthAction(name = "PROCESS_MODIFY")
public class ProcessTaskCompleteApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/complete";
	}

	@Override
	public String getName() {
		return "工单完成接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId",
					type = ApiParamType.LONG,
					desc = "工单Id",
					isRequired = true),
			@Param(name = "processTaskStepId",
			type = ApiParamType.LONG,
			desc = "工单步骤Id")
	})
	@Output({})
	@Description(desc = "工单完成接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {

		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		ProcessTaskStepVo processTaskStepVo = null;
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(processTaskStepId != null) {
			processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			if (processTaskStepVo != null) {
				throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
			}
		}else {
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
			if(processTaskStepList.size() != 1) {
				throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
			}
			processTaskStepVo = processTaskStepList.get(0);
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		processTaskStepVo.setParamObj(jsonObj);
		handler.complete(processTaskStepVo);
		
		return null;
	}

}
