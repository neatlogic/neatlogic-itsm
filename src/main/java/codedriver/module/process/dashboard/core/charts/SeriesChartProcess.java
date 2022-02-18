/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.core.charts;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.dashboard.constvalue.DashboardGroupField;
import codedriver.framework.dashboard.constvalue.IDashboardGroupField;
import codedriver.module.process.dashboard.core.DashboardChartProcessBase;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SeriesChartProcess extends DashboardChartProcessBase {
    @Override
    public String[] getSupportChart() {
        return new String[] {ChartType.AREACHART.getValue(), ChartType.LINECHART.getValue()};
    }

    @Override
    public List<IDashboardGroupField> getMyGroupFields(){
        return Collections.singletonList(
                DashboardGroupField.EVERY_DAY
        );
    }
}
