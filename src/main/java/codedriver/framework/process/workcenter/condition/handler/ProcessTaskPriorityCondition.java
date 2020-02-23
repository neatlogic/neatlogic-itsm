package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.dto.PriorityVo;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskPriorityCondition implements IWorkcenterCondition{
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getName() {
		return "priority";
	}

	@Override
	public String getDisplayName() {
		return "优先级";
	}

	@Override
	public String getHandler() {
		return ProcessFormHandlerType.SELECT.toString();
	}
	
	@Override
	public String getType() {
		return WorkcenterConditionVo.Type.COMMON.toString();
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

}
