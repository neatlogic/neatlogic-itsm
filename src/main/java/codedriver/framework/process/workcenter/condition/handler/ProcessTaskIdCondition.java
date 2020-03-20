package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionType;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskIdCondition implements IWorkcenterCondition{
	@Override
	public String getName() {
		return ProcessWorkcenterCondition.ID.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.ID.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return ProcessFormHandlerType.INPUT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessWorkcenterConditionType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

	@Override
	public Integer getSort() {
		return 1;
	}

	@Override
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.LIKE);
	}

	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.LIKE;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		if(ProcessExpression.LIKE.getExpression().equals(conditionVo.getExpression())) {
			List<String> valueList = conditionVo.getValueList();
			if(CollectionUtils.isEmpty(valueList)) {
				return false;
			}
			String id = conditionVo.getValueList().get(0);
			if(StringUtils.isBlank(id)) {
				return false;
			}
			return currentProcessTaskStepVo.getProcessTaskId().toString().contains(id);
		}else {
			return false;
		}
	}
}
