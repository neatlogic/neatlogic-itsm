/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.core.statistics.core;

import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.dashboard.core.statistics.StatisticsBase;
import codedriver.module.process.workcenter.core.SqlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class DashboardSumStatistics extends StatisticsBase {

    @Resource
    ProcessTaskMapper processTaskMapper;
    @Override
    public String getName() {
        return "sum";
    }

    @Override
    public void doService(WorkcenterVo workcenterVo, DashboardWidgetDataVo widgetDataVo, DashboardWidgetVo widgetVo) {
        //1、查出group权重，用于排序截取最大组数量
        DashboardWidgetChartConfigVo chartConfigVo = workcenterVo.getDashboardWidgetChartConfigVo();
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        chartConfigVo.setSubSql(getSubSql(workcenterVo));
        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.GROUP_SUM);
        System.out.println(sb.build());
        List<Map<String, Object>> groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());
        if (StringUtils.isNotBlank(chartConfigVo.getSubGroup())) {
            IProcessTaskColumn subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getSubGroup());
            if (subGroupColumn != null) {
                subGroupColumn.getDashboardDataVo(widgetDataVo, workcenterVo, groupMapList);
            }
        }
        groupColumn.getDashboardDataVo(widgetDataVo, workcenterVo, groupMapList);
        widgetDataVo.getDataGroupVo().setDataCountMap(workcenterVo.getDashboardWidgetChartConfigVo().getGroupDataCountMap());
        widgetDataVo.getDataGroupVo().setDataList(groupMapList);
    }

    /**
     * 获取子sql
     * @return sql
     */
    private String getSubSql(WorkcenterVo workcenterVo){
        //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.GROUP_COUNT);
        DashboardWidgetChartConfigVo chartConfigVo = workcenterVo.getDashboardWidgetChartConfigVo();
        System.out.println(sb.build());
        List<Map<String, Object>> groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
        IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getGroup());
        IProcessTaskColumn subGroupColumn = null;
        //2、如果存在subGroup,则根据步骤1查出的权重，排序截取最大组数量，查出二维数据
        if (StringUtils.isNotBlank(chartConfigVo.getSubGroup())) {
            subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(chartConfigVo.getSubGroup());
            if (subGroupColumn != null) {
                //先排序分页获取前分组数的group
                groupColumn.getExchangeToDashboardGroupDataMap(groupMapList, workcenterVo);
                //根据分组groupDataList、子分组 再次搜索
                sb = new SqlBuilder(workcenterVo, FieldTypeEnum.SUB_GROUP_COUNT);
                System.out.println(sb.build());
            }
        }
        return sb.build();
    }
}
