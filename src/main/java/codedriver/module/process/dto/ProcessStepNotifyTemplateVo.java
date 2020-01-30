package codedriver.module.process.dto;

public class ProcessStepNotifyTemplateVo {

	private String processUuid;
	private String processStepUuid;
	private String templateUuid;
	public ProcessStepNotifyTemplateVo() { }
	public ProcessStepNotifyTemplateVo(String processUuid, String processStepUuid, String templateUuid) {
		this.processUuid = processUuid;
		this.processStepUuid = processStepUuid;
		this.templateUuid = templateUuid;
	}
	public String getProcessUuid() {
		return processUuid;
	}
	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}
	public String getProcessStepUuid() {
		return processStepUuid;
	}
	public void setProcessStepUuid(String processStepUuid) {
		this.processStepUuid = processStepUuid;
	}
	public String getTemplateUuid() {
		return templateUuid;
	}
	public void setTemplateUuid(String templateUuid) {
		this.templateUuid = templateUuid;
	}
}
