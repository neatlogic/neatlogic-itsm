package codedriver.module.process.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskStatusCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return "status";
	}

	@Override
	public String getDisplayName() {
		return "工单状态";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		if(ProcessConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
			return FormHandlerType.CHECKBOX.toString();
		}else {
			return FormHandlerType.SELECT.toString();
		}
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		
		JSONArray jsonList = new JSONArray();
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("value", ProcessTaskStatus.RUNNING.getValue());
		jsonObj.put("text", ProcessTaskStatus.RUNNING.getText());
		jsonList.add(jsonObj);
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("value", ProcessTaskStatus.ABORTED.getValue());
		jsonObj1.put("text", ProcessTaskStatus.ABORTED.getText());
		jsonList.add(jsonObj1);
		
		JSONObject jsonObj2 = new JSONObject();
		jsonObj2.put("value", ProcessTaskStatus.FAILED.getValue());
		jsonObj2.put("text", ProcessTaskStatus.FAILED.getText());
		jsonList.add(jsonObj2);
		
		JSONObject jsonObj3 = new JSONObject();
		jsonObj3.put("value", ProcessTaskStatus.SUCCEED.getValue());
		jsonObj3.put("text", ProcessTaskStatus.SUCCEED.getText());
		jsonList.add(jsonObj3);
		
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 7;
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
			result = valueList.contains(processTask.getStatus());
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
