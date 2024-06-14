package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import org.springframework.stereotype.Component;

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
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepVo.getProcessTaskStepTaskVo();
        if (stepTaskVo != null) {
            return stepTaskVo.getId();
        }
        return null;
    }
}
