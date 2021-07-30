package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessSubTaskIdCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Override
    public String getName() {
        return "subtaskid";
    }

    @Override
    public String getDisplayName() {
        return "子任务id";
    }

	@Override
	public String getHandler(FormConditionModel processWorkcenterConditionType) {
		return FormHandlerType.INPUT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
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
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        return StringUtils.EMPTY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {

    }
}
