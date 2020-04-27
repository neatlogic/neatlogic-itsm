package codedriver.module.process.matrixattribute.handler;

import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.matrixattribute.core.IMatrixAttributeHandler;
@Component
public class SelectMatrixHandler implements IMatrixAttributeHandler {

	@Override
	public String getType() {
		return ProcessMatrixAttributeType.FORMSELECT.getValue();
	}

	@Override
	public JSONObject getData(ProcessMatrixAttributeVo processMatrixAttributeVo, String value) {
		JSONObject resultObj = new JSONObject();
		resultObj.put("type", ProcessMatrixAttributeType.FORMSELECT.getValue());
		resultObj.put("value", value);
		String config = processMatrixAttributeVo.getConfig();
		if(StringUtils.isNotBlank(config)) {
			JSONObject configObj = JSON.parseObject(config);
			JSONArray dataList = configObj.getJSONArray("dataList");
			if(CollectionUtils.isNotEmpty(dataList)) {
				for(int i = 0; i < dataList.size(); i++) {
					JSONObject data = dataList.getJSONObject(i);
					if(Objects.equals(value, data.getString("value"))) {
						resultObj.put("text", data.getString("text"));
						return resultObj;
					}
				}
			}
		}
		resultObj.put("text", value);
		return resultObj;
	}

}
