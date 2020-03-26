package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskStatusColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterColumn.STATUS.getValueEs();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.STATUS.getName();
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String status = json.getString(this.getName());
		return status;
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
