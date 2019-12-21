package codedriver.module.process.constvalue;

public enum ProcessTaskStepWorkerAction {
	HANDLE("handle", "处理"), UPDATE("update", "修改");
	private String type;
	private String name;

	private ProcessTaskStepWorkerAction(String _type, String _name) {
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
		for (ProcessTaskStepWorkerAction s : ProcessTaskStepWorkerAction.values()) {
			if (s.getValue().equals(_type)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _type) {
		for (ProcessTaskStepWorkerAction s : ProcessTaskStepWorkerAction.values()) {
			if (s.getValue().equals(_type)) {
				return s.getName();
			}
		}
		return "";
	}

}
