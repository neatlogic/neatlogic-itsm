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

package neatlogic.module.process.dashboard.showconfig.charts;

import neatlogic.framework.common.constvalue.dashboard.ChartType;
import neatlogic.framework.dashboard.constvalue.IDashboardGroupField;
import neatlogic.module.process.dashboard.constvalue.ProcessTaskStepDashboardGroupField;
import neatlogic.module.process.dashboard.showconfig.ProcessTaskStepDashboardWidgetShowConfigBase;
import com.alibaba.fastjson.JSONArray;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProcessTaskStepSeriesWidgetShowConfig extends ProcessTaskStepDashboardWidgetShowConfigBase {
    @Override
    public String[] getSupportChart() {
        return new String[] {ChartType.AREACHART.getValue(), ChartType.LINECHART.getValue()};
    }

    @Override
    public List<IDashboardGroupField> getMyGroupFields(){
        return Collections.singletonList(
                ProcessTaskStepDashboardGroupField.EVERY_DAY
        );
    }

    @Override
    public JSONArray getStatisticsOptionList() {
        return new JSONArray();
    }
}
