/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.constvalue;

import codedriver.framework.dashboard.constvalue.IDashboardGroupField;

public enum ProcessTaskStepDashboardGroupField implements IDashboardGroupField {
    EVERY_DAY("stepEveryday","每天"),
    EVERY_MONTH("stepEveryMonth","每月"),
    EVERY_QUARTER("stepEveryQuarter","每季度");
    private final String value;
    private final String text;

    ProcessTaskStepDashboardGroupField(String _value, String _text){
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
        for (ProcessTaskStepDashboardGroupField s : ProcessTaskStepDashboardGroupField.values()) {
            if (s.getValue().equals(_value)) {
                return s.getValue();
            }
        }
        return null;
    }
}
