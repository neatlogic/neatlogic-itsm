package codedriver.module.process.constvalue;

public enum ProcessWorkcenterConditionModel {
	SIMPLE("simple", "简单模式"), CUSTOM("custom", "自定义模式");
	private String value;
	private String name;

	private ProcessWorkcenterConditionModel(String _value, String _name) {
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
		for (ProcessWorkcenterConditionModel s : ProcessWorkcenterConditionModel.values()) {
			if (s.getValue().equals(_value)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _value) {
		for (ProcessWorkcenterConditionModel s : ProcessWorkcenterConditionModel.values()) {
			if (s.getValue().equals(_value)) {
				return s.getName();
			}
		}
		return "";
	}

}
