package codedriver.module.process.dto.condition;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class ConditionRelVo implements Serializable{
	private static final long serialVersionUID = 4997220400582456563L;
	
	private String from;
	private String to;
	private String joinType;
	
	public ConditionRelVo() {
		super();
	}
	
	public ConditionRelVo(JSONObject jsonObj) {
		this.from = jsonObj.getString("from");
		this.to = jsonObj.getString("to");
		this.joinType = jsonObj.getString("joinType");
	}
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
	
}
