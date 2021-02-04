package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dto.condition.ConditionGroupRelVo;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionRelVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.condition.handler.ProcessTaskStartTimeCondition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: SqlWhereDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:39
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlWhereDecorator extends SqlDecoratorBase {
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        // 将group 以连接表达式 存 Map<fromUuid_toUuid,joinType>
        Map<String, String> groupRelMap = new HashMap<String, String>();
        List<ConditionGroupRelVo> groupRelList = workcenterVo.getConditionGroupRelList();
        if (CollectionUtils.isNotEmpty(groupRelList)) {
            for (ConditionGroupRelVo groupRel : groupRelList) {
                groupRelMap.put(groupRel.getFrom() + "_" + groupRel.getTo(), groupRel.getJoinType());
            }
        }
        sqlSb.append(" where ");
        //上报时间
        ProcessTaskStartTimeCondition startTimeSqlCondition = (ProcessTaskStartTimeCondition) ConditionHandlerFactory.getHandler("starttime");
        startTimeSqlCondition.getDateSqlWhere(workcenterVo.getStartTimeCondition(),sqlSb,new ProcessTaskSqlTable().getShortName(),ProcessTaskSqlTable.FieldEnum.START_TIME.getValue());
        //我的待办
        if (workcenterVo.getIsProcessingOfMine() == 1) {
            sqlSb.append(" and ");
            IProcessTaskCondition sqlCondition = (IProcessTaskCondition) ConditionHandlerFactory.getHandler("processingofmine");
            sqlCondition.getSqlConditionWhere(null, 0, sqlSb);
        }
        //如果是count , distinct id 则 需要 根据条件获取需要的表。否则需要根据column获取需要的表
        if (!FieldTypeEnum.FIELD.getValue().equals(workcenterVo.getSqlFieldType())) {
            List<ISqlTable> tableList = new ArrayList<>();
            List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
            if(CollectionUtils.isNotEmpty(groupList)) {
                String fromGroupUuid = null;
                String toGroupUuid = groupList.get(0).getUuid();
                boolean isAddedAnd = false;
                for (ConditionGroupVo groupVo : groupList) {
                    // 将condition 以连接表达式 存 Map<fromUuid_toUuid,joinType>
                    Map<String, String> conditionRelMap = new HashMap<String, String>();
                    List<ConditionRelVo> conditionRelList = groupVo.getConditionRelList();
                    if (CollectionUtils.isNotEmpty(conditionRelList)) {
                        for (ConditionRelVo conditionRel : conditionRelList) {
                            conditionRelMap.put(conditionRel.getFrom() + "_" + conditionRel.getTo(),
                                    conditionRel.getJoinType());
                        }
                    }
                    //append joinType
                    if (fromGroupUuid != null) {
                        toGroupUuid = groupVo.getUuid();
                        sqlSb.append(groupRelMap.get(fromGroupUuid + "_" + toGroupUuid));
                    }
                    List<ConditionVo> conditionVoList = groupVo.getConditionList();
                    if (!isAddedAnd && CollectionUtils.isNotEmpty((conditionVoList))) {
                        sqlSb.append(" and ");
                        isAddedAnd = true;
                    }
                    sqlSb.append(" ( ");
                    String fromConditionUuid = null;
                    String toConditionUuid = null;
                    for (int i = 0; i < conditionVoList.size(); i++) {
                        ConditionVo conditionVo = conditionVoList.get(i);
                        //append joinType
                        toConditionUuid = conditionVo.getUuid();
                        if (MapUtils.isNotEmpty(conditionRelMap) && StringUtils.isNotBlank(fromConditionUuid)) {
                            sqlSb.append(conditionRelMap.get(fromConditionUuid + "_" + toConditionUuid));
                        }
                        IProcessTaskCondition sqlCondition = (IProcessTaskCondition) ConditionHandlerFactory.getHandler(conditionVo.getName());
                        sqlCondition.getSqlConditionWhere(conditionVoList, i, sqlSb);
                        fromConditionUuid = toConditionUuid;
                    }
                    sqlSb.append(" ) ");
                    fromGroupUuid = toGroupUuid;

                }
            }
        } else {
            sqlSb.append(String.format(" and %s.%s in ( '%s' ) ", new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.ID.getValue(), workcenterVo.getProcessTaskIdList().stream().map(Object::toString).collect(Collectors.joining("','"))));
        }
    }

    @Override
    public int getSort() {
        return 4;
    }
}
