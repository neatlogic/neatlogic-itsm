package codedriver.module.process.api.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
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

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
