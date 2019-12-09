package codedriver.framework.process.dto;

import java.io.Serializable;

public class ProcessStepTeamVo implements Serializable{
	private static final long serialVersionUID = 2294654720098359590L;
	private String processStepUuid;
	private Long teamId;
	private String teamName;

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

	public String getProcessStepUuid() {
		return processStepUuid;
	}

	public void setProcessStepUuid(String processStepUuid) {
		this.processStepUuid = processStepUuid;
	}

}
