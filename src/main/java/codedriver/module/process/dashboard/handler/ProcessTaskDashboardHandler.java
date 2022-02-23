package codedriver.module.process.dashboard.handler;

import codedriver.framework.dashboard.core.*;
import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.dashboard.core.DashboardChartProcessBase;
import codedriver.module.process.dashboard.core.statistics.DashboardStatisticsFactory;
import codedriver.module.process.dashboard.core.statistics.StatisticsBase;
import codedriver.module.process.dashboard.dto.DashboardWidgetChartConfigProcessVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskDashboardHandler extends DashboardHandlerBase {

    @Override
    public String getName() {
        return "processtask";
    }

    @Override
    protected JSONObject myGetData(DashboardWidgetVo widgetVo) {
        DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
        if (chart != null) {
            DashboardWidgetChartConfigVo chartConfigVo = new DashboardWidgetChartConfigProcessVo(widgetVo.getChartConfigObj());
            DashboardWidgetDataVo widgetDataVo = new DashboardWidgetDataVo();
            widgetDataVo.setChartConfigVo(chartConfigVo);
            /* start: 从mysql 获取源数据 */
            //set条件
            JSONObject conditionConfig = new JSONObject();
            conditionConfig.put("conditionConfig", widgetVo.getConditionConfig());
            conditionConfig.put("pageSize", chartConfigVo.getLimitNum());
            WorkcenterVo workcenterVo = new WorkcenterVo(conditionConfig);
            workcenterVo.setDashboardWidgetChartConfigVo(chartConfigVo);
            StatisticsBase statistics = DashboardStatisticsFactory.getStatistics(chartConfigVo.getStatisticsType());
            statistics.doService(workcenterVo, widgetDataVo, widgetVo);
            /* end: 从mysql 获取源数据 */
            /* start: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
            JSONObject data = new JSONObject();
            data.put("dataList", chart.getData(widgetDataVo).get("dataList"));
            data.put("columnList", chart.getData(widgetDataVo).get("columnList"));
            data.put("theadList", chart.getData(widgetDataVo).get("theadList"));
            /* end: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
            data.put("configObj", chartConfigVo.getConfig());
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
                IDashboardChartCustom chartCustom = DashboardChartCustomFactory.getChart(widgetVo.getChartType(), "process");
                //如果无须自定义渲染配置，则使用默认配置
                if (chartCustom == null) {
                    chartCustom = new DashboardChartProcessBase() {
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
        return "ITSM任务数据";
    }

    @Override
    public String getIcon() {
        return "xx-icon";
    }

}
