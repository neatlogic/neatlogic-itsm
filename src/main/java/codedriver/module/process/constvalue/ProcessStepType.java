package codedriver.module.process.constvalue;

public enum ProcessStepType {
	START("start", "开始"), PROCESS("process", "处理节点"), END("end", "结束");
	private String type;
	private String name;

	private ProcessStepType(String _type, String _name) {
		this.type = _type;
		this.name = _name;
	}

	public String getValue() {
		return type;
	}

	public String getName() {
		return name;
	}

	public static String getValue(String _type) {
		for (ProcessStepType s : ProcessStepType.values()) {
			if (s.getValue().equals(_type)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _type) {
		for (ProcessStepType s : ProcessStepType.values()) {
			if (s.getValue().equals(_type)) {
				return s.getName();
			}
		}
		return "";
	}

}
