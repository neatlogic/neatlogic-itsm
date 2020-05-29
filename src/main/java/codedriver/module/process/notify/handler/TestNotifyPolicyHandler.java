package codedriver.module.process.notify.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessField;
import codedriver.framework.process.notify.core.NotifyTriggerType;
@Component
public class TestNotifyPolicyHandler implements INotifyPolicyHandler{

	@Override
	public String getName() {
		return "TEST";
	}
	
	@Override
	public List<ValueTextVo> getNotifyTriggerList() {
		List<ValueTextVo> returnList = new ArrayList<>();
		for (NotifyTriggerType notifyTriggerType : NotifyTriggerType.values()) {
			if(NotifyTriggerType.TIMEOUT == notifyTriggerType) {
				continue;
			}
			returnList.add(new ValueTextVo(notifyTriggerType.getTrigger(), notifyTriggerType.getText()));
		}
		return returnList;
	}

	@Override
	public List<ValueTextVo> getVariableTypeList() {
		JSONArray resultArray = new JSONArray();
		String conditionModel = ProcessConditionModel.CUSTOM.getValue();
		//固定字段条件
		Map<String, IProcessTaskCondition> workcenterConditionMap = ProcessTaskConditionFactory.getConditionComponentMap();
		for (Map.Entry<String, IProcessTaskCondition> entry : workcenterConditionMap.entrySet()) {
			IProcessTaskCondition condition = entry.getValue();
			if(ProcessField.getValue(condition.getName())== null) {
				continue;
			}
			JSONObject commonObj = new JSONObject();
			commonObj.put("handler", condition.getName());
			commonObj.put("handlerName", condition.getDisplayName());
			commonObj.put("handlerType", condition.getHandler(conditionModel));
			if(condition.getConfig() != null) {
				commonObj.put("isMultiple",condition.getConfig().getBoolean("isMultiple"));
				commonObj.put("config", condition.getConfig().toJSONString());
			}
			commonObj.put("type", condition.getType());
			commonObj.put("defaultExpression", condition.getDefaultExpression().getExpression());
			JSONArray expressiobArray = new JSONArray();
			for(ProcessExpression expression:condition.getExpressionList()) {
				JSONObject expressionObj = new JSONObject();
				expressionObj.put("expression", expression.getExpression());
				expressionObj.put("expressionName", expression.getExpressionName());
				expressiobArray.add(expressionObj);
				commonObj.put("expressionList", expressiobArray);
			}
			resultArray.add(commonObj);
		}
		return null;
	}

}
