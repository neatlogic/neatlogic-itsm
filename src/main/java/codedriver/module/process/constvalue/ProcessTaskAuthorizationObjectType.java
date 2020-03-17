package codedriver.module.process.constvalue;

public enum ProcessTaskAuthorizationObjectType {
	PROCESSUSERTYPE("processUserType","流程干系人类型"),
	USER("user", "用户类型"),
	TEAM("team", "组类型"),
	ROLE("role", "角色类型");
	
	private String value;
	private String text;
	private ProcessTaskAuthorizationObjectType(String value, String text) {
		this.value = value;
		this.text = text;
	}
	public String getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
