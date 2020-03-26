package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskExpiredTimeColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterColumn.EXPIRED_TIME.getValueEs();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.EXPIRED_TIME.getName();
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String expiredTime = json.getString(this.getName());
		return expiredTime;
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
