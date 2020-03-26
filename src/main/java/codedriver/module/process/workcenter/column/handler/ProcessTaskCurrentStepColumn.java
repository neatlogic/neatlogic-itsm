package codedriver.module.process.workcenter.column.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskCurrentStepColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterColumn.CURRENT_STEP.getValueEs();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.CURRENT_STEP.getName();
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONArray currentStepArray = json.getJSONArray(this.getName());
		JSONArray currentStepList = new JSONArray();
		for(Object step : currentStepArray) {
			JSONObject stepJson = new JSONObject();
			stepJson.put("name", ((JSONObject)step).getString("name"));
			stepJson.put("id", ((JSONObject)step).getString("id"));
			currentStepList.add(stepJson);
		}
		return currentStepList;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		return ProcessWorkcenterColumnType.COMMON.getValue();
	}
}
