package codedriver.module.process.constvalue;

public enum ProcessBelong {
	ITSM("itsm", "itsm"), REQUEST("request", "需求"), BUG("but", "缺陷"), TASK("task", "任务");
	private String value;
	private String name;

	private ProcessBelong(String _value, String _name) {
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
		for (ProcessBelong s : ProcessBelong.values()) {
			if (s.getValue().equals(_value)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _value) {
		for (ProcessBelong s : ProcessBelong.values()) {
			if (s.getValue().equals(_value)) {
				return s.getName();
			}
		}
		return "";
	}

}
