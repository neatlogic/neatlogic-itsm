package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionType;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskTitleCondition implements IWorkcenterCondition{
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getName() {
		return ProcessWorkcenterCondition.TITLE.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.TITLE.getName();
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
		return 2;
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
	public String buildScript(ProcessTaskStepVo currentProcessTaskStepVo, WorkcenterConditionVo conditionVo) {	
		if(ProcessExpression.LIKE.equals(conditionVo.getExpression())) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
			List<String> valueList = conditionVo.getValueList();
			return "(" + processTaskVo.getTitle().contains(valueList.get(0)) + ")";
		}else {
			return "(false)";
		}
	}
}
