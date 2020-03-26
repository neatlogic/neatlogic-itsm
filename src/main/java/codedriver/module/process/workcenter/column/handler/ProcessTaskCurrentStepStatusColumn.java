package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
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
		/*List<String> currentStepStatusList = JSONObject.parseArray(json.getJSONArray("").toJSONString(), String.class);*/
		return null;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

}
