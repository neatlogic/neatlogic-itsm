package codedriver.module.process.workcenter.dto;

import java.io.Serializable;

public class WorkcenterConditionGroupRelVo implements Serializable{
	private static final long serialVersionUID = -6787505561626358277L;
	
	private String fromConditionGroupUuid;
	private String toConditionGroupUuid;
	private String joinType;
	
	public String getFromConditionGroupUuid() {
		return fromConditionGroupUuid;
	}
	public void setFromConditionGroupUuid(String fromConditionGroupUuid) {
		this.fromConditionGroupUuid = fromConditionGroupUuid;
	}
	public String getToConditionGroupUuid() {
		return toConditionGroupUuid;
	}
	public void setToConditionGroupUuid(String toConditionGroupUuid) {
		this.toConditionGroupUuid = toConditionGroupUuid;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
	
}
