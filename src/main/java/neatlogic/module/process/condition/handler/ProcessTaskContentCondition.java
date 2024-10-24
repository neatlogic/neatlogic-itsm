package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskConditionType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class ProcessTaskContentCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "content";
    }

    @Override
    public String getDisplayName() {
        return "上报内容";
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
        config.put("maxlength", 50);
        return config;
    }

    @Override
    public Integer getSort() {
        return 3;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.STRING;
    }

    @Override
    public Expression getExpression() {
        return Expression.LIKE;
    }

    @Override
    public List<Expression> getExpressionList() {
        return Arrays.asList(Expression.LIKE, Expression.NOTLIKE);
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return value;
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        String content = processTaskMapper.getProcessTaskStartContentByProcessTaskId(processTaskStepVo.getProcessTaskId());
        if (StringUtils.isNotBlank(content)) {
            return content;
        }
        return "";
    }

    @Override
    public boolean isShow(JSONObject object, String type) {
        return !Objects.equals(type, ProcessTaskConditionType.WORKCENTER.getValue());
    }
}
