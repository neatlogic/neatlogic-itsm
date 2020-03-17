package codedriver.framework.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;

@Component
public class ProcessTaskExpiredTimeColumn implements IWorkcenterColumn{

	@Override
	public String getName() {
		return ProcessWorkcenterCondition.EXPIRED_TIME.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.EXPIRED_TIME.getName();
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
