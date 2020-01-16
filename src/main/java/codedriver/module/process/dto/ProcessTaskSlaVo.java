package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.module.process.constvalue.ProcessTaskEvent;

public class ProcessTaskSlaVo {
	private Long id;
	private Long processTaskId;
	private String rule;
	private JSONObject ruleObj;
	private List<Long> processTaskStepIdList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
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

	public List<Long> getProcessTaskStepIdList() {
		return processTaskStepIdList;
	}

	public void setProcessTaskStepIdList(List<Long> processTaskStepIdList) {
		this.processTaskStepIdList = processTaskStepIdList;
	}

}
