package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.common.dto.BasePageVo;

public class WorkcenterVo extends BasePageVo implements Serializable{
	private static final long serialVersionUID = 1952066708451908924L;
	
	private String uuid;
	private String name;
	private Integer isPrivate;
	private String type;
	private Integer sort;
	private String conditionConfig;
	private JSONArray headerArray;
	private List<WorkcenterConditionGroupVo> WorkcenterConditionGroupList;
	private List<WorkcenterConditionGroupRelVo> WorkcenterConditionGroupRelList;
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getIsPrivate() {
		return isPrivate;
	}
	public void setIsPrivate(Integer isPrivate) {
		this.isPrivate = isPrivate;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public String getConditionConfig() {
		return conditionConfig;
	}
	public void setConditionConfig(String conditionConfig) {
		this.conditionConfig = conditionConfig;
	}
	public List<WorkcenterConditionGroupVo> getWorkcenterConditionGroupList() {
		return WorkcenterConditionGroupList;
	}
	public void setWorkcenterConditionGroupList(List<WorkcenterConditionGroupVo> workcenterConditionGroupList) {
		WorkcenterConditionGroupList = workcenterConditionGroupList;
	}
	public List<WorkcenterConditionGroupRelVo> getWorkcenterConditionGroupRelList() {
		return WorkcenterConditionGroupRelList;
	}
	public void setWorkcenterConditionGroupRelList(List<WorkcenterConditionGroupRelVo> workcenterConditionGroupRelList) {
		WorkcenterConditionGroupRelList = workcenterConditionGroupRelList;
	}
	public JSONArray getHeaderArray() {
		return headerArray;
	}
	public void setHeaderArray(JSONArray headerArray) {
		this.headerArray = headerArray;
	}
	
	
}
