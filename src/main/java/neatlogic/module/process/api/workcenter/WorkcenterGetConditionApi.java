package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessTaskConditionType;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterGetConditionApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "workcenter/condition/get";
    }

    @Override
    public String getName() {
        return "nmpaw.workcentergetconditionapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "conditionModel", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.input.param.desc.conditionmodel"),
            @Param(name = "workcenterUuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpaw.workcentergetconditionapi.input.param.desc.workcenteruuid"),
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.output.param.desc.uuid"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.output.param.desc.handler"),
            @Param(name = "handlerName", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.output.param.desc.handlername"),
            @Param(name = "handlerType", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.output.param.desc.handlertype"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.output.param.desc.type"),
            @Param(name = "expressionList[0].expression", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.output.param.desc.expressionlist.0.expression"),
            @Param(name = "expressionList[0].expressionName", type = ApiParamType.STRING, desc = "nmpaw.workcentergetconditionapi.output.param.desc.expressionlist.0.expressionname")
    })
    @Description(desc = "nmpaw.workcentergetconditionapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultArray = new JSONArray();
        String conditionModel = jsonObj.getString("conditionModel");
        FormConditionModel formConditionModel = FormConditionModel.getFormConditionModel(conditionModel);
        formConditionModel = formConditionModel == null ? FormConditionModel.CUSTOM : formConditionModel;
        //固定字段条件
        for(ProcessWorkcenterField f : ProcessWorkcenterField.values() ){
            IProcessTaskCondition condition = ProcessTaskConditionFactory.getHandler(f.getValue());
            if (condition == null || !condition.isShow(jsonObj, ProcessTaskConditionType.WORKCENTER.getValue())) {
                continue;
            }
            JSONObject config = condition.getConfig(ConditionConfigType.WORKCENTER);
            JSONObject commonObj = new JSONObject();
            commonObj.put("handler", condition.getName());
            commonObj.put("handlerName", condition.getDisplayName());
            commonObj.put("handlerType", condition.getHandler(formConditionModel));
            if (config != null) {
                commonObj.put("isMultiple", config.getBoolean("isMultiple"));
            }
            commonObj.put("conditionModel", condition.getHandler(formConditionModel));
            commonObj.put("type", condition.getType());
            commonObj.put("config", config);
            commonObj.put("sort", condition.getSort());
            commonObj.put("desc", condition.getDesc());
            ParamType paramType = condition.getParamType();
            if (paramType != null) {
                commonObj.put("defaultExpression", paramType.getDefaultExpression().getExpression());
                JSONArray expressionArray = new JSONArray();
                for (Expression expression : paramType.getExpressionList()) {
                    JSONObject expressionObj = new JSONObject();
                    expressionObj.put("expression", expression.getExpression());
                    expressionObj.put("expressionName", expression.getExpressionName());
                    expressionArray.add(expressionObj);
                    commonObj.put("expressionList", expressionArray);
                }
            }

            resultArray.add(commonObj);
        }
        resultArray.sort((o1, o2) -> {
            try {
                JSONObject obj1 = (JSONObject) o1;
                JSONObject obj2 = (JSONObject) o2;
                return obj1.getIntValue("sort") - obj2.getIntValue("sort");
            } catch (Exception ignored) {

            }
            return 0;
        });
        return resultArray;
    }

}
