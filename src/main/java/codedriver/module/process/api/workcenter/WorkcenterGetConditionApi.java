package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.*;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.condition.core.IConditionHandler;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.module.process.condition.handler.ProcessTaskIsShowCondition;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

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
        return "流程编辑获取条件接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "conditionModel", type = ApiParamType.STRING, desc = "条件模型 simple|custom,  simple:目前用于用于工单中心条件过滤简单模式, custom:目前用于用于工单中心条件过自定义模式;默认custom"),
            @Param(name = "workcenterUuid", type = ApiParamType.STRING, isRequired = true, desc = "draftProcessTask 非草稿的时候，过滤条件：工单状态去掉”未提交“"),
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "组件uuid"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
            @Param(name = "handlerName", type = ApiParamType.STRING, desc = "处理器名"),
            @Param(name = "handlerType", type = ApiParamType.STRING, desc = "控件类型 select|input|radio|userselect|date|area|time"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "类型  form|common"),
            @Param(name = "expressionList[0].expression", type = ApiParamType.STRING, desc = "表达式"),
            @Param(name = "expressionList[0].expressionName", type = ApiParamType.STRING, desc = "表达式名")
    })
    @Description(desc = "流程编辑获取条件接口，目前用于流程编辑，初始化条件使用")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultArray = new JSONArray();
        String conditionModel = jsonObj.getString("conditionModel");
        String workcenterUuid = jsonObj.getString("workcenterUuid");
        FormConditionModel formConditionModel = FormConditionModel.getFormConditionModel(conditionModel);
        formConditionModel = formConditionModel == null ? FormConditionModel.CUSTOM : formConditionModel;
        //固定字段条件
        for (IConditionHandler condition : ConditionHandlerFactory.getConditionHandlerList()) {
            //不支持endTime过滤，如果是简单模式 title、id、content 不返回
            //没有工单管理权限则不显示“是否隐藏工单”选项
            if ((condition.getName().equals(ProcessWorkcenterField.TITLE.getValue())
                    || condition.getName().equals(ProcessWorkcenterField.ID.getValue()) || condition.getName().equals(ProcessWorkcenterField.CONTENT.getValue()))
                    || condition.getName().equals(ProcessWorkcenterField.ENDTIME.getValue())
                    || condition.getName().equals(ProcessWorkcenterField.SERIAL_NUMBER.getValue())
                    || condition.getName().equals(ProcessWorkcenterField.STARTTIME.getValue())
                    || (Objects.equals(workcenterUuid, "draftProcessTask") && condition.getName().equals(ProcessWorkcenterField.EXPIRED_TIME.getValue()))
                    || ProcessWorkcenterField.getValue(condition.getName()) == null
                    || !(condition instanceof IProcessTaskCondition)
                    || (!AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName()) && (condition instanceof ProcessTaskIsShowCondition))
            ) {
                continue;
            }
            JSONObject config = condition.getConfig(ConditionConfigType.WORKCENTER);
            //非草稿的时候，过滤条件：工单状态去掉”未提交“
            if (condition.getName().equals(ProcessWorkcenterField.STATUS.getValue()) && !Objects.equals(workcenterUuid, "draftProcessTask")) {
                JSONArray dataArray = config.getJSONArray("dataList");
                config.put("dataList", dataArray.stream().filter(o -> !Objects.equals(ProcessTaskStatus.DRAFT.getValue(), ((ValueTextVo) o).getValue())).collect(Collectors.toList()));
            }
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
