package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
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

	private String formHandlerType = FormHandlerType.SELECT.toString();
	
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
			formHandlerType = FormHandlerType.CHECKBOX.toString();
		}else {
			formHandlerType = FormHandlerType.SELECT.toString();
		}
		return formHandlerType;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {		
		JSONArray dataList = new JSONArray();
		dataList.add(new ValueTextVo(ProcessTaskStatus.PENDING.getValue(), ProcessTaskStatus.PENDING.getText()));
		dataList.add(new ValueTextVo(ProcessTaskStatus.RUNNING.getValue(), ProcessTaskStatus.RUNNING.getText()));
		dataList.add(new ValueTextVo(ProcessTaskStatus.FAILED.getValue(), ProcessTaskStatus.FAILED.getText()));
		dataList.add(new ValueTextVo(ProcessTaskStatus.SUCCEED.getValue(), ProcessTaskStatus.SUCCEED.getText()));
		dataList.add(new ValueTextVo(ProcessTaskStatus.HANG.getValue(), ProcessTaskStatus.HANG.getText()));
		
		JSONObject config = new JSONObject();
		config.put("type", formHandlerType);
		config.put("search", false);
		config.put("multiple", true);
		config.put("value", "");
		config.put("defaultValue", "");
		config.put("dataList", dataList);
		/** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
		config.put("isMultiple", true);
		return config;
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
