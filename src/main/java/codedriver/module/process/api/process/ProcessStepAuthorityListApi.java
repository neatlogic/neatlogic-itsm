package codedriver.module.process.api.process;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessStepAuthority;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessStepAuthorityListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/step/auth/list";
	}

	@Override
	public String getName() {
		return "流程步骤权限列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Output({
		@Param(name="Return", explode=ValueTextVo[].class, desc = "流程步骤权限列表")
	})
	@Description(desc = "流程步骤权限列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return ProcessStepAuthority.getProcessStepAuthList();
	}

}
