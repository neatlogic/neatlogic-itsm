package codedriver.module.process.dto;

public class ProcessTaskFileVo {

	private Long processTaskId;
	private Long processTaskStepId;
	private String fileUuid;
	
	public ProcessTaskFileVo() {}
	public ProcessTaskFileVo(Long processTaskId, Long processTaskStepId, String fileUuid) {
		this.processTaskId = processTaskId;
		this.processTaskStepId = processTaskStepId;
		this.fileUuid = fileUuid;
	}
	public Long getProcessTaskId() {
		return processTaskId;
	}
	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}
	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}
	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}
	public String getFileUuid() {
		return fileUuid;
	}
	public void setFileUuid(String fileUuid) {
		this.fileUuid = fileUuid;
	}
}
