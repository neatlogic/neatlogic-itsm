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

import neatlogic.framework.portal.widget.core.IPortalWidgetGroup;

public enum ProcessPortalWidgetGroup implements IPortalWidgetGroup {
    group1("group1", "分组1", 11),
    group2("group2", "分组2", 12),
    group3("group3", "分组3", 13),
    group4("group4", "分组4", 14),
    MY_WORK("myWork", "我的工作", 1),
    ;

    private final String value;
    private final String text;
    private final Integer sort;

    ProcessPortalWidgetGroup(String value, String text, Integer sort) {
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
