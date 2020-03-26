package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskStartTimeColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "starttime";
	}

	@Override
	public String getDisplayName() {
		return "上报时间";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String createTime = json.getString(this.getName());
		return createTime;
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
