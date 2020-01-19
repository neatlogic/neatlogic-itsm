package codedriver.module.process.formattribute.handler;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.dto.AttributeDataVo;
import codedriver.framework.attribute.exception.AttributeValidException;
import codedriver.module.process.formattribute.core.IFormAttributeHandler;

public class TextHandler implements IFormAttributeHandler {

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getValue(AttributeDataVo attributeDataVo) {
		// TODO Auto-generated method stub
		return null;
	}

}
