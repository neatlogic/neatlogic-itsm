package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;

@Component
public class ProcessTaskStepStatusCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	@Override
	public String getName() {
		return "stepstatus";
	}

	@Override
	public String getDisplayName() {
		return "步骤状态";
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
		jsonObj.put("value", ProcessTaskStatus.PENDING.getValue());
		jsonObj.put("text", ProcessTaskStatus.PENDING.getText());
		jsonList.add(jsonObj);
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("value", ProcessTaskStatus.RUNNING.getValue());
		jsonObj1.put("text", ProcessTaskStatus.RUNNING.getText());
		jsonList.add(jsonObj1);
		
		JSONObject jsonObj3 = new JSONObject();
		jsonObj3.put("value", ProcessTaskStatus.FAILED.getValue());
		jsonObj3.put("text", ProcessTaskStatus.FAILED.getText());
		jsonList.add(jsonObj3);
		
		JSONObject jsonObj4 = new JSONObject();
		jsonObj4.put("value", ProcessTaskStatus.SUCCEED.getValue());
		jsonObj4.put("text", ProcessTaskStatus.SUCCEED.getText());
		jsonList.add(jsonObj4);
		
		JSONObject jsonObj5 = new JSONObject();
		jsonObj5.put("value", ProcessTaskStatus.HANG.getValue());
		jsonObj5.put("text", ProcessTaskStatus.HANG.getText());
		jsonList.add(jsonObj5);
		
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 8;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		Object value = StringUtils.EMPTY;
		if(condition.getValueList() instanceof String) {
			value = condition.getValueList();
		}else if(condition.getValueList() instanceof List) {
			List<String> values = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
			value = String.join("','", values);
		}
		String where = String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue())+".filtstatus",String.format("'%s'",  value));
		return where;
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
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
				return String.join("、", textList);
			}
		}		
		return value;
	}
}
