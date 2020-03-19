package codedriver.framework.process.workcenter.column.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;

@Component
public class ProcessTaskCurrentStepColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.CURRENT_STEP.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.CURRENT_STEP.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		List<String> currentStepList = el.getStringList(this.getName());
		return currentStepList;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
