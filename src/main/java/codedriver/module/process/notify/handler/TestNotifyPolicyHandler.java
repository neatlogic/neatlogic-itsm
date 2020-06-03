package codedriver.module.process.notify.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerBase;
import codedriver.framework.notify.dto.NotifyPolicyParamTypeVo;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.ProcessExpressionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessField;
import codedriver.framework.process.notify.core.NotifyTriggerType;
@Component
public class TestNotifyPolicyHandler extends NotifyPolicyHandlerBase {

	@Override
	public String getName() {
		return "TEST";
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

//	@Override
//	public List<NotifyPolicyParamTypeVo> myParamTypeList() {
//		List<NotifyPolicyParamTypeVo> resultList = new ArrayList<>();
//		String conditionModel = ProcessConditionModel.CUSTOM.getValue();
//		//固定字段条件
//		Map<String, IProcessTaskCondition> conditionMap = ProcessTaskConditionFactory.getConditionComponentMap();
//		for (Map.Entry<String, IProcessTaskCondition> entry : conditionMap.entrySet()) {
//			IProcessTaskCondition condition = entry.getValue();
//			if(ProcessField.getValue(condition.getName())== null) {
//				continue;
//			}
//			NotifyPolicyParamTypeVo notifyPolicyParamTypeVo = new NotifyPolicyParamTypeVo();
//			notifyPolicyParamTypeVo.setHandler(condition.getName());
//			notifyPolicyParamTypeVo.setHandlerName(condition.getDisplayName());
//			notifyPolicyParamTypeVo.setHandlerType(condition.getHandler(conditionModel));
//			if(condition.getConfig() != null) {
//				notifyPolicyParamTypeVo.setConfig(condition.getConfig().toJSONString());
//				notifyPolicyParamTypeVo.setIsMultiple(condition.getConfig().getBoolean("isMultiple"));
//			}
//			notifyPolicyParamTypeVo.setType(condition.getType());
//			notifyPolicyParamTypeVo.setDefaultExpression(condition.getDefaultExpression().getExpression());
//			List<ProcessExpressionVo> expressionList = new ArrayList<>();
//			for(ProcessExpression expression:condition.getExpressionList()) {
//				ProcessExpressionVo processExpressionVo = new ProcessExpressionVo();
//				processExpressionVo.setExpression(expression.getExpression());
//				processExpressionVo.setExpressionName(expression.getExpressionName());
//				expressionList.add(processExpressionVo);
//				notifyPolicyParamTypeVo.setExpressionList(expressionList);			
//			}
//			resultList.add(notifyPolicyParamTypeVo);
//		}
//		return resultList;
//	}

	@Override
	protected List<NotifyPolicyParamVo> mySystemParamList() {
		List<NotifyPolicyParamVo> notifyPolicyParamList = new ArrayList<>();
		return notifyPolicyParamList;
	}

}
