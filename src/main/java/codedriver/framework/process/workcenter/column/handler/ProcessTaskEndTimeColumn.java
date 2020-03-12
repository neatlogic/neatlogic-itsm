package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskEndTimeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "endTime";
	}

	@Override
	public String getDisplayName() {
		return "工单结束时间";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String endTime = el.getString(this.getName());
		return endTime;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
