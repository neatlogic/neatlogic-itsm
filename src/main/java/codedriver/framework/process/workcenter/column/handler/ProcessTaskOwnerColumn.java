package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskOwnerColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "owner";
	}

	@Override
	public String getDisplayName() {
		return "上报人";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String owner = el.getString(this.getName());
		return owner;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
