package codedriver.module.process.dto;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class ProcessTaskVo {
	private Long id;
	private String title;
	private String processUuid;
	private String channelUuid;
	private String priorityUuid;
	private String priority;
	private String config;
	private String configPath;
	private JSONObject configObj;
	private String status;
	private String statusText;
	private String owner;
	private String ownerName;
	private String reporter;
	private String reporterName;
	private Date startTime;
	private Date endTime;
	private Long timeCost;
	private String timeCostStr;
	private Date expireTime;
	private String configHash;
	private String urgency;
	private String urgencyText;
	private List<ProcessTaskStepVo> stepList;

	public ProcessTaskVo() {

	}

	public ProcessTaskVo(Long _id, String _status) {
		this.id = _id;
		this.status = _status;
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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
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

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public String getChannelUuid() {
		return channelUuid;
	}

	public void setChannelUuid(String channelUuid) {
		this.channelUuid = channelUuid;
	}

	public String getConfigHash() {
		return configHash;
	}

	public void setConfigHash(String configHash) {
		this.configHash = configHash;
	}

	public String getUrgency() {
		return urgency;
	}

	public void setUrgency(String urgency) {
		this.urgency = urgency;
	}

	public String getUrgencyText() {
		return urgencyText;
	}

	public void setUrgencyText(String urgencyText) {
		this.urgencyText = urgencyText;
	}

	public String getPriorityUuid() {
		return priorityUuid;
	}

	public void setPriorityUuid(String priorityUuid) {
		this.priorityUuid = priorityUuid;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		return "ProcessTaskVo [id=" + id + ", title=" + title + ", processUuid=" + processUuid + ", channelUuid=" + channelUuid + ", priorityUuid=" + priorityUuid + ", priority=" + priority + ", config=" + config + ", configPath=" + configPath + ", configObj=" + configObj + ", status=" + status + ", statusText=" + statusText + ", owner=" + owner + ", ownerName=" + ownerName + ", reporter=" + reporter + ", reporterName=" + reporterName + ", startTime=" + startTime + ", endTime=" + endTime + ", timeCost=" + timeCost + ", timeCostStr=" + timeCostStr + ", expireTime=" + expireTime + ", configHash=" + configHash + ", urgency=" + urgency + ", urgencyText=" + urgencyText + ", stepList=" + stepList + "]";
	}

}
