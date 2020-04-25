package codedriver.module.process.api.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
import codedriver.framework.process.exception.notify.NotifyTemplateNotFoundException;
import codedriver.framework.process.notify.core.NotifyDefaultTemplateFactory;
import codedriver.framework.process.notify.dto.NotifyTemplateVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class NotifyTemplateGetApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/template/get";
	}

	@Override
	public String getName() {
		return "通知模板获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "通知模板uuid")
	})
	@Output({
		@Param(explode = NotifyTemplateVo.class, desc = "通知模板信息")
	})
	@Description(desc = "通知模板获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		NotifyTemplateVo notifyTemplateVo = null;
		if(uuid.startsWith(NotifyDefaultTemplateFactory.DEFAULT_TEMPLATE_UUID_PREFIX)) {
			notifyTemplateVo = NotifyDefaultTemplateFactory.getDefaultTemplateByUuid(uuid);
		}else {
			notifyTemplateVo = notifyMapper.getNotifyTemplateByUuid(uuid);
		}
		if(notifyTemplateVo == null) {
			throw new NotifyTemplateNotFoundException(uuid);
		}
		return notifyTemplateVo;
	}

}
