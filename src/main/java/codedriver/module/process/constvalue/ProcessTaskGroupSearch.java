package codedriver.module.process.constvalue;

public enum ProcessTaskGroupSearch {
	PROCESSUSERTYPE("processUserType","工单干系人类型");
	
	private String value;
	private String text;
	private ProcessTaskGroupSearch(String value, String text) {
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
