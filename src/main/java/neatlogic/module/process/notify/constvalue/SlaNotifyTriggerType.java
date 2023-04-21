package neatlogic.module.process.notify.constvalue;

import neatlogic.framework.notify.core.INotifyTriggerType;

public enum SlaNotifyTriggerType implements INotifyTriggerType {
    TIMEOUT("timeout", "enum.process.slanotifytriggertype.timeout", "enum.process.slanotifytriggertype.timeout.1");

    private String trigger;
    private String text;
    private String description;

    private SlaNotifyTriggerType(String _trigger, String _text, String _description) {
        this.trigger = _trigger;
        this.text = _text;
        this.description = _description;
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

    public static String getText(String trigger) {
        for (SlaNotifyTriggerType n : SlaNotifyTriggerType.values()) {
            if (n.getTrigger().equals(trigger)) {
                return n.getText();
            }
        }
        return "";
    }
}
