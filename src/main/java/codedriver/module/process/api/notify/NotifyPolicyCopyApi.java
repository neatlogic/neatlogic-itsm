package codedriver.module.process.api.notify;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dto.NotifyPolicyVo;
import codedriver.framework.process.exception.notify.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyCopyApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/notify/policy/copy";
	}

	@Override
	public String getName() {
		return "通知策略复制接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "策略名"),
	})
	@Output({
		@Param(name = "notifyPolicy", explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略复制接口")
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
		NotifyPolicyVo newNotifyPolicy = new NotifyPolicyVo();
		newNotifyPolicy.setName(jsonObj.getString("name"));
		newNotifyPolicy.setConfig(notifyPolicyVo.getConfig());
		newNotifyPolicy.setFcd(new Date());
		newNotifyPolicy.setFcu(UserContext.get().getUserUuid(true));
		newNotifyPolicy.setFcuName(UserContext.get().getUserName());
		NotifyPolicyVo.notifyPolicyMap.put(newNotifyPolicy.getUuid(), newNotifyPolicy);
		return newNotifyPolicy;
	}

}
