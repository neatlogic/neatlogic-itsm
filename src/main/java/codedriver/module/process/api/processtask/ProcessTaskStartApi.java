package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessTaskStartApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "processtask/start";
	}

	@Override
	public String getName() {
		return "工单上报接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="processTaskId", type = ApiParamType.LONG, isRequired = true, desc="工单id")
	})
	@Description(desc = "工单上报接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}

}
