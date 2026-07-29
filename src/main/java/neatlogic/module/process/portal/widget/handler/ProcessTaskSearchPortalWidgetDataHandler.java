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
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.module.process.service.NewWorkcenterService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ProcessTaskSearchPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Resource
    private NewWorkcenterService newWorkcenterService;

    @Override
    public String getHandler() {
        return "process.processTaskSearch";
    }

    @Override
    protected JSONObject getMyData(JSONObject paramObj) {
        WorkcenterVo workcenterVo = paramObj.toJavaObject(WorkcenterVo.class);
        if (workcenterVo.getConditionConfig() == null) {
            workcenterVo.setConditionConfig(new JSONObject());
        }
        JSONObject workcenterResult = newWorkcenterService.doSearch(workcenterVo);
        return workcenterResult;
    }
}
