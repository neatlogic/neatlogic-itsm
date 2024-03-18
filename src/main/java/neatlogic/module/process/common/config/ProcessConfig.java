/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
