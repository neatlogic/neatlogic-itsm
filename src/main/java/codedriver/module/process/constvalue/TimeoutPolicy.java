package codedriver.module.process.constvalue;

public enum TimeoutPolicy {
	SIMPLE("simple", "简易策略"), ADVANCED("advanced", "高级策略");

	private String policy;
	private String text;

	private TimeoutPolicy(String _policy, String _text) {
		this.policy = _policy;
		this.text = _text;
	}

	public String getValue() {
		return policy;
	}

	public String getText() {
		return text;
	}

	public static String getValue(String _policy) {
		for (TimeoutPolicy s : TimeoutPolicy.values()) {
			if (s.getValue().equals(_policy)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getText(String _policy) {
		for (TimeoutPolicy s : TimeoutPolicy.values()) {
			if (s.getValue().equals(_policy)) {
				return s.getText();
			}
		}
		return "";
	}

}
