/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.handler;

import codedriver.framework.dashboard.charts.DashboardChartBase;
import codedriver.framework.dashboard.charts.DashboardChartFactory;
import codedriver.framework.dashboard.config.DashboardWidgetShowConfigFactory;
import codedriver.framework.dashboard.config.IDashboardWidgetShowConfig;
import codedriver.framework.dashboard.dto.*;
import codedriver.framework.dashboard.handler.DashboardHandlerBase;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.module.process.dashboard.dto.DashboardWidgetChartConfigProcessVo;
import codedriver.module.process.dashboard.showconfig.ProcessTaskStepDashboardWidgetShowConfigBase;
import codedriver.module.process.dashboard.statistics.DashboardStatisticsFactory;
import codedriver.module.process.dashboard.statistics.StatisticsBase;
import codedriver.framework.process.dto.DashboardWidgetParamVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskStepDashboardHandler extends DashboardHandlerBase {

    @Override
    public String getName() {
        return "processtaskStep";
    }

    @Override
    protected DashboardDataVo myGetData(DashboardWidgetVo widgetVo) {
        DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
        if (chart != null) {
            DashboardWidgetChartConfigVo chartConfigVo = new DashboardWidgetChartConfigProcessVo(widgetVo.getChartConfigObj());
            DashboardWidgetAllGroupDefineVo dashboardWidgetAllGroupDefineVo = new DashboardWidgetAllGroupDefineVo();
            dashboardWidgetAllGroupDefineVo.setChartConfigVo(chartConfigVo);
            /* start: 从mysql 获取源数据 */
            //set条件
            DashboardWidgetParamVo sqlDecoratorVo = new DashboardWidgetParamVo(widgetVo.getConditionConfigObj(), chartConfigVo.getLimitNum(), chartConfigVo, ProcessTaskDashboardHandler.class.getName());
            StatisticsBase statistics = DashboardStatisticsFactory.getStatistics(chartConfigVo.getStatisticsType());
            List<Map<String, Object>> dbDataMapList = statistics.doService(sqlDecoratorVo, dashboardWidgetAllGroupDefineVo, widgetVo);
            /* end: 从mysql 获取源数据 */
            /* start: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
            return chart.getData(dashboardWidgetAllGroupDefineVo, dbDataMapList);
            /* end: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
        }
        return null;
    }

    @Override
    public JSONObject myGetConfig(DashboardWidgetVo widgetVo) {
        DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
        JSONObject processTaskChartConfig = new JSONObject();
        JSONArray processTaskShowChartConfigArray = new JSONArray();
        if (chart != null) {
            JSONObject chartConfig = chart.getChartConfig();
            if (chartConfig.containsKey("showConfig")) {
                JSONObject showConfigJson = chartConfig.getJSONObject("showConfig");
                IDashboardWidgetShowConfig chartCustom = DashboardWidgetShowConfigFactory.getChart(widgetVo.getChartType(), "process", "processtaskStep");
                //如果无须自定义渲染配置，则使用默认配置
                if (chartCustom == null) {
                    chartCustom = new ProcessTaskStepDashboardWidgetShowConfigBase() {
                        @Override
                        public String[] getSupportChart() {
                            return new String[0];
                        }
                    };
                }
                processTaskShowChartConfigArray = chartCustom.getShowConfig(showConfigJson);
            }
        }
        processTaskChartConfig.put("showConfig", processTaskShowChartConfigArray);
        return processTaskChartConfig;
    }

    @Override
    public String getType() {
        return "ITSM";
    }

    @Override
    public String getDisplayName() {
        return "ITSM工单任务数据";
    }

    @Override
    public String getIcon() {
        return "xx-icon";
    }

    @Override
    public String getDistinctCountColumnSql() {
        return String.format(" count(distinct %s.%s)  `count` ", new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.ID.getValue());
    }

}
