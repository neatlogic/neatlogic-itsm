/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.core.showconfig.charts;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.module.process.dashboard.constvalue.ProcessTaskDashboardGroupField;
import codedriver.framework.dashboard.constvalue.IDashboardGroupField;
import codedriver.module.process.dashboard.core.showconfig.ProcessTaskDashboardWidgetShowConfigBase;
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
