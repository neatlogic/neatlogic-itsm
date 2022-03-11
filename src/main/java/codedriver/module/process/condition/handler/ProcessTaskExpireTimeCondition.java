package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.SqlDecoratorVo;
import codedriver.framework.process.workcenter.dto.JoinOnVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.table.*;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessTaskExpireTimeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    private String formHandlerType = FormHandlerType.SELECT.toString();

    @Override
    public String getName() {
        return "expiretime";
    }

    @Override
    public String getDisplayName() {
        return "是否超时";
    }

    @Override
    public String getHandler(FormConditionModel formConditionModel) {
        if (FormConditionModel.SIMPLE == formConditionModel) {
            formHandlerType = FormHandlerType.RADIO.toString();
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
        dataList.add(new ValueTextVo("1", "是"));
        // TODO es 封装暂时不支持 判断空key
        // dataList.add(new ValueTextVo("0", "否"));

        JSONObject config = new JSONObject();
        config.put("type", formHandlerType);
        config.put("search", false);
        config.put("multiple", false);
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("dataList", dataList);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", false);
        return config;
    }

    @Override
    public Integer getSort() {
        return 10;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return null;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        ConditionVo condition = conditionList.get(index);
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        if(CollectionUtils.isEmpty(valueList)) {
            throw new ParamIrregularException("expiretime");
        }
        Object value = valueList.get(0);
        sqlSb.append(" ( ");
        sqlSb.append(Expression.getExpressionSql(Expression.INCLUDE.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), String.join("','", Collections.singletonList(ProcessTaskStatus.RUNNING.getValue()))));
        sqlSb.append(" and ");
        if ("1".equals(value)) {
            // 超时单
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskStepSqlTable().getShortName(),ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue(),ProcessTaskStatus.RUNNING.getValue()));
            sqlSb.append(" and ");
            sqlSb.append(Expression.getExpressionSql(Expression.LESSTHAN.getExpression(), new ProcessTaskSlaTimeSqlTable().getShortName(), ProcessTaskSlaTimeSqlTable.FieldEnum.EXPIRE_TIME.getValue(), TimeUtil.timeNow()));
        } else {
            sqlSb.append(Expression.getExpressionSql(Expression.GREATERTHAN.getExpression(), new ProcessTaskSlaTimeSqlTable().getShortName(), ProcessTaskSlaTimeSqlTable.FieldEnum.EXPIRE_TIME.getValue(), TimeUtil.timeNow()));
        }
        sqlSb.append(" ) ");

    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                }}));
                add(new JoinTableColumnVo(new ProcessTaskStepSqlTable(), new ProcessTaskSlaSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue(), ProcessTaskSlaSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                }}));
                add(new JoinTableColumnVo(new ProcessTaskStepSqlTable(), new ProcessTaskStepSlaSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSlaSqlTable.FieldEnum.PROCESSTASK_STEP_ID.getValue()));
                    add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.RUNNING.getValue(), true));
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.RUNNING.getValue(), true, new ProcessTaskSqlTable().getShortName()));
                }}));
                add(new JoinTableColumnVo(new ProcessTaskSlaSqlTable(), new ProcessTaskSlaTimeSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSlaSqlTable.FieldEnum.ID.getValue(), ProcessTaskSlaTimeSqlTable.FieldEnum.SLA_ID.getValue()));
                }}));
            }
        };
    }
}
