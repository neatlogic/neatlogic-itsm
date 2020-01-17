package codedriver.module.process.dto;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessTaskSlaVo {
	@EntityField(name = "id",
			type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "流程任务id",
			type = ApiParamType.LONG)
	private Long processTaskId;
	@EntityField(name = "名称",
			type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "规则",
			type = ApiParamType.STRING)
	private String rule;
	private transient JSONObject ruleObj;
	@EntityField(name = "超时日期（根据工作日历计算）",
			type = ApiParamType.STRING)
	private String expireTime;
	@EntityField(name = "超时日期（不考虑工作日历）",
			type = ApiParamType.STRING)
	private String realExpireTime;
	@EntityField(name = "总耗时，单位：毫秒",
			type = ApiParamType.LONG)
	private Long timeSum;
	@EntityField(name = "剩余时间，单位：毫秒",
			type = ApiParamType.LONG)
	private Long timeLeft;
	private Long realTimeLeft;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

	public String getRealExpireTime() {
		return realExpireTime;
	}

	public void setRealExpireTime(String realExpireTime) {
		this.realExpireTime = realExpireTime;
	}

	public Long getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(Long timeLeft) {
		this.timeLeft = timeLeft;
	}

	public Long getRealTimeLeft() {
		return realTimeLeft;
	}

	public void setRealTimeLeft(Long realTimeLeft) {
		this.realTimeLeft = realTimeLeft;
	}

	public Long getTimeSum() {
		return timeSum;
	}

	public void setTimeSum(Long timesum) {
		this.timeSum = timesum;
	}

}
