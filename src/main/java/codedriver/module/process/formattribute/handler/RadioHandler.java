package codedriver.module.process.formattribute.handler;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Component
public class RadioHandler implements IFormAttributeHandler {

	@Override
	public String getType() {
		return ProcessFormHandler.FORMRADIO.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		return false;
	}

	@Override
	public String getValue(AttributeDataVo attributeDataVo, JSONObject configObj) {
		String value = attributeDataVo.getData();
		if(StringUtils.isNotBlank(value)) {
			String dataSource = configObj.getString("dataSource");
			if("static".equals(dataSource)) {
				List<ValueTextVo> dataList = JSON.parseArray(configObj.getString("dataList"), ValueTextVo.class);
				if(CollectionUtils.isNotEmpty(dataList)) {
					for(ValueTextVo data : dataList) {
						if(value.equals(data.getValue())) {
							return data.getText();
						}
					}
				}
			}else {//其他，如动态数据源，暂不实现
			}
		}
		
		return value;
	}

}
