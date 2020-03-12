package codedriver.framework.process.workcenter.column.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskCurrentStepUserColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "currentStepUser";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤处理人";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		List<String> currentStepUserList = el.getStringList(this.getName());
		return currentStepUserList;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
