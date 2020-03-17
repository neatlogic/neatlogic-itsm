package codedriver.module.process.constvalue;

public enum ProcessWorkcenterConditionType {
	COMMON("common", "工单固有属性"), FORM("form", "表单属性");
	private String value;
	private String name;

	private ProcessWorkcenterConditionType(String _value, String _name) {
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
		for (ProcessWorkcenterConditionType s : ProcessWorkcenterConditionType.values()) {
			if (s.getValue().equals(_value)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _value) {
		for (ProcessWorkcenterConditionType s : ProcessWorkcenterConditionType.values()) {
			if (s.getValue().equals(_value)) {
				return s.getName();
			}
		}
		return "";
	}

}
