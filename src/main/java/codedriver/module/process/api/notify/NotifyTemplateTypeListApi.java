package codedriver.module.process.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class NotifyTemplateTypeListApi extends ApiComponentBase{

	@Autowired
	private NotifyMapper notifyMapper;
	
	@Override
	public String getToken() {
		return "notify/template/type/list";
	}

	@Override
	public String getName() {
		return "通知模板类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
		@Param(name = "Return", explode = ValueTextVo[].class, desc = "通知模板类型列表")
	})
	@Description(desc = "通知模板类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> resultObj = new ArrayList<>();
		resultObj.add(new ValueTextVo("", "所有"));
		List<String> typeList = notifyMapper.getNotifyTemplateTypeList();
		for(String type : typeList) {
			if(StringUtils.isNotBlank(type)) {
				resultObj.add(new ValueTextVo(type, type));
			}
		}
		return resultObj;
	}

}
