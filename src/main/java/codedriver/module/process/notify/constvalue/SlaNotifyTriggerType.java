package codedriver.module.process.notify.constvalue;

import codedriver.framework.notify.core.INotifyTriggerType;

public enum SlaNotifyTriggerType implements INotifyTriggerType {
	TIMEOUT("timeout", "超时","满足SLA策略规则时触发通知");

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
		for(SlaNotifyTriggerType n : SlaNotifyTriggerType.values()) {
			if(n.getTrigger().equals(trigger)) {
				return n.getText();
			}
		}
		return "";
	}
}
