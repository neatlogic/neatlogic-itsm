package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskChannelColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "channel";
	}

	@Override
	public String getDisplayName() {
		return "服务";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String channel = el.getString(this.getName());
		return channel;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
