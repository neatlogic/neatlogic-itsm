package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.SqlDecoratorVo;
import codedriver.framework.process.workcenter.dto.JoinOnVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskStepStatusCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    private String formHandlerType = FormHandlerType.SELECT.toString();

    @Override
    public String getName() {
        return "stepstatus";
    }

    @Override
    public String getDisplayName() {
        return "步骤状态";
    }

    @Override
    public String getHandler(FormConditionModel formConditionModel) {
        if (FormConditionModel.SIMPLE == formConditionModel) {
            formHandlerType = FormHandlerType.CHECKBOX.toString();
        } else {
            formHandlerType = FormHandlerType.SELECT.toString();
        }
        return formHandlerType;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        JSONArray dataList = new JSONArray();
        dataList.add(new ValueTextVo(ProcessTaskStatus.PENDING.getValue(), ProcessTaskStatus.PENDING.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStatus.RUNNING.getValue(), ProcessTaskStatus.RUNNING.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStatus.FAILED.getValue(), ProcessTaskStatus.FAILED.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStatus.SUCCEED.getValue(), ProcessTaskStatus.SUCCEED.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStatus.HANG.getValue(), ProcessTaskStatus.HANG.getText()));

        JSONObject config = new JSONObject();
        config.put("type", formHandlerType);
        config.put("search", false);
        config.put("multiple", true);
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("dataList", dataList);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 9;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof String) {
                String text = ProcessTaskStatus.getText(value.toString());
                if (text != null) {
                    return text;
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    String text = ProcessTaskStatus.getText(valueStr);
                    if (text != null) {
                        textList.add(text);
                    } else {
                        textList.add(valueStr);
                    }
                }
                return String.join("、", textList);
            }
        }
        return value;
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                }}));
            }
        };
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        getSimpleSqlConditionWhere(conditionList.get(index), sqlSb, new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue());
        sqlSb.append(" and ");
        sqlSb.append(Expression.getExpressionSql(Expression.EXCLUDE.getExpression(), new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.IS_ACTIVE.getValue(), "0','-1"));
    }
}
