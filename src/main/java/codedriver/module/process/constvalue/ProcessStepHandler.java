package codedriver.module.process.constvalue;

public enum ProcessStepHandler {
	START("start", "开始"),
	OMNIPOTENT("omnipotent", "通用节点"),
	END("end", "结束"),
	CONDITION("condition", "条件"),
	DISTRIBUTARY("distributary", "分流");

	private String type;
	private String name;

	private ProcessStepHandler(String _type, String _name) {
		this.type = _type;
		this.name = _name;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public static String getType(String _type) {
		for (ProcessStepHandler s : ProcessStepHandler.values()) {
			if (s.getType().equals(_type)) {
				return s.getType();
			}
		}
		return null;
	}

	public static String getName(String _type) {
		for (ProcessStepHandler s : ProcessStepHandler.values()) {
			if (s.getType().equals(_type)) {
				return s.getName();
			}
		}
		return "";
	}

}
