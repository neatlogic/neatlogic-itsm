package codedriver.framework.process.attribute.handler;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.attribute.core.IAttributeHandler;
import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.ProcessTaskAttributeDataVo;
import codedriver.framework.process.exception.AttributeValidException;
import codedriver.framework.process.mapper.AttributeMapper;
import codedriver.module.process.constvalue.AttributeHandler;

@Component
public class FileHandler implements IAttributeHandler {

	@Autowired
	private AttributeMapper attributeMapper;

	@Override
	public String getType() {
		return AttributeHandler.FILE.getValue();
	}

	@Override
	public boolean valid(ProcessTaskAttributeDataVo processTaskAttributeDataVo, JSONObject configObj) throws AttributeValidException {
		return true;
	}

	@Override
	public String getConfigPage() {
		return "process.attribute.handler.file.fileconfig";
	}

	@Override
	public String getInputPage() {
		return "process.attribute.handler.file.fileinput";
	}

	@Override
	public String getViewPage() {
		return "process.attribute.handler.file.fileview";
	}

	@Override
	public Object getData(AttributeVo attributeVo, Map<String, String[]> paramMap) {
		return null;
	}

	@Override
	public List<String> getValueList(Object data) {
		return null;
	}

	@Override
	public String getText(Object data) {
		try {
			JSONObject jsonObj = JSONObject.parseObject(data.toString());
			if (jsonObj.containsKey("text")) {
				return jsonObj.getString("text");
			}
		} catch (Exception ex) {

		}
		return "";
	}

}
