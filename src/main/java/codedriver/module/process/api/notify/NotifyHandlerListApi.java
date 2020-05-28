package codedriver.module.process.api.notify;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class NotifyHandlerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/handler/list";
	}

	@Override
	public String getName() {
		return "通知插件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "通知插件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return NotifyHandlerFactory.getNotifyHandlerTypeList();
	}

}
