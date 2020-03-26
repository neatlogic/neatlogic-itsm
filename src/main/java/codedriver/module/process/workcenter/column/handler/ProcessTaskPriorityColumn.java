package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskPriorityColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterColumn.PRIORITY.getValueEs();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.PRIORITY.getName();
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String priority = json.getString(this.getName());
		return priority;
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
