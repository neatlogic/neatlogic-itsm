package codedriver.module.process.constvalue;

public enum ProcessTaskStepAction {
	STARTPROCESS("startprocess", "上报"),
	START("start", "开始"),
	ACTIVE("active", "激活"),
	COMPLETE("complete", "完成"),
	ACCEPT("accept", "接受"),
	ABORT("abort", "终止"),
	RECOVER("recover", "恢复"),
	TRANSFER("transfer", "转交"),
	BACK("back", "回退"),
	SAVE("save", "暂存");
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
