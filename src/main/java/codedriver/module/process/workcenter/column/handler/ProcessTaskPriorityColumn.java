package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterCondition;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskPriorityColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.PRIORITY.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.PRIORITY.getName();
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
