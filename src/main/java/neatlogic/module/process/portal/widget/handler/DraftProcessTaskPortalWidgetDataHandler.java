/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x
 * You may use this file only in compliance with the License.
 */

package neatlogic.module.process.portal.widget.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerBase;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.service.NewWorkcenterService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 门户“我的草稿”小组件数据处理器。
 * <p>
 * 复用工单中心预置的“我的草稿”视图，保证草稿状态、用户和工单权限过滤口径一致。
 * </p>
 */
@Component
public class DraftProcessTaskPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Resource
    private NewWorkcenterService newWorkcenterService;

    @Override
    public String getHandler() {
        return "process.draftProcessTask";
    }

    @Override
    protected JSONObject getMyData(JSONObject paramObj) {
        WorkcenterVo workcenterVo = paramObj.toJavaObject(WorkcenterVo.class);
        JSONObject startTimeCondition = null;
        if (MapUtils.isNotEmpty(workcenterVo.getConditionConfig())) {
            startTimeCondition = workcenterVo.getConditionConfig().getJSONObject("startTimeCondition");
        } else {
            startTimeCondition = new JSONObject().fluentPut("timeRange", "1").fluentPut("timeUnit", "year");
        }
        JSONObject conditionConfig = new JSONObject();
        conditionConfig.put("handlerType", "simple");
        conditionConfig.put("isProcessingOfMine", 0);
        conditionConfig.put("startTimeCondition", startTimeCondition);
        conditionConfig.put("conditionGroupRelList", new JSONArray());
        JSONObject conditionGroupObj = new JSONObject()
                .fluentPut("uuid", UuidUtil.randomUuid())
                .fluentPut("channelUuidList", new JSONArray())
                .fluentPut("conditionRelList", new JSONArray().fluentAdd(new JSONObject().fluentPut("joinType", "and").fluentPut("from", UuidUtil.getCustomUUID("status")).fluentPut("to", UuidUtil.getCustomUUID("owner"))))
                .fluentPut("conditionList", new JSONArray()
                        .fluentAdd(new JSONObject().fluentPut("uuid", UuidUtil.getCustomUUID("status")).fluentPut("name", "status").fluentPut("type", "common").fluentPut("expression", "include").fluentPut("valueList", new JSONArray().fluentAdd("draft")))
                        .fluentAdd(new JSONObject().fluentPut("uuid", UuidUtil.getCustomUUID("owner")).fluentPut("name", "owner").fluentPut("type", "common").fluentPut("expression", "include").fluentPut("valueList", new JSONArray().fluentAdd("common#loginuser")))
                )
                ;
        conditionConfig.put("conditionGroupList", new JSONArray().fluentAdd(conditionGroupObj));
        workcenterVo.setConditionConfig(conditionConfig);
        JSONObject workcenterResult = newWorkcenterService.doSearch(workcenterVo);
        return workcenterResult;
    }
}
