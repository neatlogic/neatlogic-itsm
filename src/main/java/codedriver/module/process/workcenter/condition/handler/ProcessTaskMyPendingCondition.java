package codedriver.module.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.condition.core.IWorkcenterCondition;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskMyPendingCondition implements IWorkcenterCondition{
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return "mypending";
	}

	@Override
	public String getDisplayName() {
		return "我待处理的";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return ProcessFormHandlerType.RADIO.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		JSONArray jsonList = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("value",1);
		jsonObj.put("text", "是");
		jsonList.add(jsonObj);
		JSONObject jsonObj2 = new JSONObject();
		jsonObj2.put("value",0);
		jsonObj2.put("text", "否");
		jsonList.add(jsonObj2);
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", false);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 9;
	}

	@Override
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE);
	}
	
	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.INCLUDE;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		boolean result = false;
		List<String> valueList = conditionVo.getValueList();
		if(!CollectionUtils.isEmpty(valueList)) {
			ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
			result = valueList.contains(processTask.getChannelUuid());
		}			
		if(ProcessExpression.INCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return result;
		}else if(ProcessExpression.EXCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return !result;
		}else {
			return false;
		}
	}

}
