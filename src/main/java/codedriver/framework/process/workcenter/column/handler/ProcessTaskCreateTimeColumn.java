package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskCreateTimeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "createTime";
	}

	@Override
	public String getDisplayName() {
		return "工单开始时间";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String createTime = el.getString(this.getName());
		return createTime;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
