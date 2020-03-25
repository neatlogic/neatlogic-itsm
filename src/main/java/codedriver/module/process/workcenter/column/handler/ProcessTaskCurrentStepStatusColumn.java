package codedriver.module.process.workcenter.column.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskCurrentStepStatusColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterColumn.CURRENT_STEP_STATUS.getValueEs();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.CURRENT_STEP_USER.getName();
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
		return ProcessWorkcenterColumnType.COMMON.getValue();
	}

}
