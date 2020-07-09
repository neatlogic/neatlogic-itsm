package codedriver.module.process.formattribute.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Component
public class CheckboxHandler implements IFormAttributeHandler {

	@Override
	public String getType() {
		return ProcessFormHandler.FORMCHECKBOX.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		return false;
	}

	@Override
	public Object getValue(AttributeDataVo attributeDataVo, JSONObject configObj) {
		List<String> valueList = JSON.parseArray(attributeDataVo.getData(), String.class);
		if(CollectionUtils.isNotEmpty(valueList)) {
			StringBuilder result = new StringBuilder();
			String dataSource = configObj.getString("dataSource");
			if("static".equals(dataSource)) {
				Map<String, String> valueTextMap = new HashMap<>();
				List<ValueTextVo> dataList = JSON.parseArray(configObj.getString("dataList"), ValueTextVo.class);
				if(CollectionUtils.isNotEmpty(dataList)) {
					for(ValueTextVo data : dataList) {
						valueTextMap.put(data.getValue(), data.getText());
					}
				}
				for(String value : valueList) {
					result.append("、");
					String text = valueTextMap.get(value);
					if(text != null) {
						result.append(text);
					}else {
						result.append(value);
					}
				}
			}else {//其他，如动态数据源
				for(String value : valueList) {
					result.append("、");
					if(value.contains("&=&")) {
						result.append(value.split("&=&")[1]);
					}else {
						result.append(value);
					}
				}
			}			
			return result.toString().substring(1);
		}else {
			return "";
		}
	}

}
