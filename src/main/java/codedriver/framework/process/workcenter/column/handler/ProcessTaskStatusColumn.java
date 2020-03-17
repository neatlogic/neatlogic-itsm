package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;

@Component
public class ProcessTaskStatusColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.STATUS.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.STATUS.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String status = el.getString(this.getName());
		return status;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
