/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.dashboard.statistics.core;

import neatlogic.framework.common.constvalue.dashboard.ChartType;
import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.dashboard.statistics.StatisticsBase;
import neatlogic.module.process.sql.decorator.SqlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
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
    public List<Map<String, Object>> doService(DashboardWidgetParamVo dashboardWidgetParamVo, DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, DashboardWidgetVo widgetVo) {
        //1、查出group权重，用于排序截取最大组数量
        DashboardWidgetChartConfigVo chartConfigVo = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo();
        //workcenterVo.getDashboardWidgetChartConfigVo().setGroup(chartConfigVo.getGroup());
        if (!ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType()) && !ChartType.TABLECHART.getValue().equals(widgetVo.getChartType()) && chartConfigVo.getLimitNum() != null) {
            //仅展示分组数
            dashboardWidgetParamVo.setCurrentPage(1);
            dashboardWidgetParamVo.setPageSize(chartConfigVo.getLimitNum());
        }
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        SqlBuilder sb = new SqlBuilder(dashboardWidgetParamVo, ProcessSqlTypeEnum.GROUP_COUNT);
        //System.out.println(sb.build());
        List<Map<String, Object>> dbMapDataList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());
        //2、如果存在subGroup,则根据步骤1查出的权重，排序截取最大组数量，查出二维数据
        if (StringUtils.isNotBlank(chartConfigVo.getSubGroup())) {
            IProcessTaskColumn subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getSubGroup());
            if (subGroupColumn != null) {
                //先排序分页获取前分组数的group
                LinkedHashMap<String, Object> dbExchangeGroupDataMap = groupColumn.getExchangeToDashboardGroupDataMap(dbMapDataList);
                dashboardWidgetAllGroupDefineVo.setDbExchangeGroupDataMap(dbExchangeGroupDataMap);
                dashboardWidgetParamVo.setDbExchangeGroupDataMap(dbExchangeGroupDataMap);
                //根据分组groupDataList、子分组 再次搜索
                sb = new SqlBuilder(dashboardWidgetParamVo, ProcessSqlTypeEnum.SUB_GROUP_COUNT);
                dbMapDataList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
                subGroupColumn.getDashboardAllGroupDefine(dashboardWidgetAllGroupDefineVo, dbMapDataList);
            }
        }
        groupColumn.getDashboardAllGroupDefine(dashboardWidgetAllGroupDefineVo, dbMapDataList);
        return dbMapDataList;
    }
}
