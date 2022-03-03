/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.handler;

import codedriver.framework.dashboard.charts.*;
import codedriver.framework.dashboard.config.DashboardWidgetShowConfigFactory;
import codedriver.framework.dashboard.config.IDashboardWidgetShowConfig;
import codedriver.framework.dashboard.dto.DashboardWidgetDataVo;
import codedriver.framework.dashboard.handler.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.dashboard.showconfig.ProcessTaskStepDashboardWidgetShowConfigBase;
import codedriver.module.process.dashboard.statistics.DashboardStatisticsFactory;
import codedriver.module.process.dashboard.statistics.StatisticsBase;
import codedriver.module.process.dashboard.dto.DashboardWidgetChartConfigProcessVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskStepDashboardHandler extends DashboardHandlerBase {

    @Override
    public String getName() {
        return "processtaskStep";
    }

    @Override
    protected JSONObject myGetData(DashboardWidgetVo widgetVo) {
        DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
        if (chart != null) {
            DashboardWidgetChartConfigVo chartConfigVo = new DashboardWidgetChartConfigProcessVo(widgetVo.getChartConfigObj());
            DashboardWidgetDataGroupVo widgetDataGroupVo = new DashboardWidgetDataGroupVo();
            widgetDataGroupVo.setChartConfigVo(chartConfigVo);
            /* start: 从mysql 获取源数据 */
            //set条件
            JSONObject conditionConfig = new JSONObject();
            conditionConfig.put("conditionConfig", widgetVo.getConditionConfig());
            conditionConfig.put("pageSize", chartConfigVo.getLimitNum());
            WorkcenterVo workcenterVo = new WorkcenterVo(conditionConfig);
            workcenterVo.setDataSourceHandler(widgetVo.getHandler());
            workcenterVo.setDashboardWidgetChartConfigVo(chartConfigVo);
            StatisticsBase statistics = DashboardStatisticsFactory.getStatistics(chartConfigVo.getStatisticsType());
            statistics.doService(workcenterVo, widgetDataGroupVo, widgetVo);
            /* end: 从mysql 获取源数据 */
            /* start: 将mysql源数据 按不同dashboard插件处理返回结果数据*/

            DashboardWidgetDataVo widgetDataVo = new DashboardWidgetDataVo();
            JSONObject data = new JSONObject();
            data.put("dataList", chart.getData(widgetDataGroupVo).get("dataList"));
            data.put("columnList", chart.getData(widgetDataGroupVo).get("columnList"));
            data.put("theadList", chart.getData(widgetDataGroupVo).get("theadList"));
            /* end: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
            data.put("configObj", chartConfigVo.getConfig().fluentPut("unit",statistics.getUnit()));
            return data;
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
                IDashboardWidgetShowConfig chartCustom = DashboardWidgetShowConfigFactory.getChart(widgetVo.getChartType(), "process","processtaskStep");
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

}
