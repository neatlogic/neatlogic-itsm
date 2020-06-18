package codedriver.module.process.condition.handler;

import java.util.List;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessTaskStartTimeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	@Override
	public String getName() {
		return "starttime";
	}

	@Override
	public String getDisplayName() {
		return "上报时间";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.DATE.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
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
	public ParamType getParamType() {
		return ParamType.DATE;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		return getDateEsWhere(condition,conditionList);
	}

}
