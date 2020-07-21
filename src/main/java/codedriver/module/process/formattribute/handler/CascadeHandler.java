package codedriver.module.process.formattribute.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;

@Component
public class CascadeHandler implements IFormAttributeHandler {

	@Override
	public String getType() {
		return ProcessFormHandler.FORMCASCADELIST.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		return false;
	}

	@Override
	public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
		Object dataObj = attributeDataVo.getDataObj();
		if(dataObj != null) {
			List<String> textList = new ArrayList<>();
			List<String> valueList = JSON.parseArray(JSON.toJSONString(dataObj), String.class);
			String dataSource = configObj.getString("dataSource");
			if("static".equals(dataSource)) {
				if(valueList.size() > 0) {
					JSONArray dataList = configObj.getJSONArray("dataList");
					for(int i = 0; i < dataList.size(); i++) {
						JSONObject firstObj = dataList.getJSONObject(i);
						if(Objects.equals(firstObj.getString("value"), valueList.get(0))) {
							textList.add(firstObj.getString("text"));
							if(valueList.size() > 1) {
								JSONArray secondChildren = firstObj.getJSONArray("children");
								for(int j = 0; j < secondChildren.size(); j++) {
									JSONObject secondObj = secondChildren.getJSONObject(j);
									if(Objects.equals(secondObj.getString("value"), valueList.get(1))) {
										textList.add(secondObj.getString("text"));
										if(valueList.size() > 2) {
											JSONArray thirdChildren = secondObj.getJSONArray("children");
											for(int k = 0; k < thirdChildren.size(); k++) {
												JSONObject thirdObj = thirdChildren.getJSONObject(k);
												if(Objects.equals(thirdObj.getString("value"), valueList.get(2))) {
													textList.add(secondObj.getString("text"));
												}
											}
										}
									}
								}
							}
						}
					}
				}
				
			}else {//其他，如动态数据源
				for(String key : valueList) {
					if(key.contains("&=&")) {
						textList.add(key.split("&=&")[1]);
					}else {
						textList.add(key);
					}
				}
			}
			return textList;
		}
		return dataObj;
	}

}
