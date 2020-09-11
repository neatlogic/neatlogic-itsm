package codedriver.module.process.formattribute.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;

@Component
public class SelectHandler implements IFormAttributeHandler {

	@Override
	public String getType() {
		return ProcessFormHandler.FORMSELECT.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		return false;
	}

	@Override
	public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
		Object dataObj = attributeDataVo.getDataObj();
		if(dataObj != null) {
			boolean isMultiple = configObj.getBooleanValue("isMultiple");
			String dataSource = configObj.getString("dataSource");
			if("static".equals(dataSource)) {
				List<ValueTextVo> dataList = JSON.parseArray(JSON.toJSONString(configObj.getJSONArray("dataList")), ValueTextVo.class);
				if(CollectionUtils.isNotEmpty(dataList)) {
					Map<Object, String> valueTextMap = new HashMap<>();
					for(ValueTextVo data : dataList) {
						valueTextMap.put(data.getValue(), data.getText());
					}
					if(isMultiple) {
						List<String> valueList = JSON.parseArray(JSON.toJSONString(dataObj), String.class);
						if(CollectionUtils.isNotEmpty(valueList)) {
							List<String> textList = new ArrayList<>();
							for(String key : valueList) {
								String text = valueTextMap.get(key);
								if(text != null) {
									textList.add(text);
								}else {
									textList.add(key);
								}
							}
							return textList;
						}
						return valueList;
					}else {
						String text = valueTextMap.get((String)dataObj);
						if(text != null) {
							return text;
						}else {
							return dataObj;
						}
					}
				}
			}else {//其他，如动态数据源
				if(isMultiple) {
					List<String> valueList = JSON.parseArray(JSON.toJSONString(dataObj), String.class);
					if(CollectionUtils.isNotEmpty(valueList)) {
						List<String> textList = new ArrayList<>();
						for(String key : valueList) {
							if(key.contains(IFormAttributeHandler.SELECT_COMPOSE_JOINER)) {
								textList.add(key.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER)[1]);
							}else {
								textList.add(key);
							}
						}
						return textList;
					}
					return valueList;
				}else {
					String value = (String) dataObj;
					if(value.contains(IFormAttributeHandler.SELECT_COMPOSE_JOINER)) {
						return value.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER)[1];
					}else {
						return dataObj;
					}
				}
			}
		}		
		return dataObj;
	}

}
