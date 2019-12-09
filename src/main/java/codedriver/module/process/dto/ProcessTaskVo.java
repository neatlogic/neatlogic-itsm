package codedriver.module.process.dto;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class ProcessTaskVo {
	private Long id;
	private String title;
	private String processUuid;
	private String config;
	private String configPath;
	private JSONObject configObj;
	private String status;
	private String statusText;
	private String owner;
	private String ownerName;
	private String reporter;
	private String reporterName;
	private String startTime;
	private String endTime;
	private Long timeCost;
	private String timeCostStr;
	private String expireTime;
	private List<ProcessTaskStepVo> stepList;

	public ProcessTaskVo() {

	}

	public ProcessTaskVo(Long _id) {
		this.id = _id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public JSONObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public String getReporterName() {
		return reporterName;
	}

	public void setReporterName(String reporterName) {
		this.reporterName = reporterName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Long getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(Long timeCost) {
		this.timeCost = timeCost;
	}

	public String getTimeCostStr() {
		return timeCostStr;
	}

	public void setTimeCostStr(String timeCostStr) {
		this.timeCostStr = timeCostStr;
	}

	public List<ProcessTaskStepVo> getStepList() {
		return stepList;
	}

	public void setStepList(List<ProcessTaskStepVo> stepList) {
		this.stepList = stepList;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

}
