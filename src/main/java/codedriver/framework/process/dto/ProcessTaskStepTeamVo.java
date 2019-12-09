package codedriver.framework.process.dto;

public class ProcessTaskStepTeamVo {
	private Long processTaskStepId;
	private Long teamId;
	private String teamName;

	public ProcessTaskStepTeamVo() {

	}

	public ProcessTaskStepTeamVo(ProcessStepTeamVo processStepTeamVo) {
		this.setTeamId(processStepTeamVo.getTeamId());
		this.setTeamName(processStepTeamVo.getTeamName());
	}

	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}

	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

}
