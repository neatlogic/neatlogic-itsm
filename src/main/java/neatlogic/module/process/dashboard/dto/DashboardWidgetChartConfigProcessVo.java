/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
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
