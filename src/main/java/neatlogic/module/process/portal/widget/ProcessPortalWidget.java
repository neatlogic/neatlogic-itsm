/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.process.portal.widget;

import neatlogic.framework.portal.widget.core.IPortalWidget;

public enum ProcessPortalWidget implements IPortalWidget {
    processingOfMineProcessTask("processingOfMineProcessTask", "我的待办", 1),
    processTaskSearch("processTaskSearch", "工单列表", 2),
    processSlaRisk("processSlaRisk", "SLA 风险", 3),
    processFavoriteService("processFavoriteService", "收藏服务", 4),
    processTaskOverview("processTaskOverview", "工单状态概览", 5),
//    draftProcessTask("draftProcessTask", "我的草稿", 4, ProcessPortalWidgetGroup.MY_WORK),

    ;

    private final String value;
    private final String text;
    private final Integer sort;

    ProcessPortalWidget(String value, String text, Integer sort) {
        this.value = value;
        this.text = text;
        this.sort = sort;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public Integer getSort() {
        return this.sort;
    }
}
