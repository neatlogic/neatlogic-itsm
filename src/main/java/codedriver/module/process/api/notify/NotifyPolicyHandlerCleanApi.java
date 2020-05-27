package codedriver.module.process.api.notify;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyHandlerCleanApi  extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/notify/policy/handler/clean";
	}

	@Override
	public String getName() {
		return "通知策略清空触发动作配置项接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "trigger", type = ApiParamType.ENUM, isRequired = true, desc = "通知动作类型",
				rule = "active,assign,start,transfer,urge,succeed,back,retreat,hang,abort,recover,failed,createsubtask,editsubtask,abortsubtask,redosubtask,completesubtask")
	})
	@Output({})
	@Description(desc = "通知策略清空触发动作配置项接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		return null;
	}

}
