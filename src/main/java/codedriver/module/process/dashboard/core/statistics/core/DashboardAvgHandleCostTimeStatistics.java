/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.core.statistics.core;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.dto.*;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSlaTimeSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
import codedriver.module.process.dashboard.core.statistics.StatisticsBase;
import codedriver.module.process.sql.decorator.SqlBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DashboardAvgHandleCostTimeStatistics extends StatisticsBase {

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return ProcessTaskDashboardStatistics.AVG_HANDLE_COST_TIME.getValue();
    }

    @Override
    public void doService(WorkcenterVo workcenterVo, DashboardWidgetDataVo widgetDataVo, DashboardWidgetVo widgetVo) {
        //1、查出group权重，用于排序截取最大组数量
        DashboardWidgetChartConfigVo chartConfigVo = workcenterVo.getDashboardWidgetChartConfigVo();
        //workcenterVo.getDashboardWidgetChartConfigVo().setGroup(chartConfigVo.getGroup());
        if (!ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType()) && !ChartType.TABLECHART.getValue().equals(widgetVo.getChartType()) && chartConfigVo.getLimitNum() != null) {
            //仅展示分组数
            workcenterVo.setCurrentPage(1);
            workcenterVo.setPageSize(chartConfigVo.getLimitNum());
        }
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        SqlBuilder sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.GROUP_AVG_COST_TIME);
        //System.out.println(sb.build());
        List<Map<String, Object>> groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());

        groupColumn.getDashboardDataVo(widgetDataVo, workcenterVo, groupMapList);
        widgetDataVo.getDataGroupVo().setDataCountMap(workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap());
        widgetDataVo.getDataGroupVo().setDataList(groupMapList);
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ProcessTaskStepSlaTimeSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskStepSlaTimeSqlTable.FieldEnum.TIME_COST.getValue(), "count", true, " AVG(%s.%s)/60000 ")
                )));
            }
        };
    }

    @Override
    public List<JoinTableColumnVo> getJoinTableColumnList() {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                }}));
                add(new JoinTableColumnVo(new ProcessTaskStepSqlTable(), new ProcessTaskStepSlaTimeSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskStepSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSlaTimeSqlTable.FieldEnum.PROCESSTASK_STEP_ID.getValue()));
                }}));
            }
        };
    }

    @Override
    public String getUnit(){
        return "分";
    }
}
