/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.dashboard.handler;

import neatlogic.framework.dashboard.charts.DashboardChartBase;
import neatlogic.framework.dashboard.charts.DashboardChartFactory;
import neatlogic.framework.dashboard.config.DashboardWidgetShowConfigFactory;
import neatlogic.framework.dashboard.config.IDashboardWidgetShowConfig;
import neatlogic.framework.dashboard.dto.*;
import neatlogic.framework.dashboard.handler.DashboardHandlerBase;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.module.process.dashboard.dto.DashboardWidgetChartConfigProcessVo;
import neatlogic.module.process.dashboard.showconfig.ProcessTaskStepDashboardWidgetShowConfigBase;
import neatlogic.module.process.dashboard.statistics.DashboardStatisticsFactory;
import neatlogic.module.process.dashboard.statistics.StatisticsBase;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
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
            DashboardWidgetParamVo sqlDecoratorVo = new DashboardWidgetParamVo(widgetVo.getConditionConfigObj(), chartConfigVo.getLimitNum(), chartConfigVo, ProcessTaskStepDashboardHandler.class.getName());
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
