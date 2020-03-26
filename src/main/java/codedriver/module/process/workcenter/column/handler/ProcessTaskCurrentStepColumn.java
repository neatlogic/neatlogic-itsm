package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskCurrentStepColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "currentstep";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONArray currentStepArray = json.getJSONArray(this.getName());
		if(CollectionUtils.isEmpty(currentStepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
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
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}
}
