package codedriver.module.process.dto;

public class ProcessTaskStatusVo {

	private String status;
	private String text;
	public ProcessTaskStatusVo() {
	}
	public ProcessTaskStatusVo(String status, String text) {
		this.status = status;
		this.text = text;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
