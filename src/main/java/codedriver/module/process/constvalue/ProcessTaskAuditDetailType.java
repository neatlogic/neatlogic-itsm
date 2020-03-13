package codedriver.module.process.constvalue;

public enum ProcessTaskAuditDetailType {
	TITLE("title", "标题"),
	PRIORITY("priority", "优先级"),
	CONTENT("content", "回复内容"),
	FORM("form", "表单"),
	WORKER("worker", "处理人"),
	FILE("file", "上传文件");
	
	private String status;
	private String text;

	private ProcessTaskAuditDetailType(String _status, String _text) {
		this.status = _status;
		this.text = _text;
	}

	public String getValue() {
		return status;
	}

	public String getText() {
		return text;
	}

	public static String getValue(String _status) {
		for (ProcessTaskAuditDetailType s : ProcessTaskAuditDetailType.values()) {
			if (s.getValue().equals(_status)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getText(String _status) {
		for (ProcessTaskAuditDetailType s : ProcessTaskAuditDetailType.values()) {
			if (s.getValue().equals(_status)) {
				return s.getText();
			}
		}
		return "";
	}

}
