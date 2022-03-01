/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.constvalue;

import codedriver.framework.dashboard.constvalue.IDashboardGroupField;

public enum ProcessTaskDashboardStatistics implements IDashboardGroupField {
    AVG_COST_TIME("avgCostTime","平均耗时"),
    AVG_RESPONSE_COST_TIME("avgResponseCostTime","平均响应耗时");

    private final String value;
    private final String text;

    ProcessTaskDashboardStatistics(String _value, String _text){
        value = _value;
        text = _text;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getText() {
        return text;
    }

    public static String getValue(String _value) {
        for (ProcessTaskDashboardStatistics s : ProcessTaskDashboardStatistics.values()) {
            if (s.getValue().equals(_value)) {
                return s.getValue();
            }
        }
        return null;
    }
}
