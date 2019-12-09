package codedriver.module.process.constvalue;

public enum ProcessTaskStepAction {
	INIT("init", "上报"), COMPLETE("complete", "完成"), ACCEPT("accept", "接受"), ABORT("abort", "终止"), TRANSFER("transfer", "转交");

	private String status;
	private String text;

	private ProcessTaskStepAction(String _status, String _text) {
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
		for (ProcessTaskStepAction s : ProcessTaskStepAction.values()) {
			if (s.getValue().equals(_status)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getText(String _status) {
		for (ProcessTaskStepAction s : ProcessTaskStepAction.values()) {
			if (s.getValue().equals(_status)) {
				return s.getText();
			}
		}
		return "";
	}

}
