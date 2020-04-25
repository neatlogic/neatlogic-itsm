package codedriver.module.process.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
import codedriver.framework.process.notify.core.NotifyDefaultTemplateFactory;
import codedriver.framework.process.notify.dto.NotifyTemplateVo;
import codedriver.framework.restful.annotation.Description;
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
	@Output({
		@Param(name = "Return", explode = ValueTextVo[].class, desc = "通知模板列表")
	})
	@Description(desc = "通知模板列表接口（下拉框专用）")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> notifyTemplateList = notifyMapper.getNotifyTemplateListForSelect();
		for(NotifyTemplateVo notifyTemplateVo : NotifyDefaultTemplateFactory.getDefaultTemplateList()) {
			notifyTemplateList.add(new ValueTextVo(notifyTemplateVo.getUuid(), notifyTemplateVo.getName()));
		}
		return notifyTemplateList;
	}

}
