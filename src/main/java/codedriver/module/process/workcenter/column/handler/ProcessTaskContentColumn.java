package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterCondition;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskContentColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.CONTENT.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.CONTENT.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String content = el.getString(this.getName());
		return content;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}