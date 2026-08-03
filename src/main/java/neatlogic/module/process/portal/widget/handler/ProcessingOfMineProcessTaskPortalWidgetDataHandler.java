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
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 门户“我的待办”小组件数据处理器。
 * <p>
 * 复用工单中心预置的“我的待办”视图，保证处理人、状态和工单权限过滤口径一致。
 * </p>
 */
@Component
public class ProcessingOfMineProcessTaskPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Resource
    private NewWorkcenterService newWorkcenterService;

    @Override
    public String getHandler() {
        return "process.processingOfMineProcessTask";
    }

    @Override
    protected JSONObject getMyData(JSONObject paramObj) {
        WorkcenterVo workcenterVo = paramObj.toJavaObject(WorkcenterVo.class);
//        System.out.println("aaworkcenterVo = " + JSONObject.toJSONString(workcenterVo));
        JSONObject startTimeCondition = null;
        if (MapUtils.isNotEmpty(workcenterVo.getConditionConfig())) {
            startTimeCondition = workcenterVo.getConditionConfig().getJSONObject("startTimeCondition");
        } else {
            startTimeCondition = new JSONObject().fluentPut("timeRange", "1").fluentPut("timeUnit", "year");
        }
        JSONObject conditionConfig = new JSONObject();
        conditionConfig.put("handlerType", "simple");
        conditionConfig.put("isProcessingOfMine", 1);
        conditionConfig.put("startTimeCondition", startTimeCondition);
        workcenterVo.setConditionConfig(conditionConfig);
//        System.out.println("bbworkcenterVo = " + JSONObject.toJSONString(workcenterVo));
        JSONObject workcenterResult = newWorkcenterService.doSearch(workcenterVo);
//        System.out.println("workcenterResult = " + workcenterResult);
        return workcenterResult;
    }
}
