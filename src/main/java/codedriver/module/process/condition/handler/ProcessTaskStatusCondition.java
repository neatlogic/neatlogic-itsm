package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;

@Component
public class ProcessTaskStatusCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	@Override
	public String getName() {
		return "status";
	}

	@Override
	public String getDisplayName() {
		return "工单状态";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		if(ProcessConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
			return FormHandlerType.CHECKBOX.toString();
		}else {
			return FormHandlerType.SELECT.toString();
		}
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		
		JSONArray jsonList = new JSONArray();
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("value", ProcessTaskStatus.RUNNING.getValue());
		jsonObj.put("text", ProcessTaskStatus.RUNNING.getText());
		jsonList.add(jsonObj);
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("value", ProcessTaskStatus.ABORTED.getValue());
		jsonObj1.put("text", ProcessTaskStatus.ABORTED.getText());
		jsonList.add(jsonObj1);
		
		JSONObject jsonObj2 = new JSONObject();
		jsonObj2.put("value", ProcessTaskStatus.FAILED.getValue());
		jsonObj2.put("text", ProcessTaskStatus.FAILED.getText());
		jsonList.add(jsonObj2);
		
		JSONObject jsonObj3 = new JSONObject();
		jsonObj3.put("value", ProcessTaskStatus.SUCCEED.getValue());
		jsonObj3.put("text", ProcessTaskStatus.SUCCEED.getText());
		jsonList.add(jsonObj3);
		
		JSONObject jsonObj4 = new JSONObject();
		jsonObj4.put("value", ProcessTaskStatus.DRAFT.getValue());
		jsonObj4.put("text", ProcessTaskStatus.DRAFT.getText());
		jsonList.add(jsonObj4);
		
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 7;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}

	@Override
	public Object valueConversionText(Object value) {
		if(value != null) {
			if(value instanceof String) {
				String text = ProcessTaskStatus.getText(value.toString());
				if(text != null) {
					return text;
				}
			}else if(value instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
				List<String> textList = new ArrayList<>();
				for(String valueStr : valueList) {
					String text = ProcessTaskStatus.getText(valueStr);
					if(text != null) {
						textList.add(text);					
					}else {
						textList.add(valueStr);
					}
				}
				return textList;
			}
		}		
		return value;
	}

}
