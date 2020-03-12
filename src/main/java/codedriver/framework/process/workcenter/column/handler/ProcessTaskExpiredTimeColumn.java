package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskExpiredTimeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "expiredTime";
	}

	@Override
	public String getDisplayName() {
		return "超时时间";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String expiredTime = el.getString(this.getName());
		return expiredTime;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
