package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSlaTimeSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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
    public String getHandler(String processWorkcenterConditionType) {
        if (ProcessConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
            formHandlerType = FormHandlerType.RADIO.toString();
        }
        return formHandlerType;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getMyEsName() {
        return String.format(" %s.%s", getType(), "expiretime.slaTimeVo.expireTimeLong");
    }

    @Override
    public JSONObject getConfig() {
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
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        String where = StringUtils.EMPTY;
        ConditionVo condition = conditionList.get(index);
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        Object value = valueList.get(0);
        if ("1".equals(value)) {
            // 超时单
            where = String.format(Expression.LESSTHAN.getExpressionEs(), this.getEsName(), System.currentTimeMillis());
            //+ " and "  +String.format(Expression.INCLUDE.getExpressionEs(),((IProcessTaskCondition)ConditionHandlerFactory.getHandler(ProcessWorkcenterField.STATUS.getValue())).getEsName(), "'running'");
        } else { // TODO es 封装暂时不支持 判断空key
            // where =
            // String.format(Expression.EQUAL.getExpressionEs(),ProcessWorkcenterField.getConditionValue(condition.getName())+".slaTimeVo.expireTimeLong",null)
            // + " or ";
            // where +=
            // String.format(Expression.LESSTHAN.getExpressionEs(),ProcessWorkcenterField.getConditionValue(condition.getName())+".slaTimeVo.expireTimeLong",System.currentTimeMillis());
        }

        return where;
    }

    @Override
    public String getMyEsOrderBy(String sortType) {
        String orderBy = StringUtils.EMPTY;
        if ("DESC".equalsIgnoreCase(sortType.trim())) {
            sortType = "ASC";
        } else {
            sortType = "DESC";
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = String.format(" %s %s ", this.getEsName(), sortType.toUpperCase());
        }
        return orderBy;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return null;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        ConditionVo condition = conditionList.get(index);
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        Object value = valueList.get(0);
        sqlSb.append(" ( ");
        sqlSb.append(Expression.getExpressionSql(Expression.INCLUDE.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), String.join("','", Collections.singletonList(ProcessTaskStatus.RUNNING.getValue()))));
        sqlSb.append(" and ");
        if ("1".equals(value)) {
            // 超时单
            sqlSb.append(Expression.getExpressionSql(Expression.LESSTHAN.getExpression(), new ProcessTaskSlaTimeSqlTable().getShortName(), ProcessTaskSlaTimeSqlTable.FieldEnum.EXPIRE_TIME.getValue(), TimeUtil.timeNow()));
        } else {
            sqlSb.append(Expression.getExpressionSql(Expression.GREATERTHAN.getExpression(), new ProcessTaskSlaTimeSqlTable().getShortName(), ProcessTaskSlaTimeSqlTable.FieldEnum.EXPIRE_TIME.getValue(), TimeUtil.timeNow()));
        }
        sqlSb.append(" ) ");
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return SqlTableUtil.getExpireTimeJoinTableSql();
    }
}
