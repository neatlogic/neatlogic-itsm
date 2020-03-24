package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterCondition;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskStartTimeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.STARTTIME.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.STARTTIME.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String createTime = el.getString(this.getName());
		return createTime;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
