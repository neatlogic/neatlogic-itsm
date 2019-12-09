package codedriver.module.process.constvalue;

public enum ProcessTaskEvent {

	ACTIVE("active", "激活"), SUCCEED("succeed", "成功"), FAILED("failed", "失败"), REDO("redo", "重做"), ABORT("abort", "终止"), HANDLE("handle", "处理"), TIMEOUT("timeout", "超时");
	private String name;
	private String text;

	private ProcessTaskEvent(String _name, String _text) {
		this.name = _name;
		this.text = _text;
	}

	public String getValue() {
		return name;
	}

	public String getText() {
		return text;
	}

	public static String getText(String name) {
		for (ProcessTaskEvent s : ProcessTaskEvent.values()) {
			if (s.getValue().equals(name)) {
				return s.getText();
			}
		}
		return "";
	}
}
