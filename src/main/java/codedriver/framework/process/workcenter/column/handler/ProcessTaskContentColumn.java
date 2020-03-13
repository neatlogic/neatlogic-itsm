package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskContentColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "content";
	}

	@Override
	public String getDisplayName() {
		return "上报内容";
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
