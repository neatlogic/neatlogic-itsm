package codedriver.module.process.workcenter.column.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterCondition;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskCurrentStepStatusColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.CURRENT_STEP_STATUS.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.CURRENT_STEP_USER.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		List<String> currentStepStatusList = el.getStringList(this.getName());
		return currentStepStatusList;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
