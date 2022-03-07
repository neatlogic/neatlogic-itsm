/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.processtask;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dashboard.constvalue.DashboardStatistics;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.handler.DashboardHandlerFactory;
import codedriver.framework.dashboard.handler.IDashboardHandler;
import codedriver.framework.dto.condition.ConditionGroupRelVo;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionRelVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.fulltextindex.dto.fulltextindex.FullTextIndexWordOffsetVo;
import codedriver.framework.fulltextindex.utils.FullTextIndexUtil;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessWorkcenterInitType;
import codedriver.framework.process.workcenter.dto.*;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import codedriver.module.process.condition.handler.ProcessTaskStartTimeCondition;
import codedriver.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
import codedriver.module.process.dashboard.handler.ProcessTaskDashboardHandler;
import codedriver.module.process.dashboard.statistics.DashboardStatisticsFactory;
import codedriver.module.process.dashboard.statistics.StatisticsBase;
import codedriver.module.process.dashboard.handler.ProcessTaskStepDashboardHandler;
import codedriver.module.process.sql.IProcessSqlStructure;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static codedriver.framework.common.util.CommonUtil.distinctByKey;

public abstract class ProcessSqlBase implements IProcessSqlStructure {
    Logger logger = LoggerFactory.getLogger(ProcessSqlBase.class);

    @Override
    public String getDataSourceHandlerName() {
        return ProcessTaskDashboardHandler.class.getName();
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        doMyService(sqlSb, workcenterVo);
    }

    public void doMyService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {

    }

    /**
     * group 拼接order sql
     *
     * @param workcenterVo 工单中心参数
     * @param sqlSb        sql builder
     */
    protected void groupColumnService(WorkcenterVo workcenterVo, StringBuilder sqlSb) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        getColumnSqlList(columnComponentMap, columnList, workcenterVo.getDashboardWidgetChartConfigVo().getGroup(), true);
        IDashboardHandler dashboardHandler = DashboardHandlerFactory.getHandler(workcenterVo.getDataSourceHandler());
        if(dashboardHandler != null){
            columnList.add(dashboardHandler.getDistinctCountColumnSql());
        }else {
            columnList.add("count(1) `count`");
        }
        sqlSb.append(String.join(",", columnList));
    }

    /**
     * sub group 拼接order sql
     *
     * @param workcenterVo 工单中心参数
     * @param sqlSb        sql builder
     */
    protected void subGroupColumnService(WorkcenterVo workcenterVo, StringBuilder sqlSb) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        getColumnSqlList(columnComponentMap, columnList, workcenterVo.getDashboardWidgetChartConfigVo().getGroup(), true);
        getColumnSqlList(columnComponentMap, columnList, workcenterVo.getDashboardWidgetChartConfigVo().getSubGroup(), true);
        columnList.add("count(1) `count`");
        sqlSb.append(String.join(",", columnList));
    }

    /**
     * 获取column sql list
     *
     * @param columnComponentMap 需要展示thead map
     * @param columnList         sql columnList 防止重复取column
     * @param theadName          具体的column
     * @param isGroup            是否group,目前用于dashboard统计
     */
    protected void getColumnSqlList(Map<String, IProcessTaskColumn> columnComponentMap, List<String> columnList, String theadName, Boolean isGroup) {
        if (columnComponentMap.containsKey(theadName)) {
            IProcessTaskColumn column = columnComponentMap.get(theadName);
            for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                if (!isGroup || tableSelectColumnVo.getColumnList().stream().anyMatch(SelectColumnVo::getIsPrimary)) {
                    for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                        String format = " %s.%s as %s ";
                        if (StringUtils.isNotBlank(selectColumnVo.getColumnFormat())) {
                            format = selectColumnVo.getColumnFormat();
                        }
                        String columnStr = String.format(format, tableSelectColumnVo.getTableShortName(), selectColumnVo.getColumnName(), selectColumnVo.getPropertyName());
                        if (!columnList.contains(columnStr)) {
                            columnList.add(columnStr);
                        }
                    }
                }
            }

        }
    }

    /**
     * @Description: 构造column sql
     * @Author: 89770
     * @Date: 2021/1/19 16:32
     * @Params: [sqlSb, workcenterVo]
     * @Returns: void
     **/
    protected void buildField(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        for (WorkcenterTheadVo theadVo : workcenterVo.getTheadVoList()) {
            //去掉沒有勾选的thead
            if (theadVo.getIsShow() != 1) {
                continue;
            }
            getColumnSqlList(columnComponentMap, columnList, theadVo.getName(), false);
        }
        //查询是否隐藏
        columnList.add(String.format(" %s.%s as %s ", new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.IS_SHOW.getValue(), ProcessTaskSqlTable.FieldEnum.IS_SHOW.getValue()));
        columnList.add(String.format(" %s.%s as %s ", new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskSqlTable.FieldEnum.ID.getValue()));
        sqlSb.append(String.join(",", columnList));
    }

    //where

    /**
     * 固定条件
     *
     * @param sqlSb        sql builder
     * @param workcenterVo 工单入参
     */
    protected void buildCommonConditionWhereSql(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        //上报时间
        ProcessTaskStartTimeCondition startTimeSqlCondition = (ProcessTaskStartTimeCondition) ConditionHandlerFactory.getHandler("starttime");
        startTimeSqlCondition.getDateSqlWhere(workcenterVo.getStartTimeCondition(), sqlSb, new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.START_TIME.getValue());
        //我的待办
        if (workcenterVo.getIsProcessingOfMine() == 1) {
            sqlSb.append(" and ");
            IProcessTaskCondition sqlCondition = (IProcessTaskCondition) ConditionHandlerFactory.getHandler("processingofmine");
            sqlCondition.getSqlConditionWhere(null, 0, sqlSb);
        }
        //keyword搜索框搜索 idList 过滤
        if (CollectionUtils.isNotEmpty(workcenterVo.getKeywordConditionList())) {
            for (Object obj : workcenterVo.getKeywordConditionList()) {
                JSONObject condition = JSONObject.parseObject(obj.toString());
                //title serialNumber 全词匹配
                if (ProcessTaskSqlTable.FieldEnum.TITLE.getValue().equals(condition.getString("name"))) {
                    sqlSb.append(String.format(" AND pt.title in ('%s') ", condition.getJSONArray("valueList").stream().map(Object::toString).collect(Collectors.joining("','"))));
                } else if (ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getValue().equals(condition.getString("name"))) {
                    sqlSb.append(String.format(" AND pt.serial_number in (%s) ", condition.getJSONArray("valueList").stream().map(Object::toString).collect(Collectors.joining(","))));
                } else {
                    try {
                        List<FullTextIndexWordOffsetVo> wordOffsetVoList = FullTextIndexUtil.sliceWord(condition.getJSONArray("valueList").stream().map(Object::toString).collect(Collectors.joining("")));
                        String contentWord = wordOffsetVoList.stream().map(FullTextIndexWordOffsetVo::getWord).collect(Collectors.joining("','"));
                        sqlSb.append(String.format("  AND EXISTS (SELECT 1 FROM `fulltextindex_word` fw JOIN fulltextindex_field_process ff ON fw.id = ff.`word_id` JOIN `fulltextindex_target_process` ft ON ff.`target_id` = ft.`target_id` WHERE ff.`target_id` = pt.id  AND ft.`target_type` = 'processtask' AND ff.`target_field` = '%s' AND fw.word IN ('%s') )", condition.getString("name"), contentWord));
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        //隐藏工单 过滤
        Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
        if (!isHasProcessTaskAuth) {
            sqlSb.append(" and pt.is_show = 1 ");
        }
    }

    /**
     * @Description: count , distinct id 则 需要 根据条件获取需要的表。
     * @Author: 89770
     * @Date: 2021/2/8 16:33
     * @Params: [sqlSb, workcenterVo]
     * @Returns: void
     **/
    protected void buildOtherConditionWhereSql(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        // 将group 以连接表达式 存 Map<fromUuid_toUuid,joinType>
        Map<String, String> groupRelMap = new HashMap<>();
        List<ConditionGroupRelVo> groupRelList = workcenterVo.getConditionGroupRelList();
        if (CollectionUtils.isNotEmpty(groupRelList)) {
            for (ConditionGroupRelVo groupRel : groupRelList) {
                groupRelMap.put(groupRel.getFrom() + "_" + groupRel.getTo(), groupRel.getJoinType());
            }
        }
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        if (CollectionUtils.isNotEmpty(groupList)) {
            String fromGroupUuid = null;
            String toGroupUuid = groupList.get(0).getUuid();
            boolean isAddedAnd = false;
            for (ConditionGroupVo groupVo : groupList) {
                // 将condition 以连接表达式 存 Map<fromUuid_toUuid,joinType>
                Map<String, String> conditionRelMap = new HashMap<>();
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
                String toConditionUuid;
                for (int i = 0; i < conditionVoList.size(); i++) {
                    ConditionVo conditionVo = conditionVoList.get(i);
                    //append joinType
                    toConditionUuid = conditionVo.getUuid();
                    if (MapUtils.isNotEmpty(conditionRelMap) && StringUtils.isNotBlank(fromConditionUuid)) {
                        sqlSb.append(conditionRelMap.get(fromConditionUuid + "_" + toConditionUuid));
                    }
                    //append condition
                    String handler = conditionVo.getName();
                    //如果是form
                    if (conditionVo.getType().equals(ProcessFieldType.FORM.getValue())) {
                        handler = ProcessFieldType.FORM.getValue();
                    }
                    IProcessTaskCondition sqlCondition = (IProcessTaskCondition) ConditionHandlerFactory.getHandler(handler);
                    sqlCondition.getSqlConditionWhere(conditionVoList, i, sqlSb);
                    fromConditionUuid = toConditionUuid;
                }
                sqlSb.append(" ) ");
                fromGroupUuid = toGroupUuid;

            }
        }
    }

    /**
     * 只有”我的草稿“分类才显示工单状态”未提交“的工单
     * 不是出厂"我的草稿"&&sql 条件不含有 'draft'（因为只有我的草稿分类 工单状态条件才含有"未提交"状态）&& 需是 "DISTINCT_ID"、"TOTAL_COUNT"和"LIMIT_COUNT" 类型
     *
     * @param sqlSb        sql builder
     * @param workcenterVo 工单入参
     */
    protected void draftCondition(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        if (!Objects.equals(ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getValue(), workcenterVo.getUuid())
                && !sqlSb.toString().contains("draft")
                && Arrays.asList(ProcessSqlTypeEnum.DISTINCT_ID.getValue(), ProcessSqlTypeEnum.TOTAL_COUNT.getValue(), ProcessSqlTypeEnum.LIMIT_COUNT.getValue()).contains(workcenterVo.getSqlFieldType())) {
            sqlSb.append(" and pt.status != 'draft' ");
        }
    }

    /**
     * group 拼接where sql
     *
     * @param workcenterVo 工单中心参数
     * @param sqlSb        sql builder
     */
    protected void groupWhereService(WorkcenterVo workcenterVo, StringBuilder sqlSb) {
        sqlSb.append(" where ");
        buildCommonConditionWhereSql(sqlSb, workcenterVo);
        buildOtherConditionWhereSql(sqlSb, workcenterVo);
        IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(workcenterVo.getDashboardWidgetChartConfigVo().getGroup());
        //拼接sql，对二次过滤选项，如：数值图需要二次过滤选项
        DashboardWidgetChartConfigVo chartConfigVo = workcenterVo.getDashboardWidgetChartConfigVo();
        List<String> groupDataList = new ArrayList<>();
        JSONArray configList = chartConfigVo.getConfigList();
        if (CollectionUtils.isNotEmpty(configList)) {
            groupDataList = JSONObject.parseArray(configList.toJSONString(), String.class);
        }
        //拼接sql，则根据查出的权重，排序截取最大组数量，查出二维数据
        LinkedHashMap<String, Object> groupDataMap = workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap();
        if (MapUtils.isNotEmpty(groupDataMap)) {
            for (Map.Entry<String, Object> entry : groupDataMap.entrySet()) {
                groupDataList.add(entry.getKey());
            }
        }

        if (columnHandler != null) {
            List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
            OUT:
            for (TableSelectColumnVo columnVo : columnVoList) {
                for (SelectColumnVo column : columnVo.getColumnList()) {
                    if (column.getIsPrimary()) {
                        if (CollectionUtils.isNotEmpty(groupDataList)) {
                            String format = " %s.%s ";
                            if (StringUtils.isNotBlank(column.getFormat())) {
                                format = column.getFormat();
                            }
                            format = String.format(" AND %s IN ('%%s') ", format);
                            sqlSb.append(String.format(format, columnVo.getTableShortName(), column.getColumnName(), String.join("','", groupDataList)));
                        }
                        break OUT;
                    }
                }
            }
        }
    }

    //fromjoin

    /**
     * @Description: 补充排序需要 join 的表
     * @Author: 89770
     * @Date: 2021/1/26 20:36
     * @Params: [workcenterVo, joinTableKeyList]
     * @Returns: java.util.List<codedriver.framework.process.workcenter.dto.JoinTableColumnVo>
     **/
    protected List<JoinTableColumnVo> getJoinTableOfOrder(WorkcenterVo workcenterVo, List<JoinTableColumnVo> joinTableColumnList) {
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

                            joinTableColumnList.add(handlerJoinTableColumn);


                        }
                    }
                }
            }
        }
        return joinTableColumnList;
    }

    /**
     * @Description: 根据条件获取需要的表
     * @Author: 89770
     * @Date: 2021/1/20 16:36
     * @Params: []
     * @Returns: void
     **/
    protected void buildJoinTableOfConditionSql(StringBuilder sb, WorkcenterVo workcenterVo) {
        List<JoinTableColumnVo> joinTableColumnList = getJoinTableOfCondition(sb, workcenterVo);
        buildFromJoinSql(sb, workcenterVo, joinTableColumnList);
    }

    /**
     * @Description: 如果是distinct id 则 只需要 根据条件获取需要的表
     * @Author: 89770
     * @Date: 2021/1/20 16:36
     * @Params: []
     * @Returns: void
     **/
    protected List<JoinTableColumnVo> getJoinTableOfCondition(StringBuilder sb, WorkcenterVo workcenterVo) {
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
        //根据接口入参的返回需要的conditionList,然后获取需要关联的tableList
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        for (ConditionGroupVo groupVo : groupList) {
            List<ConditionVo> conditionVoList = groupVo.getConditionList();
            for (ConditionVo conditionVo : conditionVoList) {
                IProcessTaskCondition conditionHandler = (IProcessTaskCondition) ConditionHandlerFactory.getHandler(conditionVo.getName());
                if (conditionHandler != null) {
                    List<JoinTableColumnVo> handlerJoinTableColumnList = conditionHandler.getJoinTableColumnList(workcenterVo);
                    joinTableColumnList.addAll(handlerJoinTableColumnList);
                }
            }
        }
        //我的待办 条件
        if (workcenterVo.getIsProcessingOfMine() == 1) {
            List<JoinTableColumnVo> handlerJoinTableColumnList = SqlTableUtil.getProcessingOfMineJoinTableSql();
            joinTableColumnList.addAll(handlerJoinTableColumnList);
        }
        return joinTableColumnList;
    }

    protected  List<JoinTableColumnVo> getJoinTableOfColumn(StringBuilder sb, WorkcenterVo workcenterVo) {
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
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
                    joinTableColumnList.addAll(getJoinTableColumnList(columnComponentMap, theadVo.getName()));
                }
            }
        }
        return buildFromJoinSql(sb, workcenterVo, joinTableColumnList);
    }

    /**
     * 获取分组 join 的字段
     *
     * @param workcenterVo 工单中心参数
     */
    protected List<JoinTableColumnVo> getJoinTableOfGroupColumn(StringBuilder sb, WorkcenterVo workcenterVo) {
        return getJoinTableOfGroupColumnCommon(sb, workcenterVo, false);
    }

    /**
     * 获取二级分组 join 的字段
     *
     * @param workcenterVo 工单中心参数
     */
    protected void getJoinTableOfSubGroupColumn(StringBuilder sb, WorkcenterVo workcenterVo) {
        getJoinTableOfGroupColumnCommon(sb, workcenterVo, true);
    }

    /**
     * 获取分组 join 的字段
     *
     * @param workcenterVo 工单中心参数
     */
    protected List<JoinTableColumnVo> getJoinTableOfGroupColumnCommon(StringBuilder sb, WorkcenterVo workcenterVo, boolean isSubGroup) {
        //先根据条件补充join table
        List<JoinTableColumnVo> joinTableColumnList = getJoinTableOfCondition(sb, workcenterVo);
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
                joinTableColumnList.addAll(getJoinTableColumnList(columnComponentMap, group));
            }
        }

        return buildFromJoinSql(sb, workcenterVo, joinTableColumnList);
    }

    protected List<JoinTableColumnVo> getJoinTableColumnList(Map<String, IProcessTaskColumn> columnComponentMap, String columnName) {
        IProcessTaskColumn column = columnComponentMap.get(columnName);
        List<JoinTableColumnVo> handlerJoinTableColumnList = column.getJoinTableColumnList();
        return handlerJoinTableColumnList.stream().filter(distinctByKey(JoinTableColumnVo::getHash)).collect(Collectors.toList());
    }

    /**
     * 补充主体sql
     *
     * @param sqlSb        sql
     * @param workcenterVo 工单中心参数
     */
    protected List<JoinTableColumnVo> buildFromJoinSql(StringBuilder sqlSb, WorkcenterVo workcenterVo, List<JoinTableColumnVo> joinTableColumnList) {
        //补充排序需要的表
        joinTableColumnList.addAll(getJoinTableOfOrder(workcenterVo, joinTableColumnList));
        sqlSb.append(" from  processtask pt ");
        //如果数据源有步骤则必须join processtask_step 表
        if(Objects.equals(ProcessTaskStepDashboardHandler.class.getName(),workcenterVo.getDataSourceHandler())){
            joinTableColumnList.add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
            }}));
        }
        joinTableColumnList = joinTableColumnList.stream().filter(distinctByKey(JoinTableColumnVo::getHash)).collect(Collectors.toList());
        for (JoinTableColumnVo joinTableColumn : joinTableColumnList) {
            sqlSb.append(joinTableColumn.toSqlString());
        }
        return joinTableColumnList;
    }

    /**
     * 获取二级分组 join 的字段
     *
     * @param workcenterVo 工单中心参数
     */
    protected void getJoinTableOfGroupSum(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
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

    //groupBy
    protected void getGroupByGroupCount(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        List<String> groupList = new ArrayList<>();
        groupList.add(workcenterVo.getDashboardWidgetChartConfigVo().getGroup());
        if (Objects.equals(workcenterVo.getSqlFieldType(), ProcessSqlTypeEnum.SUB_GROUP_COUNT.getValue())) {
            groupList.add(workcenterVo.getDashboardWidgetChartConfigVo().getSubGroup());
        }

        for (String group : groupList) {
            if (columnComponentMap.containsKey(group)) {
                IProcessTaskColumn column = columnComponentMap.get(group);
                for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                    for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                        if (selectColumnVo.getIsPrimary()) {
                            String columnStr = String.format("%s", selectColumnVo.getPropertyName());
                            if (!columnList.contains(columnStr)) {
                                columnList.add(columnStr);
                            }
                        }
                    }
                }

            }
        }
        sqlSb.append(String.format(" group by %s ", String.join(",", columnList)));
    }

    //order

    /**
     * group 拼接order sql
     *
     * @param workcenterVo 工单中心参数
     * @param sqlSb        sql builder
     */
    protected void groupOrderService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        if (Objects.equals(workcenterVo.getDashboardWidgetChartConfigVo().getStatisticsType(), DashboardStatistics.SUM.getValue())) {
            sqlSb.append(" order by everyday DESC");
            return;
        }
        if (StringUtils.isNotBlank(workcenterVo.getDashboardWidgetChartConfigVo().getSubGroup()) && MapUtils.isNotEmpty(workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap())) {
            List<String> groupDataList = new ArrayList<>();
            if (MapUtils.isNotEmpty(workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap())) {
                for (Map.Entry<String, Object> entry : workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap().entrySet()) {
                    groupDataList.add(entry.getKey());
                }
            }
            IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(workcenterVo.getDashboardWidgetChartConfigVo().getGroup());
            if (columnHandler != null && MapUtils.isNotEmpty(workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap())) {
                List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
                OUT:
                for (TableSelectColumnVo columnVo : columnVoList) {
                    for (SelectColumnVo column : columnVo.getColumnList()) {
                        if (column.getIsPrimary()) {
                            sqlSb.append(" ORDER BY ( CASE ");
                            for (int i = 1; i <= groupDataList.size(); i++) {
                                sqlSb.append(String.format(" WHEN %s = '%s'  THEN %d ", column.getPropertyName(), groupDataList.get(i - 1), i));
                            }
                            break OUT;
                        }
                    }
                }
                sqlSb.append(" END ),COUNT(1) DESC ");
            }
        } else {
            sqlSb.append(" order by COUNT(1) DESC");
        }
    }

    /**
     * 拼接统计方式fromjoin sql
     * @param sqlSb sql
     * @param workcenterVo 入参
     * @param statistics 统计方式
     */
    protected void buildStatisticsFromJoinSql(StringBuilder sqlSb, WorkcenterVo workcenterVo,ProcessTaskDashboardStatistics statistics){
        List<JoinTableColumnVo>  joinTableColumnList = getJoinTableOfGroupColumn(sqlSb, workcenterVo);
        //补充统计joinTable
        StatisticsBase avgStatistics = DashboardStatisticsFactory.getStatistics(statistics.getValue());
        for(JoinTableColumnVo joinTableColumnVo : avgStatistics.getJoinTableColumnList()) {
            if (joinTableColumnList.stream().noneMatch(o-> Objects.equals(o.getHash(),joinTableColumnVo.getHash()))) {
                sqlSb.append(joinTableColumnVo.toSqlString());
            }
        }
    }

    /**
     * 拼接统计方式column sql
     * @param sqlSb sql
     * @param workcenterVo 入参
     * @param statistics 统计方式
     */
    protected void buildStatisticsColumnSql(StringBuilder sqlSb, WorkcenterVo workcenterVo,ProcessTaskDashboardStatistics statistics){
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        getColumnSqlList(columnComponentMap, columnList, workcenterVo.getDashboardWidgetChartConfigVo().getGroup(), true);
        //补充统计column
        StatisticsBase avgStatistics = DashboardStatisticsFactory.getStatistics(statistics.getValue());
        List<TableSelectColumnVo> selectColumnVos = avgStatistics.getTableSelectColumn();
        for(TableSelectColumnVo tableSelectColumnVo : selectColumnVos){
            for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                String format = " %s.%s as %s ";
                if (StringUtils.isNotBlank(selectColumnVo.getColumnFormat())) {
                    format = selectColumnVo.getColumnFormat();
                }
                String columnStr = String.format(format, tableSelectColumnVo.getTableShortName(), selectColumnVo.getColumnName(), selectColumnVo.getPropertyName());
                if (!columnList.contains(columnStr)) {
                    columnList.add(columnStr);
                }
            }
        }
        sqlSb.append(String.join(",", columnList));
    }
}
