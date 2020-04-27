package codedriver.module.process.matrixattribute.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.matrixattribute.core.IMatrixAttributeHandler;
@Component
public class TextMatrixHandler implements IMatrixAttributeHandler {

	@Override
	public String getType() {
		return ProcessMatrixAttributeType.FORMINPUT.getValue();
	}

	@Override
	public JSONObject getData(ProcessMatrixAttributeVo processMatrixAttributeVo, String value) {
		JSONObject resultObj = new JSONObject();
		resultObj.put("type", ProcessMatrixAttributeType.FORMINPUT.getValue());
		resultObj.put("value", value);
		resultObj.put("text", value);
		return resultObj;
	}

}
