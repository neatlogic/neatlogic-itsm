package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.ExpressionVo;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessConditionList extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "process/condition/list";
    }

    @Override
    public String getName() {
        return "nmpap.processconditionlist.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "formUuid", type = ApiParamType.STRING, desc = "term.framework.formuuid"),
            @Param(name = "isAll", type = ApiParamType.INTEGER, rule = "0,1", desc = "term.process.isreturnallattr")})
    @Output({@Param(explode = ConditionParamVo[].class, desc = "nmpap.processconditionlist.getname")})
    @Description(desc = "nmpap.processconditionlist.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultArray = new JSONArray();
        Integer isAll = jsonObj.getInteger("isAll");
        // 固定字段条件
        for (ConditionProcessTaskOptions option : ConditionProcessTaskOptions.values()) {
            IConditionHandler condition = ConditionHandlerFactory.getHandler(option.getValue());
            if (condition != null) {
                ConditionParamVo conditionParamVo = new ConditionParamVo();
                conditionParamVo.setName(condition.getName());
                conditionParamVo.setLabel(condition.getDisplayName());
                conditionParamVo.setController(condition.getHandler(FormConditionModel.CUSTOM));
                conditionParamVo.setHandler("form" + conditionParamVo.getController());
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
                conditionParamVo.setConditionable(true);
                resultArray.add(conditionParamVo);
            }
        }

        // 表单条件
        String formUuid = jsonObj.getString("formUuid");
        if (StringUtils.isNotBlank(formUuid)) {
            //List<String> conditionableAttrUuidList = new ArrayList<>();
            FormVo form = formMapper.getFormByUuid(formUuid);
            if (form == null) {
                throw new FormNotFoundException(formUuid);
            }
            // TODO 需要确定条件节点表单扩展属性标签
//            IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
//            List<FormAttributeVo> formAttributeList = formCrossoverService.getFormAttributeList(formUuid, form.getName(), "condition");
            List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
            for (FormAttributeVo formAttributeVo : formAttrList) {
                IFormAttributeHandler formHandler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (formHandler == null) {
                    continue;
                }
                if ((isAll != null && isAll.equals(1)) || formHandler.isConditionable()) {
                    formAttributeVo.setType("form");
                    formAttributeVo.setConditionModel(FormConditionModel.CUSTOM);

                    ConditionParamVo conditionParamVo = new ConditionParamVo();
                    conditionParamVo.setName(formAttributeVo.getUuid());
                    conditionParamVo.setLabel(formAttributeVo.getLabel());
                    conditionParamVo.setController(formAttributeVo.getHandlerType());
                    conditionParamVo.setType(formAttributeVo.getType());
                    conditionParamVo.setHandler(formAttributeVo.getHandler());
                    conditionParamVo.setConfig(formAttributeVo.getConfig().toJSONString());

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
                    conditionParamVo.setConditionable(formHandler.isConditionable());
                    resultArray.add(conditionParamVo);
                }
            }
        }
        return resultArray;
    }
}
