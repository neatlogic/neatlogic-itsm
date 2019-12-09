package codedriver.framework.process.attribute.handler;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.attribute.core.IAttributeHandler;
import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.DataCubeVo;
import codedriver.framework.process.dto.ProcessTaskAttributeDataVo;
import codedriver.framework.process.exception.AttributeValidException;
import codedriver.framework.process.mapper.AttributeMapper;
import codedriver.module.process.constvalue.AttributeHandler;

@Component
public class MselectHandler implements IAttributeHandler {

	@Autowired
	private AttributeMapper attributeMapper;

	@Override
	public String getType() {
		return AttributeHandler.MSELECT.getValue();
	}

	@Override
	public boolean valid(ProcessTaskAttributeDataVo processTaskAttributeDataVo, JSONObject configObj) throws AttributeValidException {
		return true;
	}

	@Override
	public String getConfigPage() {
		return "process.attribute.handler.mselect.mselectconfig";
	}

	@Override
	public String getInputPage() {
		return "process.attribute.handler.mselect.mselectinput";
	}

	@Override
	public String getViewPage() {
		return "process.attribute.handler.mselect.mselectview";
	}

	@Override
	public Object getData(AttributeVo attributeVo, Map<String, String[]> paramMap) {
		JSONArray returnValueList = new JSONArray();
		if (StringUtils.isNotBlank(attributeVo.getDataCubeUuid())) {
			DataCubeVo dataCubeVo = attributeMapper.getDataCubeByUuid(attributeVo.getDataCubeUuid());
			JSONArray valueList = dataCubeVo.getValueList();
			String textKey = "text";
			if (StringUtils.isNotBlank(attributeVo.getDataCubeValueField())) {
				textKey = attributeVo.getDataCubeTextField();
			}
			if (valueList != null) {
				for (int i = 0; i < valueList.size(); i++) {
					JSONObject valueObj = valueList.getJSONObject(i);
					JSONObject returnValueObj = new JSONObject();
					if (valueObj.containsKey(textKey)) {
						returnValueObj.put("value", valueObj.getString("_uuid"));
						returnValueObj.put("text", valueObj.getString("textKey"));
						returnValueList.add(returnValueObj);
					}
				}
			}
		}
		return returnValueList;
	}

	@Override
	public List<String> getValueList(Object dataObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText(Object dataObj) {
		
		return null;
	}

}
