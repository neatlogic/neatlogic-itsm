package codedriver.module.process.api.notify;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
import codedriver.framework.process.exception.notify.NotifyTemplateNotFoundException;
import codedriver.framework.process.notify.dto.NotifyTemplateVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class NotifyTemplateSaveApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/template/save";
	}

	@Override
	public String getName() {
		return "通知模板保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "通知模板uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "名称"),
		@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "标题"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "内容"),
		@Param(name = "type", type = ApiParamType.STRING, desc = "类型"),
		@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = true, desc = "插件"),
		@Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "触发类型"),
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "通知模板uuid")
	})
	@Description(desc = "通知模板保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		NotifyTemplateVo notifyTemplate = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<NotifyTemplateVo>() {});
		String uuid = jsonObj.getString("uuid");
		if(StringUtils.isBlank(uuid)) {
			notifyTemplate.setFcu(UserContext.get().getUserId(true));
			notifyMapper.insertNotifyTemplate(notifyTemplate);
		}else {
			NotifyTemplateVo oldNotifyTemplate = notifyMapper.getNotifyTemplateByUuid(uuid);
			if(oldNotifyTemplate == null) {
				throw new NotifyTemplateNotFoundException(uuid);
			}
			if(!oldNotifyTemplate.equals(notifyTemplate)) {
				notifyTemplate.setLcu(UserContext.get().getUserId(true));
				notifyMapper.updateNotifyTemplateByUuid(notifyTemplate);
			}
		}
		return notifyTemplate.getUuid();
	}

}
