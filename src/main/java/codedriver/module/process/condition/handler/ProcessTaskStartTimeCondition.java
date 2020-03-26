package codedriver.module.process.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.condition.ConditionVo;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;

@Component
public class ProcessTaskStartTimeCondition implements IWorkcenterCondition{

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return "starttime";
	}

	@Override
	public String getDisplayName() {
		return "上报时间";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return ProcessFormHandlerType.DATE.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
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
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		if(ProcessExpression.BETWEEN.getExpression().equals(conditionVo.getExpression())) {
			List<String> valueList = conditionVo.getValueList();
			if(CollectionUtils.isEmpty(valueList)) {
				return false;
			}
			ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
			boolean result = false;
			long left = Long.parseLong(valueList.get(0));
			if(processTask.getStartTime().getTime() >= left) {
				result = true;
			}
			if(result && valueList.size() == 2) {
				long right = Long.parseLong(valueList.get(1));
				if(processTask.getStartTime().getTime() <= right) {
					result = true;
				}else {
					result = false;
				}
			}
			return result;
		}else {
			return false;
		}
	}

}
