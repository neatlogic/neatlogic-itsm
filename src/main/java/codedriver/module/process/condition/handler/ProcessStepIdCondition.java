package codedriver.module.process.condition.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessStepIdCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	@Override
	public String getName() {
		return "stepid";
	}

	@Override
	public String getDisplayName() {
		return "步骤id";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.INPUT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		JSONObject config = new JSONObject();
		config.put("type", "text");
		config.put("value", "");
		config.put("defaultValue", "");
		config.put("maxlength", 16);
//		config.put("name", "");
//		config.put("label", "");
		return config;
	}

	@Override
	public Integer getSort() {
		return 1;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.STRING;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		return StringUtils.EMPTY;
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
		return value;
	}
}
