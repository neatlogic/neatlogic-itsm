package codedriver.module.process.api.process;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dto.TimeoutPolicyVo;
import codedriver.framework.process.timeoutpolicy.handler.TimeoutPolicyHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class TimeoutPolicyListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/timeout/policy/list";
	}

	@Override
	public String getName() {
		return "超时策略列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Output({@Param(name="Return", explode = TimeoutPolicyVo[].class, desc = "超时策略列表")})
	@Description(desc = "超时策略列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return TimeoutPolicyHandlerFactory.getAllActiveTimeoutPolicy();
	}

}
