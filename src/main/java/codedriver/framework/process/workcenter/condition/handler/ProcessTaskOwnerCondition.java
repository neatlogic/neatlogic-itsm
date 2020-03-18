package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionType;

@Component
public class ProcessTaskOwnerCondition implements IWorkcenterCondition{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.OWNER.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.OWNER.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return ProcessFormHandlerType.USERSELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessWorkcenterConditionType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		JSONObject returnObj = new JSONObject();
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 3;
	}

	@Override
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.INCLUDE);
	}
	
	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.INCLUDE;
	}

}
