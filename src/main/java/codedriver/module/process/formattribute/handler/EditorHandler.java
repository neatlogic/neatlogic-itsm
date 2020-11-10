package codedriver.module.process.formattribute.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Component
public class EditorHandler implements IFormAttributeHandler {

	@Override
	public String getType() {
		return ProcessFormHandlerType.FORMEDITOR.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		return false;
	}

	@Override
	public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
		return attributeDataVo.getData();
	}

}
