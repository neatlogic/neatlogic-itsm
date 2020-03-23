package codedriver.module.process.constvalue;

public enum ProcessWorkcenterCondition {
	ID("id", "工单号"),
	TITLE("title", "标题"),
	CHANNELTYPE("channelType", "服务类型"),
	CHANNEL("channel", "服务"),
	CATALOG("catalog", "服务目录"),
	CONTENT("content", "上报内容"),
	ENDTIME("endTime", "结束时间"),
	STARTTIME("startTime", "开始时间"),
	EXPIRED_TIME("expiredTime", "剩余时间"),
	OWNER("owner", "上报人"),
	REPORTER("reporter", "代报人"),
	PRIORITY("priority", "优先级"),
	STATUS("status", "工单状态"),
	CURRENT_STEP("currentStep","当前步骤"),
	CURRENT_STEP_USER("currentStepUser","当前步骤用户"),
	CURRENT_STEP_STATUS("currentStepStatus","当前步骤状态"),
	WOKRTIME("worktime","时间窗口"),
	STEPUSER("stepUser", "当前处理人"),
	ACTION("action", "操作栏"),
	FORM("form", "表单");
	private String value;
	private String name;

	private ProcessWorkcenterCondition(String _value, String _name) {
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
		for (ProcessWorkcenterCondition s : ProcessWorkcenterCondition.values()) {
			if (s.getValue().equals(_value)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _value) {
		for (ProcessWorkcenterCondition s : ProcessWorkcenterCondition.values()) {
			if (s.getValue().equals(_value)) {
				return s.getName();
			}
		}
		return "";
	}

}
