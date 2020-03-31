package codedriver.module.process.workcenter.column.handler;

import java.util.ListIterator;

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
		JSONArray stepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.STEP.getValue());
		if(CollectionUtils.isEmpty(stepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONArray stepResultArray = JSONArray.parseArray(stepArray.toJSONString());
		ListIterator<Object> stepIterator = stepResultArray.listIterator();
		while(stepIterator.hasNext()) {
			JSONObject currentStepJson = (JSONObject)stepIterator.next();
			if(currentStepJson.getString("status").equals(ProcessTaskStatus.RUNNING.getValue())) {
				currentStepJson.put("statusname", ProcessTaskStatus.getText(currentStepJson.getString("status")));
				//去掉待处理,但未开始的user/role/team
				JSONArray userTypeArray = currentStepJson.getJSONArray("usertypelist");
				if(CollectionUtils.isNotEmpty(userTypeArray)) {
					ListIterator<Object> userTypeIterator = userTypeArray.listIterator();
					while(userTypeIterator.hasNext()) {
						JSONObject userTypeJson = (JSONObject) userTypeIterator.next();
						if(userTypeJson.getString("usertype").equals("pending")) {
							userTypeIterator.remove();
						}
					}
				}
			}else {
				stepIterator.remove();
			}
		}
		return stepResultArray;
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
		return 5;
	}
}
