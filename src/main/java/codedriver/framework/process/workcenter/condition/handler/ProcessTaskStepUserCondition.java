package codedriver.framework.process.workcenter.condition.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskStepUserCondition implements IWorkcenterCondition{

	@Override
	public String getName() {
		return "stepUser";
	}

	@Override
	public String getDisplayName() {
		return "处理人";
	}

	@Override
	public String getHandler() {
		return WorkcenterConditionVo.Handler.USERSELECT.toString();
	}
	
	@Override
	public String getType() {
		return WorkcenterConditionVo.Type.COMMON.toString();
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

	@Override
	public Integer getSort() {
		return 4;
	}

	@Override
	public String[] getExpressionList() {
		return new String[] { WorkcenterConditionVo.ProcessExpressionEs.EQUAL.getExpressionName() };
	}

}
