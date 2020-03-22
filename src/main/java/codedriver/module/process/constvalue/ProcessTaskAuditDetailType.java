package codedriver.module.process.constvalue;

public enum ProcessTaskAuditDetailType {
	TITLE("title", "标题", "title"),
	PRIORITY("priority", "优先级", "priorityUuid"),
	CONTENT("content", "回复内容", "content"),
	//FORM("form", "表单"),
	WORKER("worker", "处理人", "workerList"),
	FILE("file", "上传文件", "fileUuidList");
	
	private String status;
	private String text;
	private String paramName;
	
	private ProcessTaskAuditDetailType(String _status, String _text, String _paramName) {
		this.status = _status;
		this.text = _text;
		this.paramName = _paramName;
	}

	public String getValue() {
		return status;
	}

	public String getText() {
		return text;
	}

	public String getParamName() {
		return paramName;
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
