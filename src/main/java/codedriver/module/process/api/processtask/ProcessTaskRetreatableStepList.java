package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskRetreatableStepList extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/retreatablestep/list";
	}

	@Override
	public String getName() {
		return "当前用户可撤回的步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true)
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")
	})
	@Description(desc = "当前用户可撤回的步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
//		return ProcessStepUtilHandlerFactory.getHandler().getRetractableStepList(processTaskId);
		return processTaskService.getRetractableStepListByProcessTaskId(processTaskId);
	}

}
