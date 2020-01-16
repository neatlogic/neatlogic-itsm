package codedriver.module.process.dto;

public class ProcessInformPointcutVo {
	private String value;
	private String name;
	public ProcessInformPointcutVo() {
	}
	public ProcessInformPointcutVo(String value, String name) {
		this.value = value;
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
