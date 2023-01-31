package neatlogic.module.process.api.process;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.ExpressionVo;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessConditionList extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "process/condition/list";
    }

    @Override
    public String getName() {
        return "流程条件列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "formUuid", type = ApiParamType.STRING, desc = "流程绑定表单的uuid")})
    @Output({@Param(explode = ConditionParamVo[].class, desc = "流程条件列表")})
    @Description(desc = "流程条件列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultArray = new JSONArray();
        // 固定字段条件
        for (ConditionProcessTaskOptions option : ConditionProcessTaskOptions.values()) {
            IConditionHandler condition = ConditionHandlerFactory.getHandler(option.getValue());
            if (condition != null) {
                ConditionParamVo conditionParamVo = new ConditionParamVo();
                conditionParamVo.setName(condition.getName());
                conditionParamVo.setLabel(condition.getDisplayName());
                conditionParamVo.setController(condition.getHandler(FormConditionModel.CUSTOM));
                if (condition.getConfig() != null) {
                    conditionParamVo.setConfig(condition.getConfig().toJSONString());
                }
                conditionParamVo.setType(condition.getType());
                ParamType paramType = condition.getParamType();
                if (paramType != null) {
                    conditionParamVo.setParamType(paramType.getName());
                    conditionParamVo.setParamTypeName(paramType.getText());
                }
                Expression expression = condition.getExpression();
                if (expression != null) {
                    conditionParamVo.setDefaultExpression(expression.getExpression());
                }
                List<Expression> expressionList = condition.getExpressionList();
                if (CollectionUtils.isNotEmpty(expressionList)) {
                    for (Expression exp : expressionList) {
                        conditionParamVo.getExpressionList().add(new ExpressionVo(exp));
                    }
                }
                resultArray.add(conditionParamVo);
            }
        }

        // 表单条件
        String formUuid = jsonObj.getString("formUuid");
        if (StringUtils.isNotBlank(formUuid)) {
            List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
            for (FormAttributeVo formAttributeVo : formAttrList) {

                IFormAttributeHandler formHandler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if(formHandler == null){
                    continue;
                }
                if (formHandler.isConditionable()) {
                    formAttributeVo.setType("form");
                    formAttributeVo.setConditionModel(FormConditionModel.CUSTOM);
                    ConditionParamVo conditionParamVo = new ConditionParamVo();
                    conditionParamVo.setName(formAttributeVo.getUuid());
                    conditionParamVo.setLabel(formAttributeVo.getLabel());
                    conditionParamVo.setController(formAttributeVo.getHandlerType());
                    conditionParamVo.setType(formAttributeVo.getType());
                    conditionParamVo.setHandler(formAttributeVo.getHandler());
                    conditionParamVo.setConfig(formAttributeVo.getConfig());
                    if ("formdate".equals(formAttributeVo.getHandler())) {
                        JSONObject config = conditionParamVo.getConfig();
                        if (MapUtils.isNotEmpty(config)) {
                            config.put("type", "datetimerange");
                            conditionParamVo.setConfig(config.toJSONString());
                        }
                    } else if ("formtime".equals(formAttributeVo.getHandler())) {
                        JSONObject config = conditionParamVo.getConfig();
                        if (MapUtils.isNotEmpty(config)) {
                            config.put("type", "timerange");
                            conditionParamVo.setConfig(config.toJSONString());
                        }
                    }
                    ParamType paramType = formHandler.getParamType();
                    if (paramType != null) {
                        conditionParamVo.setParamType(paramType.getName());
                        conditionParamVo.setParamTypeName(paramType.getText());
                        Expression expression = paramType.getDefaultExpression();
                        if (expression != null) {
                            conditionParamVo.setDefaultExpression(expression.getExpression());
                        }
                        if (CollectionUtils.isNotEmpty(paramType.getExpressionList())) {
                            for (Expression exp : paramType.getExpressionList()) {
                                conditionParamVo.getExpressionList().add(new ExpressionVo(exp));
                            }
                        }
                    }
                    resultArray.add(conditionParamVo);
                }
            }
        }
        return resultArray;
    }

}
