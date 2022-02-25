/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.constvalue;

import codedriver.framework.dashboard.constvalue.IDashboardGroupField;

public enum ProcessTaskDashboardGroupField implements IDashboardGroupField {
    EVERY_DAY("everyday","每天"),
    EVERY_MONTH("everyMonth","每月"),
    EVERY_QUARTER("everyQuarter","每季度");
    private final String value;
    private final String text;

    ProcessTaskDashboardGroupField(String _value, String _text){
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
        for (ProcessTaskDashboardGroupField s : ProcessTaskDashboardGroupField.values()) {
            if (s.getValue().equals(_value)) {
                return s.getValue();
            }
        }
        return null;
    }
}
