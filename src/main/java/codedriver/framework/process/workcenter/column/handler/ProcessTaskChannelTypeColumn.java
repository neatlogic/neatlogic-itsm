package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;

@Component
public class ProcessTaskChannelTypeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.CHANNELTYPE.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.CHANNELTYPE.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String channelType = el.getString(this.getName());
		return channelType;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
