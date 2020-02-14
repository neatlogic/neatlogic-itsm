package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.List;

public class WorkcenterConditionGroupVo implements Serializable{
	private static final long serialVersionUID = 8392325201425982471L;
	
	private String uuid;
	private List<WorkcenterConditionVo> conditionList;
	private List<WorkcenterConditionRelVo> conditionRelList;
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		/*if(uuid == null) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}*/
		return uuid;
	}

	public List<WorkcenterConditionVo> getConditionList() {
		return conditionList;
	}

	public void setConditionList(List<WorkcenterConditionVo> conditionList) {
		this.conditionList = conditionList;
	}

	public List<WorkcenterConditionRelVo> getConditionRelList() {
		return conditionRelList;
	}

	public void setConditionRelList(List<WorkcenterConditionRelVo> conditionRelList) {
		this.conditionRelList = conditionRelList;
	}
	
	
}
