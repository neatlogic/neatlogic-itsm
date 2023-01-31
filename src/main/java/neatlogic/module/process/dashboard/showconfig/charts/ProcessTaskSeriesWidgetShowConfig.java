/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.dashboard.showconfig.charts;

import neatlogic.framework.common.constvalue.dashboard.ChartType;
import neatlogic.module.process.dashboard.constvalue.ProcessTaskDashboardGroupField;
import neatlogic.framework.dashboard.constvalue.IDashboardGroupField;
import neatlogic.module.process.dashboard.showconfig.ProcessTaskDashboardWidgetShowConfigBase;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProcessTaskSeriesWidgetShowConfig extends ProcessTaskDashboardWidgetShowConfigBase {
    @Override
    public String[] getSupportChart() {
        return new String[] {ChartType.AREACHART.getValue(), ChartType.LINECHART.getValue()};
    }

    @Override
    public List<IDashboardGroupField> getMyGroupFields(){
        return Collections.singletonList(
                ProcessTaskDashboardGroupField.EVERY_DAY
        );
    }
}
