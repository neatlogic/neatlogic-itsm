package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskCurrentStepStatusColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "currentstepstatus";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤状态";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONArray currentStepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.CURRENT_STEP.getValue());
		if(CollectionUtils.isEmpty(currentStepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONArray stepArray = JSONArray.parseArray(currentStepArray.toJSONString());
		for(Object currentStepObj: stepArray) {
			JSONObject currentStepJson = (JSONObject)currentStepObj;
			currentStepJson.put("statusname", ProcessTaskStatus.getText(currentStepJson.getString("status")));
			currentStepJson.remove("handlerlist");
		}
		return stepArray;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 6;
	}

}
