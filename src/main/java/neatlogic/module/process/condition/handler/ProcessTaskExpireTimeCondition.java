package neatlogic.module.process.condition.handler;

import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.*;
import neatlogic.framework.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        return 13;
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
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        ConditionVo condition = groupVo.getConditionList().get(index);
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
                add(new JoinTableColumnVo(new ProcessTaskStepSqlTable(), new ProcessTaskStepSlaSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSlaSqlTable.FieldEnum.PROCESSTASK_STEP_ID.getValue()));
                    add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.RUNNING.getValue(), true));
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.RUNNING.getValue(), true, new ProcessTaskSqlTable().getShortName()));
                }}));
                add(new JoinTableColumnVo(new ProcessTaskStepSlaSqlTable(), new ProcessTaskSlaTimeSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskStepSlaSqlTable.FieldEnum.SLA_ID.getValue(), ProcessTaskSlaTimeSqlTable.FieldEnum.SLA_ID.getValue()));
                }}));
            }
        };
    }

    @Override
    public boolean isShow(JSONObject jsonObj, String type) {
        if(Objects.equals(type, ProcessTaskConditionType.WORKCENTER.getValue())){
            String workcenterUuid = jsonObj.getString("workcenterUuid");
            return !Objects.equals(workcenterUuid, ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getValue());
        }
        return true;
    }
}
