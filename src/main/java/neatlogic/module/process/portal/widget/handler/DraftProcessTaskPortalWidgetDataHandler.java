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
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.module.process.service.NewWorkcenterService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * 门户“我的草稿”小组件数据处理器。
 * <p>
 * 复用工单中心预置的“我的草稿”视图，保证草稿状态、用户和工单权限过滤口径一致。
 * </p>
 */
@Component
public class DraftProcessTaskPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    private static final String WORKCENTER_UUID = "draftProcessTask";
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;

    @Resource
    private WorkcenterMapper workcenterMapper;

    @Resource
    private NewWorkcenterService newWorkcenterService;

    @Override
    public String getHandler() {
        return "process.draftProcessTask";
    }

    @Override
    protected JSONObject getMyData(JSONObject paramObj) {
        if (paramObj == null) {
            paramObj = new JSONObject();
        }
        JSONObject result = new JSONObject();
        result.put("tbodyList", new ArrayList<>());

        WorkcenterVo workcenterVo = workcenterMapper.getWorkcenterByUuid(WORKCENTER_UUID);
        if (workcenterVo == null) {
            return result;
        }

        boolean needPage = Boolean.TRUE.equals(paramObj.getBoolean("needPage"));
        int pageSize = getPageSize(paramObj, needPage);
        workcenterVo.setCurrentPage(needPage ? paramObj.getInteger("currentPage") : 1);
        workcenterVo.setPageSize(pageSize);
        workcenterVo.setExpectOffsetRowNum(pageSize);

        JSONObject workcenterResult = newWorkcenterService.doSearch(workcenterVo);
        if (workcenterResult == null) {
            return result;
        }

        normalizeTbodyList(workcenterResult.getJSONArray("tbodyList"));
        if (!needPage) {
            JSONArray tbodyList = workcenterResult.getJSONArray("tbodyList");
            if (tbodyList == null) {
                return result;
            }
            if (tbodyList.size() > pageSize) {
                tbodyList = new JSONArray(tbodyList.subList(0, pageSize));
            }
            result.put("tbodyList", tbodyList);
            return result;
        }

        return workcenterResult;
    }

    private int getPageSize(JSONObject paramObj, boolean needPage) {
        Integer pageSize = needPage ? paramObj.getInteger("pageSize") : paramObj.getInteger("limit");
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(pageSize, MAX_LIMIT);
    }

    private void normalizeTbodyList(JSONArray tbodyList) {
        if (tbodyList == null) {
            return;
        }
        for (Object item : tbodyList) {
            if (item instanceof JSONObject) {
                JSONObject task = (JSONObject) item;
                copyIfAbsent(task, "id", "taskid");
                copyIfAbsent(task, "serialNumber", "serialnumber");
                copyIfAbsent(task, "name", "title");
                copyIfAbsent(task, "statusName", "status");
                copyIfAbsent(task, "channelName", "channel");
                copyIfAbsent(task, "catalogName", "catalog");
                copyIfAbsent(task, "currentStepName", "currentstepname");
                copyIfAbsent(task, "currentStepName", "currentstep");
                copyIfAbsent(task, "startTime", "starttime");
                copyIfAbsent(task, "createTime", "starttime");
                copyIfAbsent(task, "expireTime", "expiretime");
            }
        }
    }

    private void copyIfAbsent(JSONObject jsonObj, String targetKey, String sourceKey) {
        if (!jsonObj.containsKey(targetKey) && jsonObj.containsKey(sourceKey)) {
            jsonObj.put(targetKey, jsonObj.get(sourceKey));
        }
    }
}
