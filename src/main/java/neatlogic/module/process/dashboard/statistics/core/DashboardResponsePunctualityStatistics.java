/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.dashboard.statistics.core;

import neatlogic.framework.common.constvalue.dashboard.ChartType;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
import neatlogic.framework.process.workcenter.dto.*;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSlaTimeSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
import neatlogic.module.process.dashboard.statistics.StatisticsBase;
import neatlogic.module.process.sql.decorator.SqlBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DashboardResponsePunctualityStatistics extends StatisticsBase {

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return ProcessTaskDashboardStatistics.RESPONSE_PUNCTUALITY.getValue();
    }

    @Override
    public List<Map<String, Object>> doService(DashboardWidgetParamVo dashboardSqlDecoratorVo, DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, DashboardWidgetVo widgetVo) {
        //1、查出group权重，用于排序截取最大组数量
        DashboardWidgetChartConfigVo chartConfigVo = dashboardSqlDecoratorVo.getDashboardWidgetChartConfigVo();
        //workcenterVo.getDashboardWidgetChartConfigVo().setGroup(chartConfigVo.getGroup());
        if (!ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType()) && !ChartType.TABLECHART.getValue().equals(widgetVo.getChartType()) && chartConfigVo.getLimitNum() != null) {
            //仅展示分组数
            dashboardSqlDecoratorVo.setCurrentPage(1);
            dashboardSqlDecoratorVo.setPageSize(chartConfigVo.getLimitNum());
        }
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        SqlBuilder sb = new SqlBuilder(dashboardSqlDecoratorVo, ProcessSqlTypeEnum.GROUP_RESPONSE_PUNCTUALITY);
        //System.out.println(sb.build());
        List<Map<String, Object>> dbDataMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());

        groupColumn.getDashboardAllGroupDefine(dashboardWidgetAllGroupDefineVo, dbDataMapList);
        return dbDataMapList;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ProcessTaskStepSlaTimeSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskStepSlaTimeSqlTable.FieldEnum.IS_TIMEOUT.getValue(), "count", true, "ROUND( (1 -SUM( %s.%s ) / COUNT( 1 )) * 100,2)")
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
        return "%";
    }
}
