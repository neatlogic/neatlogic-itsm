package codedriver.module.process.notify.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerBase;
import codedriver.framework.notify.dto.ExpressionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessField;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.notify.core.NotifyTriggerType;
@Component
public class ProcessNotifyPolicyHandler extends NotifyPolicyHandlerBase {

	@Override
	public String getName() {
		return "ITSM";
	}
	
	@Override
	public List<ValueTextVo> myNotifyTriggerList() {
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
	protected List<ConditionParamVo> mySystemParamList() {
		List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
		String conditionModel = ProcessConditionModel.CUSTOM.getValue();
		Map<String, IProcessTaskCondition> conditionMap = ProcessTaskConditionFactory.getConditionComponentMap();
		for (Map.Entry<String, IProcessTaskCondition> entry : conditionMap.entrySet()) {
			IProcessTaskCondition condition = entry.getValue();
			if(ProcessField.getValue(condition.getName())== null) {
				continue;
			}
			ConditionParamVo param = new ConditionParamVo();
			param.setName(condition.getName());
			param.setDisplayName(condition.getDisplayName());
			param.setController(condition.getHandler(conditionModel));
			if(condition.getConfig() != null) {
				param.setIsMultiple(condition.getConfig().getBoolean("isMultiple"));
				param.setConfig(condition.getConfig().toJSONString());
			}
			param.setType(condition.getType());
			ParamType paramType = condition.getParamType();
			if(paramType != null) {
				param.setParamType(paramType.getName());
				param.setParamTypeName(paramType.getText());
				param.setDefaultExpression(paramType.getDefaultExpression().getExpression());
				for(Expression expression : paramType.getExpressionList()) {
					param.getExpressionList().add(new ExpressionVo(expression.getExpression(), expression.getExpressionName()));
				}
			}
			
			param.setIsEditable(0);
			notifyPolicyParamList.add(param);
		}
		return notifyPolicyParamList;
	}

	@Override
	protected void myAuthorityConfig(JSONObject config) {
		List<String> groupList = JSON.parseArray(config.getJSONArray("groupList").toJSONString(), String.class);
		groupList.add(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue());
		config.put("groupList", groupList);
	}

}
