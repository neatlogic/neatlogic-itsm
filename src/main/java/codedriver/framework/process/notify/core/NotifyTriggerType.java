package codedriver.framework.process.notify.core;

public enum NotifyTriggerType {
	ACTIVE("active", "激活", "流程步骤已激活", "流程：${data.title}<br>步骤：${data.stepName}<br>上报人：${data.reporterName}"),
	START("start", "开始", "流程步骤已开始", "流程步骤已开始"),
	FAILED("failed", "失败", "流程步骤已失败", "流程步骤已失败"),
	SUCCEED("succeed", "成功", "流程步骤已成功", "流程步骤已成功"),
	HANG("hang", "挂起", "流程步骤已挂起", "流程步骤已挂起"),
	ABORT("aborted", "终止", "流程步骤已终止", "流程步骤已终止"),
	RECOVER("recover", "恢复", "流程步骤已恢复", "流程步骤已恢复"),
	ACCEPT("accept", "接管", "流程步骤接管", "流程步骤接管"),
	TRANSFER("transfer", "转交", "流程步骤已转交", "流程步骤已转交"),
	ASSIGN("assign", "分配处理人", "流程步骤已分配", "流程步骤已分配"),
	TIMEOUT("timeout", "超时", "超时标题", "超时内容");

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
