/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.process.portal.widget.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerBase;
import org.springframework.stereotype.Component;

@Component
public class PersonalProcessTaskOverviewPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Override
    public String getHandler() {
        return "process.personalProcessTaskOverview";
    }

    @Override
    protected JSONObject getMyData(JSONObject paramObj) {
        Integer timeRange = paramObj.getInteger("timeRange");
        String timeUnit = paramObj.getString("timeUnit");
        // 我的待办
        // 可抢单
        // 处理中
        // 已超时
        // 已完成
        return null;
    }
}
