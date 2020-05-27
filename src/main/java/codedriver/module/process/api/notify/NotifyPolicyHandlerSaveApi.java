package codedriver.module.process.api.notify;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dto.NotifyPolicyVo;
import codedriver.framework.process.exception.notify.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyHandlerSaveApi  extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/notify/policy/handler/save";
	}

	@Override
	public String getName() {
		return "通知策略保存触发动作配置项接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "trigger", type = ApiParamType.ENUM, isRequired = true, desc = "通知动作类型",
				rule = "active,assign,start,transfer,urge,succeed,back,retreat,hang,abort,recover,failed,createsubtask,editsubtask,abortsubtask,redosubtask,completesubtask"),
		@Param(name = "handlerList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "动作配置项信息")	
	})
	@Output({})
	@Description(desc = "通知策略保存触发动作配置项接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String uuid = jsonObj.getString("uuid");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(uuid);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(uuid);
		}

		String trigger = jsonObj.getString("trigger");
		JSONArray handlerList = jsonObj.getJSONArray("handlerList");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		JSONArray triggerList = configObj.getJSONArray("triggerList");
		for(int i = 0; i < triggerList.size(); i++) {
			JSONObject triggerObj = triggerList.getJSONObject(i);
			if(trigger.equals(triggerObj.getString("trigger"))) {
				triggerObj.put("handlerList", handlerList);
			}
		}
		notifyPolicyVo.setConfig(configObj.toJSONString());
		return null;
	}

}
