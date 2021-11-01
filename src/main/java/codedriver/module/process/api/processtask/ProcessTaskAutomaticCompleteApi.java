package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskAutomaticNotAllowNextStepsException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepMustBeAutomaticException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAutomaticCompleteApi extends PrivateApiComponentBase {

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
		if(ProcessStepHandlerType.AUTOMATIC.getHandler().equals(processTaskStepVo.getHandler())) {
		    throw new ProcessTaskStepMustBeAutomaticException();
		}
		jsonObj.put("processTaskId", processTaskStepVo.getProcessTaskId());
		if(action.equals(ProcessTaskOperationType.STEP_BACK.getValue())) {
			flowDirection = ProcessFlowDirection.BACKWARD.getValue();
		}
		/** 不允许多个后续步骤 **/
		List<Long> processTaskStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(processTaskStepId, flowDirection);
		if(CollectionUtils.isEmpty(processTaskStepIdList)||(CollectionUtils.isNotEmpty(processTaskStepIdList) && processTaskStepIdList.size()>1)) {
			throw new ProcessTaskAutomaticNotAllowNextStepsException();
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler != null) {
			jsonObj.put("nextStepId", processTaskStepIdList.get(0));
			processTaskStepVo.getParamObj().putAll(jsonObj);
			handler.complete(processTaskStepVo);
		}else {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		return null;
	}
	
}
