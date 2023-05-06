package neatlogic.module.process.notify.constvalue;

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.util.I18n;

public enum SlaNotifyTriggerType implements INotifyTriggerType {
    TIMEOUT("timeout", new I18n("enum.process.slanotifytriggertype.timeout"), new I18n("enum.process.slanotifytriggertype.timeout"));

    private String trigger;
    private I18n text;
    private I18n description;

    SlaNotifyTriggerType(String _trigger, I18n _text, I18n _description) {
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
        return text.toString();
    }

    @Override
    public String getDescription() {
        return description.toString();
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
