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
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.util.$;
@Component
public class ProcessTaskProcessStepCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Resource
    private ProcessMapper processMapper;

    @Override
    public String getName() {
        return "processStep";
    }

    @Override
    public String getDisplayName() {
        return $.t("nmpch.processtaskprocessstepcondition.getdisplayname");
    }

    @Override
    public String getDesc() {
        return $.t("nmpch.processtaskprocessstepcondition.getdesc");
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return null;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        return null;
    }

    @Override
    public Integer getSort() {
        return null;
    }

    @Override
    public ParamType getParamType() {
        return null;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof String) {
                ProcessStepVo processStepVo = processMapper.getProcessStepByUuid(value.toString());
                if (processStepVo != null) {
                    return processStepVo.getName();
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    ProcessStepVo processStepVo = processMapper.getProcessStepByUuid(valueStr);
                    if (processStepVo != null) {
                        textList.add(processStepVo.getName());
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
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        ConditionVo conditionVo = groupVo.getConditionList().get(index);
        //补充服务目录条件
        List<String> channelUuidList = groupVo.getChannelUuidList();
        if (CollectionUtils.isEmpty(channelUuidList)) {
            throw new ParamIrregularException("ConditionGroup", " lost channelUuidList");
        }
        ConditionVo channelCondition = new ConditionVo();
        channelCondition.setName("channel");
        channelCondition.setLabel($.t("term.itsm.channel"));
        channelCondition.setType("common");
        channelCondition.setExpression(Expression.INCLUDE.getExpression());
        channelCondition.setValueList(channelUuidList);
        getSimpleSqlConditionWhere(channelCondition, sqlSb, new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.CHANNEL_UUID.getValue());
        sqlSb.append(" and ");
        getSimpleSqlConditionWhere(conditionVo, sqlSb, new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.PROCESS_STEP_UUID.getValue());
        sqlSb.append(" and ");
        sqlSb.append(Expression.getExpressionSql(Expression.EXCLUDE.getExpression(), new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.IS_ACTIVE.getValue(), "0"));
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

}
