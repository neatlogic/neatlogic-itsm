/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

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

import neatlogic.framework.util.$;
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
            sqlSb.append(Expression.getExpressionSql(Expression.EQUAL.getExpression(), new ProcessTaskStepAuditSqlTable().getShortName(), ProcessTaskStepCostSqlTable.FieldEnum.START_OPERATE.getValue(), ProcessTaskStepOperationType.STEP_TRANSFER.getValue()));
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
        return $.t("nmpch.processtaskaboutmecondition.getdisplayname");
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
        dataList.add(new ValueTextVo("doneOfMine", $.t("nmpch.processtaskaboutmecondition.runtime.label.1")));
        dataList.add(new ValueTextVo("focusOfMine", $.t("nmpch.processtaskaboutmecondition.runtime.label.2")));
        dataList.add(new ValueTextVo("needScoreOfMine", $.t("nmpch.processtaskaboutmecondition.runtime.label.3")));
        dataList.add(new ValueTextVo("scoredOfMine", $.t("nmpch.processtaskaboutmecondition.runtime.label.4")));
        dataList.add(new ValueTextVo("transferredOfMine", $.t("nmpch.processtaskaboutmecondition.runtime.label.5")));

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
                return $.t("nmpch.processtaskaboutmecondition.text.doneofmine");
            } else if (v.equalsIgnoreCase("focusOfMine")) {
                return $.t("nmpch.processtaskaboutmecondition.text.focusofmine");
            } else if (v.equalsIgnoreCase("needScoreOfMine")) {
                return $.t("nmpch.processtaskaboutmecondition.text.needscoreofmine");
            } else if (v.equalsIgnoreCase("scoredOfMine")) {
                return $.t("nmpch.processtaskaboutmecondition.text.scoredofmine");
            } else if (v.equalsIgnoreCase("transferredOfMine")) {
                return $.t("nmpch.processtaskaboutmecondition.text.transferredofmine");
            }
        } else if (value instanceof JSONArray) {
            List<String> textList = new ArrayList<>();
            JSONArray vList = (JSONArray) value;
            for (int i = 0; i < vList.size(); i++) {
                String v = vList.getString(i);
                if (v.equalsIgnoreCase("doneOfMine")) {
                    textList.add($.t("nmpch.processtaskaboutmecondition.text.doneofmine"));
                } else if (v.equalsIgnoreCase("focusOfMine")) {
                    textList.add($.t("nmpch.processtaskaboutmecondition.text.focusofmine"));
                } else if (v.equalsIgnoreCase("needScoreOfMine")) {
                    textList.add($.t("nmpch.processtaskaboutmecondition.text.needscoreofmine"));
                } else if (v.equalsIgnoreCase("scoredOfMine")) {
                    textList.add($.t("nmpch.processtaskaboutmecondition.text.scoredofmine"));
                } else if (v.equalsIgnoreCase("transferredOfMine")) {
                    textList.add($.t("nmpch.processtaskaboutmecondition.text.transferredofmine"));
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
