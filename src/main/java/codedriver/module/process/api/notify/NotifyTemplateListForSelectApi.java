package codedriver.module.process.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
import codedriver.framework.process.notify.core.NotifyDefaultTemplateFactory;
import codedriver.framework.process.notify.dto.NotifyTemplateVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class NotifyTemplateListForSelectApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/template/list/forselect";
	}

	@Override
	public String getName() {
		return "通知模板列表接口（下拉框专用）";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = false, desc = "插件"),
		@Param(name = "trigger", type = ApiParamType.STRING, isRequired = false, desc = "触发类型")
		})
	@Output({
		@Param(name = "Return", explode = ValueTextVo[].class, desc = "通知模板列表")
	})
	@Description(desc = "通知模板列表接口（下拉框专用）")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		NotifyTemplateVo notifyTemplateVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<NotifyTemplateVo>() {});
		List<ValueTextVo> notifyTemplateList = notifyMapper.getNotifyTemplateListForSelect(notifyTemplateVo);
		notifyTemplateList.add(new ValueTextVo(NotifyDefaultTemplateFactory.DEFAULT_TEMPLATE_UUID_PREFIX, NotifyDefaultTemplateFactory.DEFAULT_TEMPLATE_TYPE));
		return notifyTemplateList;
	}

}
