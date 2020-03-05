package codedriver.module.process.constvalue;

public enum ProcessWorkcenterType {
	DEFAULT("default", "默认出厂"), SYSTEM("system", "系统分类"), CUSTOM("custom", "自定义");
	private String value;
	private String name;

	private ProcessWorkcenterType(String _value, String _name) {
		this.value = _value;
		this.name = _name;
	}

	public String getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public static String getValue(String _value) {
		for (ProcessWorkcenterType s : ProcessWorkcenterType.values()) {
			if (s.getValue().equals(_value)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _value) {
		for (ProcessWorkcenterType s : ProcessWorkcenterType.values()) {
			if (s.getValue().equals(_value)) {
				return s.getName();
			}
		}
		return "";
	}

}
