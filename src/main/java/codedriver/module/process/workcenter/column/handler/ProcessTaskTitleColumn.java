package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterCondition;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskTitleColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.TITLE.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.TITLE.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String title = el.getString(this.getName());
		return title;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
