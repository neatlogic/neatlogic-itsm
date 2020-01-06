package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class ProcessStepVo implements Serializable {
	private static final long serialVersionUID = -1211661404097123528L;

	private String processUuid;
	private String uuid;
	private String chartUid;
	private String name;
	private String type;
	private String typeName;
	private String handler;
	private String config;
	private String formUuid;
	private String description;
	private JSONObject configObj;
	private Boolean isWorkerPolicyListSorted = false;
	private Boolean isAttributeListSorted = false;
	private Boolean isTimeoutPolicyListSorted = false;
	private List<ProcessStepUserVo> userList;
	private List<ProcessStepRelVo> relList;
	private List<ProcessStepTeamVo> teamList;
	private List<ProcessStepWorkerPolicyVo> workerPolicyList;
	private List<ProcessStepTimeoutPolicyVo> timeoutPolicyList;
	private List<ProcessStepFormAttributeVo> formAttributeList;

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof ProcessStepVo))
			return false;

		final ProcessStepVo step = (ProcessStepVo) other;
		try {
			if (getUuid().equals(step.getUuid())) {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = getUuid().hashCode() * 19;
		return result;
	}

	public synchronized String getUuid() {
		if (StringUtils.isBlank(uuid)) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public JSONObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	public List<ProcessStepUserVo> getUserList() {
		return userList;
	}

	public void setUserList(List<ProcessStepUserVo> userList) {
		this.userList = userList;
	}

	public List<ProcessStepRelVo> getRelList() {
		return relList;
	}

	public void setRelList(List<ProcessStepRelVo> relList) {
		this.relList = relList;
	}

	public List<ProcessStepTeamVo> getTeamList() {
		return teamList;
	}

	public void setTeamList(List<ProcessStepTeamVo> teamList) {
		this.teamList = teamList;
	}

	public List<ProcessStepWorkerPolicyVo> getWorkerPolicyList() {
		if (!isWorkerPolicyListSorted && workerPolicyList != null && workerPolicyList.size() > 0) {
			Collections.sort(workerPolicyList);
			isWorkerPolicyListSorted = true;
		}
		return workerPolicyList;
	}

	public void setWorkerPolicyList(List<ProcessStepWorkerPolicyVo> workerPolicyList) {
		this.workerPolicyList = workerPolicyList;
	}

	public String getChartUid() {
		return chartUid;
	}

	public void setChartUid(String chartUid) {
		this.chartUid = chartUid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ProcessStepTimeoutPolicyVo> getTimeoutPolicyList() {
		if (!isTimeoutPolicyListSorted && timeoutPolicyList != null && timeoutPolicyList.size() > 0) {
			Collections.sort(timeoutPolicyList);
			isTimeoutPolicyListSorted = true;
		}
		return timeoutPolicyList;
	}

	public void setTimeoutPolicyList(List<ProcessStepTimeoutPolicyVo> timeoutPolicyList) {
		this.timeoutPolicyList = timeoutPolicyList;
	}

	public List<ProcessStepFormAttributeVo> getFormAttributeList() {
		return formAttributeList;
	}

	public void setFormAttributeList(List<ProcessStepFormAttributeVo> formAttributeList) {
		this.formAttributeList = formAttributeList;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

}
