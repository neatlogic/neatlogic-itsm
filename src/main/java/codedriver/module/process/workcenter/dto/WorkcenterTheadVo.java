package codedriver.module.process.workcenter.dto;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;

public class WorkcenterTheadVo {

	private String workcenterUuid;
	private String name ;
	private Integer sort ;
	private String userId;
	
	public WorkcenterTheadVo(JSONObject obj) {
		this.name = obj.getString("name");
		this.sort = obj.getInteger("sort");
		this.userId = UserContext.get().getUserId();
	}
	
	public WorkcenterTheadVo(String _workcenterUuid,String _userId) {
		this.workcenterUuid = _workcenterUuid;
		this.userId = _userId;
	}
		
	public WorkcenterTheadVo() {
		
	}
	public String getWorkcenterUuid() {
		return workcenterUuid;
	}
	public void setWorkcenterUuid(String workcenterUuid) {
		this.workcenterUuid = workcenterUuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
}
