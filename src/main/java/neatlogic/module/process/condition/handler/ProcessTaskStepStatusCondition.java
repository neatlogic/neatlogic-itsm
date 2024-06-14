package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
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
        dataList.add(new ValueTextVo(ProcessTaskStepStatus.PENDING.getValue(), ProcessTaskStepStatus.PENDING.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStepStatus.RUNNING.getValue(), ProcessTaskStepStatus.RUNNING.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStepStatus.FAILED.getValue(), ProcessTaskStepStatus.FAILED.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStepStatus.SUCCEED.getValue(), ProcessTaskStepStatus.SUCCEED.getText()));
        dataList.add(new ValueTextVo(ProcessTaskStepStatus.HANG.getValue(), ProcessTaskStepStatus.HANG.getText()));

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
                String text = ProcessTaskStepStatus.getText(value.toString());
                if (text != null) {
                    return text;
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    String text = ProcessTaskStepStatus.getText(valueStr);
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
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        getSimpleSqlConditionWhere(groupVo.getConditionList().get(index), sqlSb, new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue());
        sqlSb.append(" and ");
        sqlSb.append(Expression.getExpressionSql(Expression.EXCLUDE.getExpression(), new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.IS_ACTIVE.getValue(), "0','-1"));
    }
}
