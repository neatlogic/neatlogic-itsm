/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
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
