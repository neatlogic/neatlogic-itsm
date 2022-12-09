/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.common.config;

import codedriver.framework.common.config.IConfigListener;

import java.util.Properties;

public class ProcessConfig implements IConfigListener {

    private static String MOBILE_FORM_UI_TYPE;

    public static String MOBILE_FORM_UI_TYPE() {
        return MOBILE_FORM_UI_TYPE;
    }

    @Override
    public void loadConfig(Properties prop) {
        MOBILE_FORM_UI_TYPE = prop.getProperty("mobile.form.ui.type", "0");
    }
}
