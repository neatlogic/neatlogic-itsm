package codedriver.module.process.api.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class NotifyTemplateDeleteApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/template/delete";
	}

	@Override
	public String getName() {
		return "通知模板删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "通知模板uuid")
	})
	@Description(desc = "通知模板删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		notifyMapper.deleteNotifyTemplateByUuid(jsonObj.getString("uuid"));
		return null;
	}

}
