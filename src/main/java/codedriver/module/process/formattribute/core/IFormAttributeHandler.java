package codedriver.module.process.formattribute.core;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.dto.AttributeDataVo;
import codedriver.framework.attribute.exception.AttributeValidException;

public interface IFormAttributeHandler {
	public String getType();

	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException;

	public String getValue(AttributeDataVo attributeDataVo);
}
