package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessTaskStepUserAuthorityVo;
@Service
public class ProcessTaskStepUserAuthorityGetApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "processtask/step/userauthority/get";
	}

	@Override
	public String getName() {
		return "工单步骤当前用户权限获取接口";
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
		@Param(explode = ProcessTaskStepUserAuthorityVo.class, desc = "权限数据")
	})
	@Description(desc = "工单步骤当前用户权限获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
