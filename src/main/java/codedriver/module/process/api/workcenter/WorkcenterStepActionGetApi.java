package codedriver.module.process.api.workcenter;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.NewWorkcenterService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterStepActionGetApi extends PrivateApiComponentBase {

	@Autowired
	NewWorkcenterService newWorkcenterService;
	
	@Override
	public String getToken() {
		return "workcenter/processtask/get";
	}

	@Override
	public String getName() {
		return "获取工单中心工单";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "taskId", desc="工单ID" ,type = ApiParamType.STRING, isRequired = true),
	})
	@Output({
	})
	@Description(desc = "获取工单中心工单")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long taskId = jsonObj.getLong("taskId");

		JSONObject result = newWorkcenterService.doSearch(taskId);
		return result;
	}
}
