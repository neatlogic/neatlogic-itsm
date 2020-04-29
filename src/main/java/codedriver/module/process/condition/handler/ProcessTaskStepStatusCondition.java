package codedriver.module.process.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskStepStatusCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return "stepstatus";
	}

	@Override
	public String getDisplayName() {
		return "步骤状态";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		if(ProcessConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
			return ProcessFormHandlerType.CHECKBOX.toString();
		}else {
			return ProcessFormHandlerType.SELECT.toString();
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
		jsonObj.put("value", ProcessTaskStatus.PENDING.getValue());
		jsonObj.put("text", ProcessTaskStatus.PENDING.getText());
		jsonList.add(jsonObj);
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("value", ProcessTaskStatus.RUNNING.getValue());
		jsonObj1.put("text", ProcessTaskStatus.RUNNING.getText());
		jsonList.add(jsonObj1);
		
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
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		boolean result = false;
		List<String> valueList = conditionVo.getValueList();
		if(!CollectionUtils.isEmpty(valueList)) {
			ProcessTaskStepVo processTaskStep = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
			result = valueList.contains(processTaskStep.getStatus());
		}			
		if(ProcessExpression.INCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return result;
		}else if(ProcessExpression.EXCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return !result;
		}else {
			return false;
		}
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		Object value = condition.getValueList().get(0);
		if(condition.getValueList().size()>1) {
			value = String.join("','",condition.getValueList());
		}
		String where = String.format(ProcessExpression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue())+".filtstatus",String.format("'%s'",  value));
		return where;
	}
}
