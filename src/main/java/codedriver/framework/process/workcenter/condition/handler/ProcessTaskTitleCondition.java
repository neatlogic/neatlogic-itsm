package codedriver.framework.process.workcenter.condition.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskTitleCondition implements IWorkcenterCondition{

	@Override
	public String getName() {
		return "title";
	}

	@Override
	public String getDisplayName() {
		return "标题";
	}

	@Override
	public String getType() {
		return WorkcenterConditionVo.Type.TEXT.toString();
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

	@Override
	public Integer getSort() {
		return 1;
	}

	@Override
	public String[] getExpressionList() {
		return new String[] { WorkcenterConditionVo.ProcessExpressionEs.EQUAL.getExpressionName() };
	}

}
