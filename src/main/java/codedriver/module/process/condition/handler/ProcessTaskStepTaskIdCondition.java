package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessTaskStepTaskIdCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Override
    public String getName() {
        return "steptaskid";
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
        return 20;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.STRING;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {

    }

    @Override
    public Object getConditionParamData(ProcessTaskVo processTaskVo) {
        ProcessTaskStepVo currentTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        if (currentTaskStepVo != null) {
            ProcessTaskStepTaskVo stepTaskVo = currentTaskStepVo.getProcessTaskStepTaskVo();
            if (stepTaskVo != null) {
                return stepTaskVo.getId();
            }
        }
        return null;
    }
}
