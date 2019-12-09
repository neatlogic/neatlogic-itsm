package codedriver.module.process.constvalue;

public enum ProcessTaskStepUserStatus {
	DOING("doing", "处理中"), DONE("done", "处理完毕");

	private String status;
	private String text;

	private ProcessTaskStepUserStatus(String _status, String _text) {
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
		for (ProcessTaskStepUserStatus s : ProcessTaskStepUserStatus.values()) {
			if (s.getValue().equals(_status)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getText(String _status) {
		for (ProcessTaskStepUserStatus s : ProcessTaskStepUserStatus.values()) {
			if (s.getValue().equals(_status)) {
				return s.getText();
			}
		}
		return "";
	}

}
