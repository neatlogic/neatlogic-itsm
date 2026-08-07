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
import neatlogic.framework.util.$;
import neatlogic.module.process.portal.widget.handler.*;

import java.util.List;

public enum ProcessPortalWidget implements IPortalWidget {
    PROCESSING_OF_MINE_PROCESSTASK("processingOfMineProcessTask", "nfpc.processworkcenterinittype.text.processing_of_mine_processtask", 1, List.of(ProcessingOfMineProcessTaskPortalWidgetDataHandler.class, ProcessTaskTheadListWidgetDataHandler.class)),
    DRAFT_PROCESS_TASK("draftProcessTask", "nfpc.processworkcenterinittype.text.draft_processtask", 2, List.of(DraftProcessTaskPortalWidgetDataHandler.class, ProcessTaskTheadListWidgetDataHandler.class)),
    PROCESS_TASK_SEARCH("processTaskSearch", "nmpap.processtasklistforrelationapi.output.param.desc.tbodylist", 3, List.of(ProcessTaskSearchPortalWidgetDataHandler.class, ProcessTaskTheadListWidgetDataHandler.class)),
    PROCESS_FAVORITE_SERVICE("processFavoriteService", "nmppw.processportalwidget.text.processfavoriteservice", 4, List.of(FavoritedServiceListPortalWidgetDataHandler.class)),
    PERSONAL_PROCESS_TASK_OVERVIEW("personalProcessTaskOverview", "nmppw.processportalwidget.text.personalprocesstaskoverview", 5, List.of(PersonalProcessTaskOverviewPortalWidgetDataHandler.class)),
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
        return $.t(this.text);
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
