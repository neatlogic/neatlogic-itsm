package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.workcenter.dto.*;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @Title: SqlColumnDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:38
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlFromJoinDecorator extends SqlDecoratorBase {

    private final Map<String, SqlFromJoinDecorator.BuildFromJoin<StringBuilder, WorkcenterVo, List<String>, List<JoinTableColumnVo>>> buildFromJoinMap = new HashMap<>();

    @FunctionalInterface
    public interface BuildFromJoin<sb, T, List, joinTableColumnList> {
        void build(StringBuilder sb, T t, List joinTableKeyList, joinTableColumnList joinTableColumnList);
    }

    /**
     * @Description: 初始化构建sql where
     * @Author: 89770
     * @Date: 2021/1/19 14:35
     * @Params: []
     * @Returns: void
     **/
    @PostConstruct
    public void fieldDispatcherInit() {
        //根据column获取需要的表
        buildFromJoinMap.put(FieldTypeEnum.FIELD.getValue(), this::getJoinTableOfColumn);

        buildFromJoinMap.put(FieldTypeEnum.LIMIT_COUNT.getValue(), this::buildJoinTableOfConditionSql);
        //如果是distinct id 则 只需要 根据条件获取需要的表
        buildFromJoinMap.put(FieldTypeEnum.DISTINCT_ID.getValue(), this::buildJoinTableOfConditionSql);

        buildFromJoinMap.put(FieldTypeEnum.TOTAL_COUNT.getValue(), this::buildJoinTableOfConditionSql);

        buildFromJoinMap.put(FieldTypeEnum.FULL_TEXT.getValue(), this::buildJoinTableOfConditionSql);

        buildFromJoinMap.put(FieldTypeEnum.GROUP_COUNT.getValue(), this::getJoinTableOfGroupColumn);

        buildFromJoinMap.put(FieldTypeEnum.SUB_GROUP_COUNT.getValue(), this::getJoinTableOfSubGroupColumn);

        buildFromJoinMap.put(FieldTypeEnum.GROUP_SUM.getValue(), this::getJoinTableOfGroupSum);
    }

    /**
     * @Description: 构建回显字段sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:53
     * @Params: []
     * @Returns: java.lang.String
     **/
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        List<String> joinTableKeyList = new ArrayList<>();
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
        if (buildFromJoinMap.containsKey(workcenterVo.getSqlFieldType())) {
            buildFromJoinMap.get(workcenterVo.getSqlFieldType()).build(sqlSb, workcenterVo, joinTableKeyList, joinTableColumnList);
        }
    }

    /**
     * @Description: 补充排序需要 join 的表
     * @Author: 89770
     * @Date: 2021/1/26 20:36
     * @Params: [workcenterVo, joinTableKeyList]
     * @Returns: java.util.List<codedriver.framework.process.workcenter.dto.JoinTableColumnVo>
     **/
    private void getJoinTableOfOrder(WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        JSONArray sortJsonArray = workcenterVo.getSortList();
        if (CollectionUtils.isNotEmpty(sortJsonArray)) {
            for (Object sortObj : sortJsonArray) {
                JSONObject sortJson = JSONObject.parseObject(sortObj.toString());
                for (Map.Entry<String, Object> entry : sortJson.entrySet()) {
                    String handler = entry.getKey();
                    IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(handler);
                    if (column != null && column.getIsSort()) {
                        List<JoinTableColumnVo> handlerJoinTableColumnList = column.getJoinTableColumnList();
                        for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                            String key = handlerJoinTableColumn.getHash();
                            if (!joinTableKeyList.contains(handlerJoinTableColumn.getHash())) {
                                joinTableColumnList.add(handlerJoinTableColumn);
                                joinTableKeyList.add(key);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @Description: 根据条件获取需要的表
     * @Author: 89770
     * @Date: 2021/1/20 16:36
     * @Params: []
     * @Returns: void
     **/
    private void buildJoinTableOfConditionSql(StringBuilder sb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        getJoinTableOfCondition(sb, workcenterVo, joinTableKeyList, joinTableColumnList);
        buildFromJoinSql(sb, workcenterVo, joinTableKeyList, joinTableColumnList);
    }

    /**
     * @Description: 如果是distinct id 则 只需要 根据条件获取需要的表
     * @Author: 89770
     * @Date: 2021/1/20 16:36
     * @Params: []
     * @Returns: void
     **/
    private void getJoinTableOfCondition(StringBuilder sb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        //根据接口入参的返回需要的conditionList,然后获取需要关联的tableList
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        for (ConditionGroupVo groupVo : groupList) {
            List<ConditionVo> conditionVoList = groupVo.getConditionList();
            for (ConditionVo conditionVo : conditionVoList) {
                IProcessTaskCondition conditionHandler = (IProcessTaskCondition) ConditionHandlerFactory.getHandler(conditionVo.getName());
                if (conditionHandler != null) {
                    List<JoinTableColumnVo> handlerJoinTableColumnList = conditionHandler.getJoinTableColumnList(workcenterVo);
                    for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                        String key = handlerJoinTableColumn.getHash();
                        if (!joinTableKeyList.contains(key)) {
                            joinTableColumnList.add(handlerJoinTableColumn);
                            joinTableKeyList.add(key);
                        }
                    }
                }
            }
        }
        //我的待办 条件
        if (workcenterVo.getIsProcessingOfMine() == 1) {
            List<JoinTableColumnVo> handlerJoinTableColumnList = SqlTableUtil.getProcessingOfMineJoinTableSql();
            for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                String key = handlerJoinTableColumn.getHash();
                if (!joinTableKeyList.contains(key)) {
                    joinTableColumnList.add(handlerJoinTableColumn);
                    joinTableKeyList.add(key);
                }
            }
        }
    }

    private void getJoinTableOfColumn(StringBuilder sb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        //根据接口入参的返回需要的columnList,然后获取需要关联的tableList
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //循环所有需要展示的字段
        if (CollectionUtils.isNotEmpty(workcenterVo.getTheadVoList())) {
            for (WorkcenterTheadVo theadVo : workcenterVo.getTheadVoList()) {
                //去掉沒有勾选的thead
                if (theadVo.getIsShow() != 1) {
                    continue;
                }
                if (columnComponentMap.containsKey(theadVo.getName())) {
                    getJoinTableColumnList(columnComponentMap, theadVo.getName(), joinTableKeyList, joinTableColumnList);
                }
            }
        }
        buildFromJoinSql(sb, workcenterVo, joinTableKeyList, joinTableColumnList);
    }

    /**
     * 获取分组 join 的字段
     *
     * @param workcenterVo        工单中心参数
     * @param joinTableKeyList    join 表key列表 防止重复join表
     * @param joinTableColumnList join 表字段列表
     */
    private void getJoinTableOfGroupColumn(StringBuilder sb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        getJoinTableOfGroupColumnCommon(sb, workcenterVo, joinTableKeyList, joinTableColumnList, false);
    }

    /**
     * 获取二级分组 join 的字段
     *
     * @param workcenterVo        工单中心参数
     * @param joinTableKeyList    join 表key列表 防止重复join表
     * @param joinTableColumnList join 表字段列表
     */
    private void getJoinTableOfSubGroupColumn(StringBuilder sb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        getJoinTableOfGroupColumnCommon(sb, workcenterVo, joinTableKeyList, joinTableColumnList, true);
    }

    /**
     * 获取二级分组 join 的字段
     *
     * @param workcenterVo        工单中心参数
     * @param joinTableKeyList    join 表key列表 防止重复join表
     * @param joinTableColumnList join 表字段列表
     */
    private void getJoinTableOfGroupColumnCommon(StringBuilder sb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList, boolean isSubGroup) {
        //先根据条件补充join table
        getJoinTableOfCondition(sb, workcenterVo, joinTableKeyList, joinTableColumnList);
        //根据接口入参的返回需要的columnList,然后获取需要关联的tableList
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //循环所有需要展示的字段
        List<String> groupList = new ArrayList<>();
        groupList.add(workcenterVo.getDashboardWidgetChartConfigVo().getGroup());
        if (isSubGroup) {
            groupList.add(workcenterVo.getDashboardWidgetChartConfigVo().getSubGroup());
        }
        if (CollectionUtils.isNotEmpty(groupList)) {//group by 需要join的表
            for (String group : groupList) {
                getJoinTableColumnList(columnComponentMap, group, joinTableKeyList, joinTableColumnList);
            }
        }

        buildFromJoinSql(sb, workcenterVo, joinTableKeyList, joinTableColumnList);
    }

    private void getJoinTableColumnList(Map<String, IProcessTaskColumn> columnComponentMap, String columnName, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        IProcessTaskColumn column = columnComponentMap.get(columnName);
        List<JoinTableColumnVo> handlerJoinTableColumnList = column.getJoinTableColumnList();
        for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
            String key = handlerJoinTableColumn.getHash();
            if (!joinTableKeyList.contains(key)) {
                joinTableColumnList.add(handlerJoinTableColumn);
                joinTableKeyList.add(key);
            }
        }
    }

    /**
     * 补充主体sql
     *
     * @param sqlSb               sql
     * @param workcenterVo        工单中心参数
     * @param joinTableKeyList    join 表key列表 防止重复join表
     * @param joinTableColumnList join 表字段列表
     */
    private void buildFromJoinSql(StringBuilder sqlSb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        //补充排序需要的表
        getJoinTableOfOrder(workcenterVo, joinTableKeyList, joinTableColumnList);
        sqlSb.append(" from  processtask pt ");
        for (JoinTableColumnVo joinTableColumn : joinTableColumnList) {
            sqlSb.append(joinTableColumn.toSqlString());
        }
    }

    /**
     * 获取二级分组 join 的字段
     *
     * @param workcenterVo        工单中心参数
     * @param joinTableKeyList    join 表key列表 防止重复join表
     * @param joinTableColumnList join 表字段列表
     */
    private void getJoinTableOfGroupSum(StringBuilder sqlSb, WorkcenterVo workcenterVo, List<String> joinTableKeyList, List<JoinTableColumnVo> joinTableColumnList) {
        DashboardWidgetChartConfigVo chartVo = workcenterVo.getDashboardWidgetChartConfigVo();
        String subGroup = chartVo.getSubGroup();
        String subGroupJoinOn = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(subGroup)) {
            IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(subGroup);
            String subGroupProperty = null;
            for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                Optional<SelectColumnVo> optional = tableSelectColumnVo.getColumnList().stream().filter(SelectColumnVo::getIsPrimary).findFirst();
                if (optional.isPresent()) {
                    subGroupProperty = optional.get().getPropertyName();
                }
            }
            subGroupJoinOn = String.format(" and a.%s = b.%s ", subGroupProperty, subGroupProperty);
        }
        sqlSb.append(String.format("from (%s) a join (%s) b ON a.everyday >= b.everyday %s", chartVo.getSubSql(), chartVo.getSubSql(), subGroupJoinOn));
    }

    @Override
    public int getSort() {
        return 3;
    }

}
