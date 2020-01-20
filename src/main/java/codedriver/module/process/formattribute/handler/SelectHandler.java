package codedriver.module.process.formattribute.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.dto.AttributeDataVo;
import codedriver.framework.attribute.exception.AttributeValidException;
import codedriver.module.process.formattribute.core.IFormAttributeHandler;

@Component
public class SelectHandler implements IFormAttributeHandler {

	@Override
	public String getType() {
		return "select";
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getValue(AttributeDataVo attributeDataVo) {
		return attributeDataVo.getData();
	}

}
