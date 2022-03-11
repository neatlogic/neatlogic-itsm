/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.statistics.core;

import codedriver.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.DashboardWidgetParamVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.dashboard.statistics.StatisticsBase;
import codedriver.module.process.sql.decorator.SqlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DashboardSumStatistics extends StatisticsBase {

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "sum";
    }

    @Override
    public List<Map<String, Object>> doService(DashboardWidgetParamVo dashboardWidgetParamVo, DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo, DashboardWidgetVo widgetVo) {
        //1、查出group权重，用于排序截取最大组数量
        DashboardWidgetChartConfigVo chartConfigVo = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo();
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        chartConfigVo.setSubSql(getSubSql(dashboardWidgetParamVo,dashboardWidgetAllGroupDefineVo));
        SqlBuilder sb = new SqlBuilder(dashboardWidgetParamVo, ProcessSqlTypeEnum.GROUP_SUM);
        //System.out.println(sb.build());
        List<Map<String, Object>> groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        //裁剪最大group
        Set<String> groupSet = new HashSet<>();
        int subStartIndex = groupMapList.size();
        for (int i = groupMapList.size() - 1; i > 0; i--) {
            if (groupSet.size() < chartConfigVo.getLimitNum()) {
                groupSet.add(groupMapList.get(i).get(chartConfigVo.getGroup()).toString());
                subStartIndex = i;
            }
        }
        if (subStartIndex < groupMapList.size()) {
            groupMapList = groupMapList.subList(subStartIndex, groupMapList.size());
        }
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());
        if (StringUtils.isNotBlank(chartConfigVo.getSubGroup())) {
            IProcessTaskColumn subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getSubGroup());
            if (subGroupColumn != null) {
                subGroupColumn.getDashboardAllGroupDefine(dashboardWidgetAllGroupDefineVo, groupMapList);
            }
        }
        groupColumn.getDashboardAllGroupDefine(dashboardWidgetAllGroupDefineVo, groupMapList);
        return groupMapList;
    }

    /**
     * 获取子sql
     *
     * @return sql
     */
    private String getSubSql(DashboardWidgetParamVo dashboardWidgetParamVo, DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo) {
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        SqlBuilder sb = new SqlBuilder(dashboardWidgetParamVo, ProcessSqlTypeEnum.GROUP_COUNT);
        DashboardWidgetChartConfigVo chartConfigVo = dashboardWidgetParamVo.getDashboardWidgetChartConfigVo();
        //System.out.println(sb.build());
        List<Map<String, Object>> groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());
        IProcessTaskColumn subGroupColumn = null;
        //2、如果存在subGroup,则根据步骤1查出的权重，排序截取最大组数量，查出二维数据
        if (StringUtils.isNotBlank(chartConfigVo.getSubGroup())) {
            subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getSubGroup());
            if (subGroupColumn != null) {
                //先排序分页获取前分组数的group
                LinkedHashMap<String, Object> dbExchangeGroupDataMap = groupColumn.getExchangeToDashboardGroupDataMap(groupMapList);
                dashboardWidgetAllGroupDefineVo.setDbExchangeGroupDataMap(dbExchangeGroupDataMap);
                dashboardWidgetParamVo.setDbExchangeGroupDataMap(dbExchangeGroupDataMap);
                //根据分组groupDataList、子分组 再次搜索
                sb = new SqlBuilder(dashboardWidgetParamVo, ProcessSqlTypeEnum.SUB_GROUP_COUNT);
                //System.out.println(sb.build());
            }
        }
        return sb.build();
    }
}
