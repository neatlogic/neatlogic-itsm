package neatlogic.module.process.api.process;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.constvalue.automatic.CallbackType;
import neatlogic.framework.process.constvalue.automatic.FailPolicy;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessAutomaticInitApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "process/automatic/init";
	}

	@Override
	public String getName() {
		return "获取automatic初始化数据接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({
		@Param(name = "failPolicyList[].value", type = ApiParamType.STRING, isRequired = true, desc = "失败策略"),
		@Param(name = "failPolicyList[].text", type = ApiParamType.STRING, isRequired = true, desc = "失败策略名"),
		@Param(name = "callbackList[].value", type = ApiParamType.STRING, isRequired = true, desc = "回调类型"),
		@Param(name = "callbackList[].text", type = ApiParamType.STRING, isRequired = true, desc = "回调类型名"),
	})
	@Description(desc="获取automatic初始化数据接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultJson = new JSONObject();
		resultJson.put("failPolicyList", FailPolicy.getJSONArray());
		resultJson.put("callbackList", CallbackType.getJSONArray());
		return resultJson;
	}

}
