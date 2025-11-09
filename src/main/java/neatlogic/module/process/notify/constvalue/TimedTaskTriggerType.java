/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.notify.constvalue;

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.util.I18n;

/**
 * @author: linbq
 * @since: 2021/4/8 17:40
 **/
public enum TimedTaskTriggerType implements INotifyTriggerType {
    PENDINGPROCESSTASK("pendingprocesstask", new I18n("待我处理的工单"), new I18n("定时任务触发通知"));

    private String trigger;
    private I18n text;
    private I18n description;

    TimedTaskTriggerType(String trigger, I18n text, I18n description) {
        this.trigger = trigger;
        this.text = text;
        this.description = description;
    }

    @Override
    public String getTrigger() {
        return trigger;
    }

    @Override
    public String getText() {
        return text.toString();
    }

    @Override
    public String getDescription() {
        return description.toString();
    }
}
