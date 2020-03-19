package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionModel;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionType;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskStepStatusCondition implements IWorkcenterCondition{

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.CURRENT_STEP_STATUS.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.CURRENT_STEP_STATUS.getName();
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
		
		JSONArray jsonList = new JSONArray();
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("value", ProcessTaskStatus.PENDING.getValue());
		jsonObj.put("text", ProcessTaskStatus.PENDING.getText());
		jsonList.add(jsonObj);
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("value", ProcessTaskStatus.RUNNING.getValue());
		jsonObj1.put("text", ProcessTaskStatus.RUNNING.getText());
		jsonList.add(jsonObj1);
		
		JSONObject jsonObj2 = new JSONObject();
		jsonObj2.put("value", ProcessTaskStatus.ABORTED.getValue());
		jsonObj2.put("text", ProcessTaskStatus.ABORTED.getText());
		jsonList.add(jsonObj2);
		
		JSONObject jsonObj3 = new JSONObject();
		jsonObj3.put("value", ProcessTaskStatus.FAILED.getValue());
		jsonObj3.put("text", ProcessTaskStatus.FAILED.getText());
		jsonList.add(jsonObj3);
		
		JSONObject jsonObj4 = new JSONObject();
		jsonObj4.put("value", ProcessTaskStatus.SUCCEED.getValue());
		jsonObj4.put("text", ProcessTaskStatus.SUCCEED.getText());
		jsonList.add(jsonObj4);
		
		JSONObject jsonObj5 = new JSONObject();
		jsonObj5.put("value", ProcessTaskStatus.HANG.getValue());
		jsonObj5.put("text", ProcessTaskStatus.HANG.getText());
		jsonList.add(jsonObj5);
		
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 8;
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
	public String buildScript(ProcessTaskStepVo currentProcessTaskStepVo, WorkcenterConditionVo conditionVo) {
		boolean result = false;
		List<String> valueList = conditionVo.getValueList();
		if(!CollectionUtils.isEmpty(valueList)) {
			ProcessTaskStepVo processTaskStep = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
			result = valueList.contains(processTaskStep.getStatus());
		}			
		if(ProcessExpression.INCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return "(" + result + ")";
		}else if(ProcessExpression.EXCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return "(" + !result + ")";
		}else {
			return "(false)";
		}
	}
}
