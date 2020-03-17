package codedriver.module.process.constvalue;

public enum ProcessWorkcenterCondition {
	ID("id", "工单号"),
	TITLE("title", "标题"),
	CHANNELTYPE("channelType", "服务类型"),
	CHANNEL("channel", "服务"),
	CONTENT("content", "上报内容"),
	ENDTIME("endTime", "结束时间"),
	STARTTIME("startTime", "开始时间"),
	OWNER("owner", "上报人"),
	PRIORITY("priority", "优先级"),
	STATUS("status", "工单状态"),
	STEPSTATUS("owner", "工单步骤状态"),
	STEPUSER("stepUser", "当前处理人");
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
