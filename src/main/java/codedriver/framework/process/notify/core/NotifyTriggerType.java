package codedriver.framework.process.notify.core;

public enum NotifyTriggerType {
	ACTIVE("active", "激活", "流程步骤已激活", "流程：${data.title}<br>步骤：${data.stepName}<br>上报人：${data.reporterName}"),
	START("start", "开始", "流程步骤已开始", "流程步骤已开始"),
	FAILED("failed", "已失败", "流程步骤已失败", "流程步骤已失败"),
	SUCCEED("succeed", "已成功", "流程步骤已成功", "流程步骤已成功");

	private String trigger;
	private String text;
	private String titleTemplate;
	private String contentTemplate;

	private NotifyTriggerType(String _trigger, String _text, String _titleTemplate, String _contentTemplate) {
		this.trigger = _trigger;
		this.text = _text;
		this.titleTemplate = _titleTemplate;
		this.contentTemplate = _contentTemplate;
	}

	public String getTitleTemplate() {
		return titleTemplate;
	}

	public String getContentTemplate() {
		return contentTemplate;
	}

	public String getTrigger() {
		return trigger;
	}

	public String getText() {
		return text;
	}

	public static String getTitleTemplate(String trigger) {
		for (NotifyTriggerType s : NotifyTriggerType.values()) {
			if (s.getTrigger().equals(trigger)) {
				return s.getTitleTemplate();
			}
		}
		return null;
	}

	public static String getContentTemplate(String trigger) {
		for (NotifyTriggerType s : NotifyTriggerType.values()) {
			if (s.getTrigger().equals(trigger)) {
				return s.getContentTemplate();
			}
		}
		return null;
	}
}
