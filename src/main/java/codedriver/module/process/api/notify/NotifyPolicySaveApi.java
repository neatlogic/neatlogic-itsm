package codedriver.module.process.api.notify;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dto.NotifyPolicyVo;
import codedriver.framework.process.exception.notify.NotifyPolicyNotFoundException;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicySaveApi  extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/notify/policy/save";
	}

	@Override
	public String getName() {
		return "通知策略信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "策略uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "策略名"),
	})
	@Output({
		@Param(name = "notifyPolicy", explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {		
		String uuid = jsonObj.getString("uuid");
		String name = jsonObj.getString("name");
		if(StringUtils.isNotBlank(uuid)) {
			NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(uuid);
			if(notifyPolicyVo == null) {
				throw new NotifyPolicyNotFoundException(uuid);
			}
			notifyPolicyVo.setName(name);
			return notifyPolicyVo;
		}else {
			NotifyPolicyVo notifyPolicyVo = new NotifyPolicyVo();
			notifyPolicyVo.setName(name);
			notifyPolicyVo.setFcd(new Date());
			notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
			notifyPolicyVo.setFcuName(UserContext.get().getUserName());
			JSONObject configObj = new JSONObject();
			JSONArray triggerList = new JSONArray();
			for (NotifyTriggerType notifyTriggerType : NotifyTriggerType.values()) {
				if(NotifyTriggerType.TIMEOUT == notifyTriggerType) {
					continue;
				}
				JSONObject triggerObj = new JSONObject();
				triggerObj.put("trigger", notifyTriggerType.getTrigger());
				triggerObj.put("triggerName", notifyTriggerType.getText());
				triggerObj.put("handlerList", new JSONArray());
			}
			configObj.put("triggerList", triggerList);
			notifyPolicyVo.setConfig(configObj.toJSONString());
			NotifyPolicyVo.notifyPolicyMap.put(notifyPolicyVo.getUuid(), notifyPolicyVo);
			NotifyPolicyVo.notifyPolicyList.add(notifyPolicyVo);
			return notifyPolicyVo;
		}
	}

}
