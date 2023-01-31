/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.notify.constvalue;

import neatlogic.framework.notify.core.INotifyTriggerType;

/**
 * @author: linbq
 * @since: 2021/4/8 17:40
 **/
public enum TimedTaskTriggerType implements INotifyTriggerType {
    PENDINGPROCESSTASK("pendingprocesstask", "待我处理的工单", "定时任务触发通知");

    private String trigger;
    private String text;
    private String description;

    TimedTaskTriggerType(String trigger, String text, String description) {
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
        return text;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
