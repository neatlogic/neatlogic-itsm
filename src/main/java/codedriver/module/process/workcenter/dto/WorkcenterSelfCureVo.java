package codedriver.module.process.workcenter.dto;

public class WorkcenterSelfCureVo{
	private Long processTaskId;
	
	public WorkcenterSelfCureVo(String tenant, Long processTaskId) {
		this.processTaskId = processTaskId;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}
	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}
	
	
}
