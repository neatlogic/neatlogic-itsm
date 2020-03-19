package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionModel;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionType;
import codedriver.module.process.dto.PriorityVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskPriorityCondition implements IWorkcenterCondition{
	@Autowired
	private PriorityMapper priorityMapper;

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getName() {
		return ProcessWorkcenterCondition.PRIORITY.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.PRIORITY.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		if(ProcessWorkcenterConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
			return ProcessFormHandlerType.CHECKBOX.toString();
		}else {
			return ProcessFormHandlerType.SELECT.toString();
		}
	}
	
	@Override
	public String getType() {
		return ProcessWorkcenterConditionType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		PriorityVo priorityVo = new PriorityVo();
		priorityVo.setIsActive(1);
		List<PriorityVo> priorityList = priorityMapper.searchPriorityList(priorityVo);
		JSONArray jsonList = new JSONArray();
		for (PriorityVo priority : priorityList) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("value", priority.getUuid());
			jsonObj.put("text", priority.getName());
			jsonList.add(jsonObj);
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 5;
	}

	@Override
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.INCLUDE);
	}
	
	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.INCLUDE;
	}

	@Override
	public String buildScript(ProcessTaskStepVo currentProcessTaskStepVo, WorkcenterConditionVo conditionVo) {
		if(ProcessExpression.INCLUDE.getExpression().equals(conditionVo.getExpression())) {
			List<String> valueList = conditionVo.getValueList();
			if(CollectionUtils.isEmpty(valueList)) {
				return "(false)";
			}
			ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());		
			return "("+ valueList.contains(processTask.getPriorityUuid()) +")";
		}else {
			return "(false)";
		}
		
	}

}
