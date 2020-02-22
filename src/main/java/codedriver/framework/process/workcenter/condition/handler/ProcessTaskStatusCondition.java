package codedriver.framework.process.workcenter.condition.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskStatusCondition implements IWorkcenterCondition{

	@Override
	public String getName() {
		return "status";
	}

	@Override
	public String getDisplayName() {
		return "流程状态";
	}

	@Override
	public String getHandler() {
		return WorkcenterConditionVo.Handler.CHECKBOX.toString();
	}
	
	@Override
	public String getType() {
		return WorkcenterConditionVo.Type.COMMON.toString();
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
		jsonObj2.put("value", ProcessTaskStatus.SUCCEED.getValue());
		jsonObj2.put("text", ProcessTaskStatus.SUCCEED.getText());
		jsonList.add(jsonObj3);
		
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 7;
	}

	@Override
	public String[] getExpressionList() {
		return new String[] { WorkcenterConditionVo.ProcessExpressionEs.EQUAL.getExpressionName() };
	}

}