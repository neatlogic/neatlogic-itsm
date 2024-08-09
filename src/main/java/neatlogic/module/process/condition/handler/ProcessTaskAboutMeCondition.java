/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskAboutMeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    private String formHandlerType = FormHandlerType.SELECT.toString();

    private final Map<String, MyProcessTask<StringBuilder>> mapSql = new HashMap<>();

    private final Map<String, MyProcessTask<List<JoinTableColumnVo>>> joinTableSqlMap = new HashMap<>();

    @FunctionalInterface
    public interface MyProcessTask<T> {
        void build(T t);
    }

    {

        mapSql.put("doneOfMine", (sqlSb) -> {
            sqlSb.append(" ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.INCLUDE.getExpression(), new ProcessTaskStepUserSqlTable().getShortName(), ProcessTaskStepUserSqlTable.FieldEnum.STATUS.getValue(), String.format("%s','%s", ProcessTaskStepUserStatus.DONE.getValue(), ProcessTaskStepUserStatus.TRANSFERRED.getValue())));
            sqlSb.append(" ) and ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskStepUserSqlTable().getShortName(), ProcessTaskStepUserSqlTable.FieldEnum.USER_UUID.getValue(), UserContext.get().getUserUuid(true)));
            sqlSb.append(" ) ");
        });
        mapSql.put("focusOfMine", (sqlSb) -> {
            sqlSb.append(" ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskFocusSqlTable().getShortName(), ProcessTaskFocusSqlTable.FieldEnum.USER_UUID.getValue(), UserContext.get().getUserUuid(true)));
            sqlSb.append(" ) ");
        });
        mapSql.put("needScoreOfMine", (sqlSb) -> {
            sqlSb.append(" ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.OWNER.getValue(), UserContext.get().getUserUuid(true)));
            sqlSb.append(" ) and ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.NEED_SCORE.getValue(), "1"));
            sqlSb.append(" ) and ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.SUCCEED.getValue()));
            sqlSb.append(" ) ");
        });

        mapSql.put("scoredOfMine", (sqlSb) -> {
            sqlSb.append(" ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.OWNER.getValue(), UserContext.get().getUserUuid(true)));
            sqlSb.append(" ) and ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.SCORED.getValue()));
            sqlSb.append(" ) ");
        });

        mapSql.put("transferredOfMine", (sqlSb) -> {
            sqlSb.append(" ( ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskStepAuditSqlTable().getShortName(), ProcessTaskStepCostSqlTable.FieldEnum.START_OPERATE.getValue(), ProcessTaskOperationType.STEP_TRANSFER.getValue()));
            sqlSb.append(" and ");
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskStepAuditSqlTable().getShortName(), ProcessTaskStepCostSqlTable.FieldEnum.START_USER_UUID.getValue(), UserContext.get().getUserUuid(true)));
            sqlSb.append(" ) ");
        });

        joinTableSqlMap.put("doneOfMine", (list) -> {
            list.add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
            }}));
            list.add(new JoinTableColumnVo(new ProcessTaskStepSqlTable(), new ProcessTaskStepUserSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue(), ProcessTaskStepUserSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepUserSqlTable.FieldEnum.PROCESSTASK_STEP_ID.getValue()));
            }}));
        });
        joinTableSqlMap.put("focusOfMine", (list) -> {
            list.add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskFocusSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskFocusSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
            }}));
        });
        joinTableSqlMap.put("transferredOfMine", (list) -> {
            list.add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepCostSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepCostSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
            }}));
        });
    }

    @Override
    public String getName() {
        return "aboutme";
    }

    @Override
    public String getDisplayName() {
        return "与我相关";
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
        dataList.add(new ValueTextVo("doneOfMine", "已办"));
        dataList.add(new ValueTextVo("focusOfMine", "已关注"));
        dataList.add(new ValueTextVo("needScoreOfMine", "待评分"));
        dataList.add(new ValueTextVo("scoredOfMine", "已评分"));
        dataList.add(new ValueTextVo("transferredOfMine", "已转交"));

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
        return 6;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }


    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value instanceof String) {
            String v = value.toString();
            if (v.equalsIgnoreCase("doneOfMine")) {
                return "已办";
            } else if (v.equalsIgnoreCase("focusOfMine")) {
                return "已关注";
            } else if (v.equalsIgnoreCase("needScoreOfMine")) {
                return "待评分";
            } else if (v.equalsIgnoreCase("scoredOfMine")) {
                return "已评分";
            } else if (v.equalsIgnoreCase("transferredOfMine")) {
                return "已转交";
            }
        } else if (value instanceof JSONArray) {
            List<String> textList = new ArrayList<>();
            JSONArray vList = (JSONArray) value;
            for (int i = 0; i < vList.size(); i++) {
                String v = vList.getString(i);
                if (v.equalsIgnoreCase("doneOfMine")) {
                    textList.add("已办");
                } else if (v.equalsIgnoreCase("focusOfMine")) {
                    textList.add("已关注");
                } else if (v.equalsIgnoreCase("needScoreOfMine")) {
                    textList.add("待评分");
                } else if (v.equalsIgnoreCase("scoredOfMine")) {
                    textList.add("已评分");
                } else if (v.equalsIgnoreCase("transferredOfMine")) {
                    textList.add("已转交");
                }
            }
            return String.join("、", textList);
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        sqlSb.append(" ( ");
        ConditionVo condition = groupVo.getConditionList().get(index);
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        int i = 0;
        for (String value : valueList) {
            MyProcessTask<StringBuilder> result = mapSql.get(value);
            if (result != null) {
                // 拼接条件
                if (i != 0) {
                    sqlSb.append(" or ");
                }
                result.build(sqlSb);

            }
            i++;
        }
        sqlSb.append(" ) ");
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        List<JoinTableColumnVo> joinTableColumnVoList = new ArrayList<>();
        for (ConditionGroupVo conditionGroupVo : sqlDecoratorVo.getConditionGroupList()) {
            for (ConditionVo conditionVo : conditionGroupVo.getConditionList()) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(conditionVo.getValueList()), String.class);
                for (String value : valueList) {
                    MyProcessTask<List<JoinTableColumnVo>> result = joinTableSqlMap.get(value);
                    if (result != null) {
                        result.build(joinTableColumnVoList);
                    }
                }
            }
        }
        return joinTableColumnVoList;
    }
}
