package codedriver.framework.process.attribute.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.attribute.core.IAttributeHandler;
import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.ProcessTaskAttributeDataVo;
import codedriver.framework.process.exception.AttributeValidException;
import codedriver.module.process.constvalue.AttributeHandler;

@Component
public class TextHandler implements IAttributeHandler {

	@Override
	public String getType() {
		return AttributeHandler.TEXT.getValue();
	}

	@Override
	public boolean valid(ProcessTaskAttributeDataVo processTaskAttributeDataVo, JSONObject configObj) throws AttributeValidException {
		return true;
	}

	@Override
	public String getConfigPage() {
		return null;
	}

	@Override
	public String getInputPage() {
		return "process.attribute.handler.text.textinput";
	}

	@Override
	public String getViewPage() {
		return "process.attribute.handler.text.textview";
	}

	@Override
	public Object getData(AttributeVo attributeVo, Map<String, String[]> paramMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getValueList(Object data) {
		List<String> valueList = new ArrayList<>();
		if (data != null) {
			valueList.add(data.toString());
		}
		return valueList;
	}

	@Override
	public String getText(Object dataObj) {
		if (dataObj != null) {
			return dataObj.toString();
		}
		return "";
	}
}
