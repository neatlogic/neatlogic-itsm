package codedriver.module.process.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessTaskVo {
	@EntityField(name = "工单id", type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "父工单id", type = ApiParamType.LONG)
	private Long parentId;
	@EntityField(name = "标题", type = ApiParamType.STRING)
	private String title;
	@EntityField(name = "流程uuid", type = ApiParamType.STRING)
	private String processUuid;
	@EntityField(name = "服务uuid", type = ApiParamType.STRING)
	private String channelUuid;
	@EntityField(name = "优先级uuid", type = ApiParamType.STRING)
	private String priorityUuid;
	@EntityField(name = "优先级名", type = ApiParamType.STRING)
	private String priorityName;
	@EntityField(name = "工单流程图信息", type = ApiParamType.STRING)
	private String config;
	private String configPath;
	private JSONObject configObj;
	@EntityField(name = "状态", type = ApiParamType.STRING)
	private String status;
	@EntityField(name = "状态名", type = ApiParamType.STRING)
	private String statusText;
	@EntityField(name = "上报人userId", type = ApiParamType.STRING)
	private String owner;
	@EntityField(name = "上报人", type = ApiParamType.STRING)
	private String ownerName;
	@EntityField(name = "代报人userId", type = ApiParamType.STRING)
	private String reporter;
	@EntityField(name = "代报人", type = ApiParamType.STRING)
	private String reporterName;
	@EntityField(name = "开始时间", type = ApiParamType.LONG)
	private Date startTime;
	@EntityField(name = "结束时间", type = ApiParamType.LONG)
	private Date endTime;
	@EntityField(name = "耗时(秒)", type = ApiParamType.LONG)
	private Long timeCost;
	private String timeCostStr;
	@EntityField(name = "超时时间点", type = ApiParamType.LONG)
	private Date expireTime;
	private String configHash;
	private String urgency;
	private String urgencyText;
	private List<ProcessTaskStepVo> stepList;
	
	@EntityField(name = "描述内容", type = ApiParamType.STRING)
	private String content;
	@EntityField(name = "优先级信息", type = ApiParamType.JSONOBJECT)
	private PriorityVo priority;
	@EntityField(name = "工单表单信息", type = ApiParamType.STRING)
	private String formConfig;
	@EntityField(name = "工单表单属性值", type = ApiParamType.JSONOBJECT)
	Map<String, String> formAttributeDataMap;
	
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

	public String getPriorityName() {
		return priorityName;
	}

	public void setPriorityName(String priorityName) {
		this.priorityName = priorityName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public PriorityVo getPriority() {
		return priority;
	}

	public void setPriority(PriorityVo priority) {
		this.priority = priority;
	}

	public String getFormConfig() {
		return formConfig;
	}

	public void setFormConfig(String formConfig) {
		this.formConfig = formConfig;
	}

	public Map<String, String> getFormAttributeDataMap() {
		return formAttributeDataMap;
	}

	public void setFormAttributeDataMap(Map<String, String> formAttributeDataMap) {
		this.formAttributeDataMap = formAttributeDataMap;
	}

}
