package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskReporterColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "reporter";
	}

	@Override
	public String getDisplayName() {
		return "代报人";
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
