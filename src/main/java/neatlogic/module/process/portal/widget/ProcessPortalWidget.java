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
import neatlogic.framework.portal.widgetdata.core.IPortalWidgetDataHandler;
import neatlogic.module.process.portal.widget.handler.*;

import java.util.List;

public enum ProcessPortalWidget implements IPortalWidget {
    processingOfMineProcessTask("processingOfMineProcessTask", "我的待办", 1, List.of(ProcessingOfMineProcessTaskPortalWidgetDataHandler.class, ProcessTaskTheadListWidgetDataHandler.class)),
    draftProcessTask("draftProcessTask", "我的草稿", 2, List.of(DraftProcessTaskPortalWidgetDataHandler.class, ProcessTaskTheadListWidgetDataHandler.class)),
    processTaskSearch("processTaskSearch", "工单列表", 3, List.of(ProcessTaskSearchPortalWidgetDataHandler.class, ProcessTaskTheadListWidgetDataHandler.class)),
    processFavoriteService("processFavoriteService", "收藏服务", 4, List.of(FavoritedServiceListPortalWidgetDataHandler.class)),
    personalProcessTaskOverview("personalProcessTaskOverview", "个人工单状态概览", 5, List.of(PersonalProcessTaskOverviewPortalWidgetDataHandler.class)),
    ;

    private final String value;
    private final String text;
    private final Integer sort;
    private final List<Class<? extends IPortalWidgetDataHandler>> portalWidgetDataHandlerClassList;

    ProcessPortalWidget(String value, String text, Integer sort, List<Class<? extends IPortalWidgetDataHandler>> portalWidgetDataHandlerClassList) {
        this.value = value;
        this.text = text;
        this.sort = sort;
        this.portalWidgetDataHandlerClassList = portalWidgetDataHandlerClassList;
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

    @Override
    public List<Class<? extends IPortalWidgetDataHandler>> getPortalWidgetDataHandlerClassList() {
        return this.portalWidgetDataHandlerClassList;
    }
}
