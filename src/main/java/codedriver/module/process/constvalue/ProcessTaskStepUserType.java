package codedriver.module.process.constvalue;

public enum ProcessTaskStepUserType {
	MAJOR("major", "主处理人"), MINOR("minor", "协助处理人");

	private String status;
	private String text;

	private ProcessTaskStepUserType(String _status, String _text) {
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
		for (ProcessTaskStepUserType s : ProcessTaskStepUserType.values()) {
			if (s.getValue().equals(_status)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getText(String _status) {
		for (ProcessTaskStepUserType s : ProcessTaskStepUserType.values()) {
			if (s.getValue().equals(_status)) {
				return s.getText();
			}
		}
		return "";
	}

}
