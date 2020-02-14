package codedriver.module.process.workcenter.dto;

public class WorkcenterConditionRelVo {
	private String fromConditionUuid;
	private String toConditionUuid;
	private String joinType;
	
	public String getFromConditionUuid() {
		return fromConditionUuid;
	}
	public void setFromConditionUuid(String fromConditionUuid) {
		this.fromConditionUuid = fromConditionUuid;
	}
	public String getToConditionUuid() {
		return toConditionUuid;
	}
	public void setToConditionUuid(String toConditionUuid) {
		this.toConditionUuid = toConditionUuid;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
	
}
