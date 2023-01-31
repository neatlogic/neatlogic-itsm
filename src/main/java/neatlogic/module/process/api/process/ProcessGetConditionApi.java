package neatlogic.module.process.api.process;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.constvalue.ProcessField;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

// @Service
// @Transactional
// @OperationType(type = OperationTypeEnum.SEARCH)
@Deprecated
public class ProcessGetConditionApi extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "process/condition/get";
    }

    @Override
    public String getName() {
        return "流程编辑获取条件接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "formUuid", type = ApiParamType.STRING, desc = "流程绑定表单的uuid")})
    @Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "组件uuid"),
        @Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
        @Param(name = "handlerName", type = ApiParamType.STRING, desc = "处理器名"),
        @Param(name = "handlerType", type = ApiParamType.STRING,
            desc = "控件类型 select|input|radio|userselect|date|area|time"),
        @Param(name = "type", type = ApiParamType.STRING, desc = "类型  form|common"),
        @Param(name = "expressionList[0].expression", type = ApiParamType.STRING, desc = "表达式"),
        @Param(name = "expressionList[0].expressionName", type = ApiParamType.STRING, desc = "表达式名")})
    @Description(desc = "流程编辑获取条件接口，目前用于流程编辑，初始化条件使用")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultArray = new JSONArray();
        // 固定字段条件
        for (IConditionHandler condition : ConditionHandlerFactory.getConditionHandlerList()) {
            if (condition instanceof IProcessTaskCondition && ProcessField.getValue(condition.getName()) != null) {
                JSONObject commonObj = new JSONObject();
                commonObj.put("handler", condition.getName());
                commonObj.put("handlerName", condition.getDisplayName());
                commonObj.put("handlerType", condition.getHandler(FormConditionModel.CUSTOM));
                if (condition.getConfig() != null) {
                    commonObj.put("isMultiple", condition.getConfig().getBoolean("isMultiple"));
                    commonObj.put("config", condition.getConfig().toJSONString());
                }
                commonObj.put("type", condition.getType());
                ParamType paramType = condition.getParamType();
                if (paramType != null) {
                    commonObj.put("basicType", paramType.getName());
                    commonObj.put("basicTypeName", paramType.getText());
                    commonObj.put("defaultExpression", paramType.getDefaultExpression().getExpression());
                    JSONArray expressiobArray = new JSONArray();
                    for (Expression expression : paramType.getExpressionList()) {
                        JSONObject expressionObj = new JSONObject();
                        expressionObj.put("expression", expression.getExpression());
                        expressionObj.put("expressionName", expression.getExpressionName());
                        expressiobArray.add(expressionObj);
                        commonObj.put("expressionList", expressiobArray);
                    }
                }
                resultArray.add(commonObj);
            }
        }
        // 表单条件
        if (jsonObj.containsKey("formUuid") && !StringUtils.isBlank(jsonObj.getString("formUuid"))) {
            String formUuid = jsonObj.getString("formUuid");
            List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
            for (FormAttributeVo formAttributeVo : formAttrList) {
                IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (handler != null && !handler.isConditionable()) {
                    continue;
                }
                formAttributeVo.setType("form");
                formAttributeVo.setConditionModel(FormConditionModel.CUSTOM);
                resultArray.add(JSONObject.toJSON(formAttributeVo));
            }
        }
        return resultArray;
    }

}
