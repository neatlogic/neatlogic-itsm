package codedriver.framework.process.workcenter.column.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskCurrentStepColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "currentStep";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤";
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
