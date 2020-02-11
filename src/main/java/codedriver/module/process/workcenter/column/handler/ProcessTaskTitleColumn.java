package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.module.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskTitleColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "title";
	}

	@Override
	public String getDisplayName() {
		return "标题";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String title = el.getString("title");
		return title;
	}

}
