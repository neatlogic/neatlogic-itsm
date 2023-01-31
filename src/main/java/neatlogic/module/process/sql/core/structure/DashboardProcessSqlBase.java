/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.sql.core.structure;

import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.dashboard.constvalue.DashboardStatistics;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.handler.DashboardHandlerFactory;
import neatlogic.framework.dashboard.handler.IDashboardHandler;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.condition.handler.ProcessTaskStartTimeCondition;
import neatlogic.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
import neatlogic.module.process.dashboard.handler.ProcessTaskStepDashboardHandler;
import neatlogic.module.process.dashboard.statistics.DashboardStatisticsFactory;
import neatlogic.module.process.dashboard.statistics.StatisticsBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static neatlogic.framework.common.util.CommonUtil.distinctByKey;

public abstract class DashboardProcessSqlBase extends ProcessSqlBase<DashboardWidgetParamVo> {

    @Override
    public void doService(StringBuilder sqlSb, DashboardWidgetParamVo sqlDecoratorVo) {
        doMyService(sqlSb, sqlDecoratorVo);
    }

    public void doMyService(StringBuilder sqlSb, DashboardWidgetParamVo sqlDecoratorVo) {

    }

    /**
     * group 拼接order sql
     *
     * @param sqlDecoratorVo 工单中心参数
     * @param sqlSb          sql builder
     */
    protected void groupColumnService(DashboardWidgetParamVo sqlDecoratorVo, StringBuilder sqlSb) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        getColumnSqlList(columnComponentMap, columnList, sqlDecoratorVo.getDashboardWidgetChartConfigVo().getGroup(), true);
        IDashboardHandler dashboardHandler = DashboardHandlerFactory.getHandler(sqlDecoratorVo.getDataSourceHandler());
        if (dashboardHandler != null) {
            columnList.add(dashboardHandler.getDistinctCountColumnSql());
        } else {
            columnList.add("count(1) `count`");
        }
        sqlSb.append(String.join(",", columnList));
    }

    /**
     * sub group 拼接order sql
     *
     * @param dashboardWidgetParamVo 工单中心参数
     * @param sqlSb                  sql builder
     */
    protected void subGroupColumnService(DashboardWidgetParamVo dashboardWidgetParamVo, StringBuilder sqlSb) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        getColumnSqlList(columnComponentMap, columnList, dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup(), true);
        getColumnSqlList(columnComponentMap, columnList, dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getSubGroup(), true);
        columnList.add("count(1) `count`");
        sqlSb.append(String.join(",", columnList));
    }

    /**
     * 固定条件
     *
     * @param sqlSb                  sql builder
     * @param dashboardWidgetParamVo 工单入参
     */
    protected void buildDashboardCommonConditionWhereSql(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        //上报时间
        ProcessTaskStartTimeCondition startTimeSqlCondition = (ProcessTaskStartTimeCondition) ConditionHandlerFactory.getHandler("starttime");
        startTimeSqlCondition.getDateSqlWhere(dashboardWidgetParamVo.getStartTimeCondition(), sqlSb, new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.START_TIME.getValue());
        //隐藏工单 过滤
        Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
        if (!isHasProcessTaskAuth) {
            sqlSb.append(" and pt.is_show = 1 ");
        }
    }

    /**
     * group 拼接where sql
     *
     * @param dashboardWidgetParamVo 工单中心参数
     * @param sqlSb                  sql builder
     */
    protected void groupWhereService(DashboardWidgetParamVo dashboardWidgetParamVo, StringBuilder sqlSb) {
        sqlSb.append(" where ");
        buildDashboardCommonConditionWhereSql(sqlSb, dashboardWidgetParamVo);
        buildOtherConditionWhereSql(sqlSb, dashboardWidgetParamVo);
        sqlSb.append(" and pt.status != 'draft' ");
        IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup());
        //拼接sql，对二次过滤选项，如：数值图需要二次过滤选项
        DashboardWidgetChartConfigVo chartConfigVo = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo();
        List<String> groupDataList = new ArrayList<>();
        JSONArray configList = chartConfigVo.getConfigList();
        if (CollectionUtils.isNotEmpty(configList)) {
            groupDataList = JSONObject.parseArray(configList.toJSONString(), String.class);
            groupDataList = groupDataList.stream().map(GroupSearch::removePrefix).collect(Collectors.toList());
        }
        //二级分组拼接sql，则根据查出的权重，排序截取最大组数量，查出二维数据
        LinkedHashMap<String, Object> groupDataMap = dashboardWidgetParamVo.getDbExchangeGroupDataMap();
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

    /**
     * 获取分组 join 的字段
     *
     * @param sqlDecoratorVo 工单中心参数
     */
    protected List<JoinTableColumnVo> getJoinTableOfGroupColumn(StringBuilder sb, DashboardWidgetParamVo sqlDecoratorVo) {
        return getJoinTableOfGroupColumnCommon(sb, sqlDecoratorVo, false);
    }

    /**
     * 获取二级分组 join 的字段
     *
     * @param sqlDecoratorVo 工单中心参数
     */
    protected void getJoinTableOfSubGroupColumn(StringBuilder sb, DashboardWidgetParamVo sqlDecoratorVo) {
        getJoinTableOfGroupColumnCommon(sb, sqlDecoratorVo, true);
    }

    /**
     * 获取分组 join 的字段
     *
     * @param dashboardWidgetParamVo 参数
     */
    protected List<JoinTableColumnVo> getJoinTableOfGroupColumnCommon(StringBuilder sb, DashboardWidgetParamVo dashboardWidgetParamVo, boolean isSubGroup) {
        //先根据条件补充join table
        List<JoinTableColumnVo> joinTableColumnList = getJoinTableOfCondition(sb, dashboardWidgetParamVo);
        //根据接口入参的返回需要的columnList,然后获取需要关联的tableList
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //循环所有需要展示的字段
        List<String> groupList = new ArrayList<>();
        groupList.add(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup());
        if (isSubGroup) {
            groupList.add(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getSubGroup());
        }
        if (CollectionUtils.isNotEmpty(groupList)) {//group by 需要join的表
            for (String group : groupList) {
                joinTableColumnList.addAll(getJoinTableColumnList(columnComponentMap, group));
            }
        }

        //如果数据源有步骤则必须join processtask_step 表
        if (Objects.equals(ProcessTaskStepDashboardHandler.class.getName(), dashboardWidgetParamVo.getDataSourceHandler())) {
            joinTableColumnList.add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
            }}));
        }

        return buildFromJoinSql(sb, dashboardWidgetParamVo, joinTableColumnList);
    }

    /**
     * 补充主体sql
     *
     * @param sqlSb          sql
     * @param sqlDecoratorVo 工单中心参数
     */
    protected List<JoinTableColumnVo> buildFromJoinSql(StringBuilder sqlSb, DashboardWidgetParamVo sqlDecoratorVo, List<JoinTableColumnVo> joinTableColumnList) {
        //补充排序需要的表
        sqlSb.append(" from  processtask pt ");
        joinTableColumnList = joinTableColumnList.stream().filter(distinctByKey(JoinTableColumnVo::getHash)).collect(Collectors.toList());
        for (JoinTableColumnVo joinTableColumn : joinTableColumnList) {
            sqlSb.append(joinTableColumn.toSqlString());
        }
        return joinTableColumnList;
    }

    /**
     * 获取二级分组 join 的字段
     *
     * @param dashboardWidgetParamVo 参数
     */
    protected void getJoinTableOfGroupSum(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        DashboardWidgetChartConfigVo chartVo = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo();
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
        String everyday = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup();
        sqlSb.append(String.format("from (%s) a join (%s) b ON a.%s >= b.%s %s", chartVo.getSubSql(), chartVo.getSubSql(), everyday, everyday, subGroupJoinOn));
    }

    /**
     * 拼group by sql
     *
     * @param sqlSb                  sql
     * @param dashboardWidgetParamVo dashboard 入参
     */
    protected void getGroupByGroupCount(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        List<String> groupList = new ArrayList<>();
        groupList.add(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup());
        if (Objects.equals(dashboardWidgetParamVo.getSqlFieldType(), ProcessSqlTypeEnum.SUB_GROUP_COUNT.getValue())) {
            groupList.add(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getSubGroup());
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
     * @param dashboardWidgetParamVo 参数
     * @param sqlSb                  sql builder
     */
    protected void groupOrderService(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        if (Objects.equals(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getStatisticsType(), DashboardStatistics.SUM.getValue())) {
            String everyday = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup();
            sqlSb.append(String.format("order by %s ASC", everyday));
            return;
        }
        if (StringUtils.isNotBlank(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getSubGroup()) && MapUtils.isNotEmpty(dashboardWidgetParamVo.getDbExchangeGroupDataMap())) {
            List<String> groupDataList = new ArrayList<>();
            if (MapUtils.isNotEmpty(dashboardWidgetParamVo.getDbExchangeGroupDataMap())) {
                for (Map.Entry<String, Object> entry : dashboardWidgetParamVo.getDbExchangeGroupDataMap().entrySet()) {
                    groupDataList.add(entry.getKey());
                }
            }
            IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(dashboardWidgetParamVo.getDashboardWidgetChartConfigVo().getGroup());
            if (columnHandler != null && MapUtils.isNotEmpty(dashboardWidgetParamVo.getDbExchangeGroupDataMap())) {
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
     * 拼接统计方式 fromjoin sql
     *
     * @param sqlSb          sql
     * @param sqlDecoratorVo 入参
     * @param statistics     统计方式
     */
    protected void buildStatisticsFromJoinSql(StringBuilder sqlSb, DashboardWidgetParamVo sqlDecoratorVo, ProcessTaskDashboardStatistics statistics) {
        List<JoinTableColumnVo> joinTableColumnList = getJoinTableOfGroupColumn(sqlSb, sqlDecoratorVo);
        //补充统计joinTable
        StatisticsBase avgStatistics = DashboardStatisticsFactory.getStatistics(statistics.getValue());
        for (JoinTableColumnVo joinTableColumnVo : avgStatistics.getJoinTableColumnList()) {
            if (joinTableColumnList.stream().noneMatch(o -> Objects.equals(o.getHash(), joinTableColumnVo.getHash()))) {
                sqlSb.append(joinTableColumnVo.toSqlString());
            }
        }
    }

    /**
     * 拼接统计方式column sql
     *
     * @param sqlSb          sql
     * @param sqlDecoratorVo 入参
     * @param statistics     统计方式
     */
    protected void buildStatisticsColumnSql(StringBuilder sqlSb, DashboardWidgetParamVo sqlDecoratorVo, ProcessTaskDashboardStatistics statistics) {
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        List<String> columnList = new ArrayList<>();
        getColumnSqlList(columnComponentMap, columnList, sqlDecoratorVo.getDashboardWidgetChartConfigVo().getGroup(), true);
        //补充统计column
        StatisticsBase avgStatistics = DashboardStatisticsFactory.getStatistics(statistics.getValue());
        List<TableSelectColumnVo> selectColumnVos = avgStatistics.getTableSelectColumn();
        for (TableSelectColumnVo tableSelectColumnVo : selectColumnVos) {
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
