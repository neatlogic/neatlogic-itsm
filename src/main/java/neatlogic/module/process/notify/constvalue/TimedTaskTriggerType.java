/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
