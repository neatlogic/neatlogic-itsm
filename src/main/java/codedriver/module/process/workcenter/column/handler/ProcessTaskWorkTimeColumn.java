package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterCondition;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskWorkTimeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.WOKRTIME.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.WOKRTIME.getName();
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
