package codedriver.framework.process.workcenter.column.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskCurrentStepStatusColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "currentStepStatus";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤状态";
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
