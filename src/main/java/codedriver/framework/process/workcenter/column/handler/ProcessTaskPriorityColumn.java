package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskPriorityColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "priority";
	}

	@Override
	public String getDisplayName() {
		return "优先级";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String priority = el.getString(this.getName());
		return priority;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
