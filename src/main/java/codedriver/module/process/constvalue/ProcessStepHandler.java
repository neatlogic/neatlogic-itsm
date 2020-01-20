package codedriver.module.process.constvalue;

public enum ProcessStepHandler {
	START("start", "start", "开始"),
	OMNIPOTENT("omnipotent", "process", "通用节点"),
	END("end", "end", "结束"),
	CONDITION("condition", "converge", "条件"),
	DISTRIBUTARY("distributary", "converge", "分流");

	private String handler;
	private String name;
	private String type;

	private ProcessStepHandler(String _handler,String _type, String _name) {
		this.handler = _handler;
		this.type = _type;
		this.name = _name;
	}

	public String getHandler() {
		return handler;
	}

	public String getName() {
		return name;
	}

	public static String getHandler(String _handler) {
		for (ProcessStepHandler s : ProcessStepHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getHandler();
			}
		}
		return null;
	}

	public static String getName(String _handler) {
		for (ProcessStepHandler s : ProcessStepHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getName();
			}
		}
		return "";
	}
	
	public static String getType(String _handler) {
		for (ProcessStepHandler s : ProcessStepHandler.values()) {
			if (s.getHandler().equals(_handler)) {
				return s.getType();
			}
		}
		return "";
	}

	public String getType() {
		return type;
	}


}
