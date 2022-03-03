/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.core.showconfig.charts;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.module.process.dashboard.core.showconfig.ProcessTaskStepDashboardWidgetShowConfigBase;
import org.springframework.stereotype.Service;

@Service
public class ProcessTaskStepBarWidgetShowConfig extends ProcessTaskStepDashboardWidgetShowConfigBase {
    @Override
    public String[] getSupportChart() {
        return new String[]{ChartType.BARCHART.getValue(), ChartType.STACKBARCHART.getValue(), ChartType.COLUMNCHART.getValue(), ChartType.STACKCOLUMNCHART.getValue()};
    }

}
