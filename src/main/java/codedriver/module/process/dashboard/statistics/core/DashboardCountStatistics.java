/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.statistics.core;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.dashboard.statistics.StatisticsBase;
import codedriver.module.process.sql.decorator.SqlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class DashboardCountStatistics extends StatisticsBase {

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "count";
    }

    @Override
    public void doService(WorkcenterVo workcenterVo, DashboardWidgetDataGroupVo dashboardWidgetDataGroupVo, DashboardWidgetVo widgetVo) {
        //1、查出group权重，用于排序截取最大组数量
        DashboardWidgetChartConfigVo chartConfigVo = workcenterVo.getDashboardWidgetChartConfigVo();
        //workcenterVo.getDashboardWidgetChartConfigVo().setGroup(chartConfigVo.getGroup());
        if (!ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType()) && !ChartType.TABLECHART.getValue().equals(widgetVo.getChartType()) && chartConfigVo.getLimitNum() != null) {
            //仅展示分组数
            workcenterVo.setCurrentPage(1);
            workcenterVo.setPageSize(chartConfigVo.getLimitNum());
        }
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        SqlBuilder sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.GROUP_COUNT);
        //System.out.println(sb.build());
        List<Map<String, Object>> groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());

        IProcessTaskColumn subGroupColumn = null;
        //2、如果存在subGroup,则根据步骤1查出的权重，排序截取最大组数量，查出二维数据
        if (StringUtils.isNotBlank(chartConfigVo.getSubGroup())) {
            subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getSubGroup());
            if (subGroupColumn != null) {
                //workcenterVo.getDashboardWidgetChartConfigVo().setSubGroup(chartConfigVo.getSubGroup());
                //先排序分页获取前分组数的group
                groupColumn.getExchangeToDashboardGroupDataMap(groupMapList, workcenterVo);
                //根据分组groupDataList、子分组 再次搜索
                sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.SUB_GROUP_COUNT);
                groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
                subGroupColumn.getDashboardDataVo(dashboardWidgetDataGroupVo, workcenterVo, groupMapList);
            }
        }
        groupColumn.getDashboardDataVo(dashboardWidgetDataGroupVo, workcenterVo, groupMapList);
        dashboardWidgetDataGroupVo.getDataGroupVo().setDataCountMap(workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap());
        dashboardWidgetDataGroupVo.getDataGroupVo().setDataList(groupMapList);
    }
}
