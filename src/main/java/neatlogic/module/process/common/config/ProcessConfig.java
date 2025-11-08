/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.common.config;

import neatlogic.framework.common.config.IConfigListener;

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
