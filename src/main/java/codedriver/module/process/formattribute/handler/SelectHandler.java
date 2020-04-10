package codedriver.module.process.formattribute.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.exception.AttributeValidException;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dto.AttributeDataVo;
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
	public String getValue(AttributeDataVo attributeDataVo, JSONObject configObj) {
		String value = attributeDataVo.getData();
		List<String> valueList = null;
		boolean isMultiple = configObj.getBooleanValue("isMultiple");
		if(isMultiple) {
			valueList = JSON.parseArray(value, String.class);
			if(CollectionUtils.isEmpty(valueList)) {
				return value;
			}
		}else {
			if(StringUtils.isBlank(value)) {
				return value;
			}
		}
		String dataSource = configObj.getString("dataSource");
		if("static".equals(dataSource)) {
			List<ValueTextVo> dataList = JSON.parseArray(configObj.getString("dataList"), ValueTextVo.class);
			if(CollectionUtils.isNotEmpty(dataList)) {
				Map<String, String> valueTextMap = new HashMap<>();
				for(ValueTextVo data : dataList) {
					valueTextMap.put(data.getValue(), data.getText());
				}
				if(isMultiple) {
					StringBuilder result = new StringBuilder();
					for(String key : valueList) {
						result.append("、");
						String text = valueTextMap.get(key);
						if(text != null) {
							result.append(text);
						}else {
							result.append(key);
						}
					}
					return result.toString().substring(1);
				}else {
					String text = valueTextMap.get(value);
					if(text != null) {
						return text;
					}else {
						return value;
					}
				}
			}
		}else {//其他，如动态数据源，暂不实现
		}
		
		return value;
	}

}
