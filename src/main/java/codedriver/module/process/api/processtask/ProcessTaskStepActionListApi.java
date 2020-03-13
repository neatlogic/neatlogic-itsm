package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskStepActionListApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/step/action/List";
	}

	@Override
	public String getName() {
		return "工单步骤当前用户操作权限列表获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤id")
	})
	@Output({
		@Param(name = "Return", explode = ValueTextVo[].class, desc = "当前用户操作权限列表")
	})
	@Description(desc = "工单步骤当前用户操作权限列表获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		List<String> actionList = processTaskService.getProcessTaskStepActionList(processTaskId, processTaskStepId);
		if(!actionList.contains(ProcessTaskStepAction.VIEW.getValue())) {
			throw new ProcessTaskRuntimeException("您没有权限执行此操作");
		}

		List<ValueTextVo> resultList = new ArrayList<>();
		for(ProcessTaskStepAction processTaskStepAction : ProcessTaskStepAction.values()) {
			if(processTaskStepAction == ProcessTaskStepAction.VIEW || processTaskStepAction == ProcessTaskStepAction.ACTIVE || processTaskStepAction == ProcessTaskStepAction.BACK) {
				continue;
			}
			if(!actionList.contains(processTaskStepAction.getValue())) {
				continue;
			}
			ValueTextVo valueText = new ValueTextVo();
			valueText.setValue(processTaskStepAction.getValue());
			valueText.setText(processTaskStepAction.getText());
			resultList.add(valueText);
		}
		return resultList;
	}

}
