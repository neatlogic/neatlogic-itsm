package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskWorkTimeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "worktime";
	}

	@Override
	public String getDisplayName() {
		return "时间窗口";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String worktime = el.getString(this.getName());
		return worktime;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
