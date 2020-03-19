package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;

@Component
public class ProcessTaskReporterColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.REPORTER.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.REPORTER.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String catalog = el.getString(this.getName());
		return catalog;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
