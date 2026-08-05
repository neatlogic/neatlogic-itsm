package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import org.springframework.stereotype.Component;

import neatlogic.framework.util.$;
@Component
public class ProcessTaskProcessingOfMineCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Override
    public String getName() {
        return "processingofmine";
    }

    @Override
    public String getDisplayName() {
        return $.t("nmpch.processtaskprocessingofminecondition.getdisplayname");
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return FormHandlerType.SELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        return new JSONObject();
    }

    @Override
    public Integer getSort() {
        return 4;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.NUMBER;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return null;
    }

    @Override
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        getProcessingOfMineConditionSqlWhere(sqlSb);
    }
}
