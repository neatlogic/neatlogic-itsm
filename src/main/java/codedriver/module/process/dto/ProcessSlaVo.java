package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class ProcessSlaVo implements Serializable {
	private static final long serialVersionUID = 2183891795903221664L;
	private String processUuid;
	private String uuid;
	private String name;
	private String rule;
	private JSONObject ruleObj;

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

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public JSONObject getRuleObj() {
		if (ruleObj == null && StringUtils.isNotBlank(rule)) {
			ruleObj = JSONObject.parseObject(rule);
		}
		return ruleObj;
	}

	public void setRuleObj(JSONObject ruleObj) {
		this.ruleObj = ruleObj;
	}

}
