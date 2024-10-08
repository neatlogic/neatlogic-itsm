package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskAutomaticNotAllowNextStepsException;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepMustBeAutomaticException;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAutomaticCompleteApi extends PrivateApiComponentBase {

	@Resource
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
		@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
	})
	@Output({
		@Param(name = "Status", type = ApiParamType.STRING, desc = "状态"),
		@Param(name = "Message", type = ApiParamType.STRING, desc = "异常信息"),
	})
	@Description(desc = "流转自动化处理步骤")
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
		/* 不允许多个后续步骤 **/
		List<Long> processTaskStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(processTaskStepId, flowDirection);
		if(CollectionUtils.isEmpty(processTaskStepIdList)||(CollectionUtils.isNotEmpty(processTaskStepIdList) && processTaskStepIdList.size()>1)) {
			throw new ProcessTaskAutomaticNotAllowNextStepsException();
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler != null) {
			jsonObj.put("nextStepId", processTaskStepIdList.get(0));
			processTaskStepVo.getParamObj().putAll(jsonObj);
			handler.autoComplete(processTaskStepVo);
		}else {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		return null;
	}
	
}
