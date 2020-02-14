package codedriver.module.process.workcenter.dto;

import java.io.Serializable;

public class WorkcenterConditionRelVo implements Serializable{
	private static final long serialVersionUID = 4997220400582456563L;
	
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
