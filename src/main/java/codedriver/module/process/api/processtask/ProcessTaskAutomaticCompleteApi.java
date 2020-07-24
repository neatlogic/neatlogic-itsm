package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskAutomaticNotAllowNextStepsException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessTaskAutomaticCompleteApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/automatic/complete";
	}

	@Override
	public String getName() {
		return "流转自动化处理步骤";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "当前步骤Id"),
		@Param(name = "action", type = ApiParamType.ENUM, rule = "back,complete", isRequired = true, desc = "操作类型，complete：流转,back：回退"),
	})
	@Output({
		@Param(name = "Status", type = ApiParamType.STRING, desc = "操作成功"),
		@Param(name = "Message", type = ApiParamType.STRING, desc = "异常信息"),
	})
	@Description(desc = "流转自动化处理步骤接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		String action = jsonObj.getString("action");
		String flowDirection = ProcessFlowDirection.FORWARD.getValue();
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if(processTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		jsonObj.put("processTaskId", processTaskStepVo.getProcessTaskId());
		if(action.equals(ProcessTaskStepAction.BACK.getValue())) {
			flowDirection = ProcessFlowDirection.BACKWARD.getValue();
		}
		/** 不允许多个后续步骤 **/
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId,flowDirection);
		if(CollectionUtils.isEmpty(processTaskStepList)||(CollectionUtils.isNotEmpty(processTaskStepList) && processTaskStepList.size()>1)) {
			throw new ProcessTaskAutomaticNotAllowNextStepsException();
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler != null) {
			jsonObj.put("nextStepId", processTaskStepList.get(0).getId());
			processTaskStepVo.setParamObj(jsonObj);
			handler.complete(processTaskStepVo);
		}else {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		return null;
	}

}
