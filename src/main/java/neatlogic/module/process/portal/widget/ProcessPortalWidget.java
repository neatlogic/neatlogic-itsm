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
    allProcessTask("allProcessTask", "所有工单"),
    processingOfMineProcessTask("processingOfMineProcessTask", "我的待办"),
    doneOfMineProcessTask("doneOfMineProcessTask", "我的已办"),
    draftProcessTask("draftProcessTask", "我的草稿"),
    ;

    private final String value;
    private final String text;

    ProcessPortalWidget(String value, String text) {
        this.value = value;
        this.text = text;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getText() {
        return this.text;
    }
}
