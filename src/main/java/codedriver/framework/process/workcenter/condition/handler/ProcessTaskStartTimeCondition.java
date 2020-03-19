package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
public class ProcessTaskStartTimeCondition implements IWorkcenterCondition{

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.STARTTIME.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.STARTTIME.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return ProcessFormHandlerType.DATE.toString();
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
		return Arrays.asList(ProcessExpression.BETWEEN);
	}
	
	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.BETWEEN;
	}

	@Override
	public String buildScript(ProcessTaskStepVo currentProcessTaskStepVo, WorkcenterConditionVo conditionVo) {
		if(ProcessExpression.BETWEEN.getExpression().equals(conditionVo.getExpression())) {
			List<String> valueList = conditionVo.getValueList();
			if(CollectionUtils.isEmpty(valueList)) {
				return "(false)";
			}
			ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
			boolean result = false;
			long start = Long.parseLong(valueList.get(0));
			if(processTask.getStartTime().getTime() >= start) {
				result = true;
			}
			if(result && valueList.size() == 2) {
				long end = Long.parseLong(valueList.get(1));
				if(processTask.getStartTime().getTime() <= end) {
					result = true;
				}else {
					result = true;
				}
			}
			return "(" + result + ")";
		}else {
			return "(false)";
		}
	}

}
