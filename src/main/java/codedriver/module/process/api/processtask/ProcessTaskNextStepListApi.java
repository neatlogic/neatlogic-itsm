package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
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
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskNextStepListApi extends ApiComponentBase{

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/nextstep/list";
	}

	@Override
	public String getName() {
		return "下一可流转步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "当前步骤Id"),
		@Param(name = "action", type = ApiParamType.ENUM, rule = "complete,back", desc = "操作类型"),
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "下一可流转步骤列表")
	})
	@Description(desc = "下一可流转步骤列表接口")
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
		if(handler == null) {
			throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
		}
		ProcessTaskStepAction processTaskStepAction = ProcessTaskStepAction.COMPLETE;
		String action = jsonObj.getString("action");
		if(ProcessTaskStepAction.BACK.getValue().equals(action)) {
			processTaskStepAction = ProcessTaskStepAction.BACK;
		}else {
			action = ProcessTaskStepAction.COMPLETE.getValue();
		}
		handler.verifyActionAuthoriy(processTaskId, processTaskStepId, processTaskStepAction);
		List<ProcessTaskStepVo> resultList = new ArrayList<>();
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getToProcessTaskStepByFromId(processTaskStepId);
		for(ProcessTaskStepVo processTaskStep : processTaskStepList) {
			if(processTaskStep.getIsActive() != null) {
				if(ProcessTaskStepAction.COMPLETE.getValue().equals(action) && ProcessFlowDirection.FORWARD.getValue().equals(processTaskStep.getFlowDirection())) {
					if(StringUtils.isNotBlank(processTaskStep.getAliasName())) {
						processTaskStep.setName(processTaskStep.getAliasName());
						processTaskStep.setFlowDirection("");
					}else {
						processTaskStep.setFlowDirection(ProcessFlowDirection.FORWARD.getText());
					}
					resultList.add(processTaskStep);
				}else if(ProcessTaskStepAction.BACK.getValue().equals(action) && ProcessFlowDirection.BACKWARD.getValue().equals(processTaskStep.getFlowDirection()) && processTaskStep.getIsActive().intValue() != 0){
					if(StringUtils.isNotBlank(processTaskStep.getAliasName())) {
						processTaskStep.setName(processTaskStep.getAliasName());
						processTaskStep.setFlowDirection("");
					}else {
						processTaskStep.setFlowDirection(ProcessFlowDirection.BACKWARD.getText());
					}
					resultList.add(processTaskStep);
				}
			}
		}
		return resultList;
	}

}
