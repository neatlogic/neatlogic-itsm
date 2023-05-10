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

package neatlogic.module.process.dashboard.dto;

import neatlogic.framework.common.constvalue.dashboard.DashboardShowConfig;
import neatlogic.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import com.alibaba.fastjson.JSONObject;

public class DashboardWidgetChartConfigProcessVo extends DashboardWidgetChartConfigVo {
    private static final long serialVersionUID = 7667104916854083316L;

    public DashboardWidgetChartConfigProcessVo(){}

    public DashboardWidgetChartConfigProcessVo(JSONObject configChart) {
        super(configChart);
        if (configChart.containsKey(DashboardShowConfig.GROUPFIELD.getValue())) {
            String groupField = configChart.getString(DashboardShowConfig.GROUPFIELD.getValue());
            super.setGroupName(ProcessWorkcenterField.getText(groupField));
        }
        if (configChart.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
            String subGroupField = configChart.getString(DashboardShowConfig.SUBGROUPFIELD.getValue());
            super.setSubGroupName(ProcessWorkcenterField.getText(subGroupField));
        }
    }
}
