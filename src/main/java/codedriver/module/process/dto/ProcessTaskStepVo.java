package codedriver.module.process.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;

public class ProcessTaskStepVo extends BasePageVo {
	private Long id;
	private Long processTaskId;
	private Long fromProcessTaskStepId;
	private Long startProcessTaskStepId;
	private String processUuid;
	private String processStepUuid;
	private String name;
	private String status;
	private String statusName;
	private String handler;
	private String type;
	private String formUuid;
	private String editPage;
	private String viewPage;
	private Integer isActive = 0;
	private Integer isCheck;
	private String startTime;
	private String endTime;
	private String expireTime;
	private String config;
	private Long contentId;
	private Long expireTimeLong;
	private String error;
	private String result;
	private String configHash;
	private JSONObject configObj;
	private Boolean isAllDone = false;
	private Boolean isCurrentUserDone = false;
	private Boolean isWorkerPolicyListSorted = false;
	private Boolean isAttributeListSorted = false;
	private Boolean isTimeoutPolicyListSorted = false;
	private List<ProcessTaskStepUserVo> userList;
	private List<ProcessTaskStepTeamVo> teamList;
	private List<ProcessTaskStepRelVo> relList;
	private List<ProcessTaskStepAttributeVo> attributeList;
	private List<ProcessTaskStepWorkerPolicyVo> workerPolicyList;
	private List<ProcessTaskStepTimeoutPolicyVo> timeoutPolicyList;
	private List<ProcessTaskStepFormAttributeVo> formAttributeList;
	private JSONObject paramObj;

	public ProcessTaskStepVo() {

	}

	public ProcessTaskStepVo(ProcessStepVo processStepVo) {
		this.setProcessUuid(processStepVo.getProcessUuid());
		this.setProcessStepUuid(processStepVo.getUuid());
		this.setName(processStepVo.getName());
		this.setHandler(processStepVo.getHandler());
		this.setType(processStepVo.getType());
		this.setEditPage(processStepVo.getEditPage());
		this.setViewPage(processStepVo.getViewPage());
		this.setConfig(processStepVo.getConfig());
		this.setFormUuid(processStepVo.getFormUuid());
		if (processStepVo.getUserList() != null && processStepVo.getUserList().size() > 0) {
			List<ProcessTaskStepUserVo> userList = new ArrayList<>();
			for (ProcessStepUserVo userVo : processStepVo.getUserList()) {
				ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(userVo);
				userList.add(processTaskStepUserVo);
			}
			this.setUserList(userList);
		}
		if (processStepVo.getTeamList() != null && processStepVo.getTeamList().size() > 0) {
			List<ProcessTaskStepTeamVo> teamList = new ArrayList<>();
			for (ProcessStepTeamVo teamVo : processStepVo.getTeamList()) {
				ProcessTaskStepTeamVo processTaskStepTeamVo = new ProcessTaskStepTeamVo(teamVo);
				teamList.add(processTaskStepTeamVo);
			}
			this.setTeamList(teamList);
		}
		if (processStepVo.getAttributeList() != null && processStepVo.getAttributeList().size() > 0) {
			List<ProcessTaskStepAttributeVo> attributeList = new ArrayList<>();
			for (ProcessStepAttributeVo attributeVo : processStepVo.getAttributeList()) {
				attributeVo.setProcessStepUuid(processStepVo.getUuid());
				ProcessTaskStepAttributeVo processTaskStepAttributeVo = new ProcessTaskStepAttributeVo(attributeVo);
				attributeList.add(processTaskStepAttributeVo);
			}
			this.setAttributeList(attributeList);
		}
		if (processStepVo.getFormAttributeList() != null && processStepVo.getAttributeList().size() > 0) {
			List<ProcessTaskStepFormAttributeVo> attributeList = new ArrayList<>();
			for (ProcessStepFormAttributeVo attributeVo : processStepVo.getFormAttributeList()) {
				attributeVo.setProcessStepUuid(processStepVo.getUuid());
				ProcessTaskStepFormAttributeVo processTaskStepAttributeVo = new ProcessTaskStepFormAttributeVo(attributeVo);
				attributeList.add(processTaskStepAttributeVo);
			}
			this.setFormAttributeList(attributeList);
		}
		if (processStepVo.getWorkerPolicyList() != null && processStepVo.getWorkerPolicyList().size() > 0) {
			List<ProcessTaskStepWorkerPolicyVo> policyList = new ArrayList<>();
			for (ProcessStepWorkerPolicyVo policyVo : processStepVo.getWorkerPolicyList()) {
				policyVo.setProcessStepUuid(processStepVo.getUuid());
				ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo(policyVo);
				policyList.add(processTaskStepWorkerPolicyVo);
			}
			this.setWorkerPolicyList(policyList);
		}
		if (processStepVo.getTimeoutPolicyList() != null && processStepVo.getTimeoutPolicyList().size() > 0) {
			List<ProcessTaskStepTimeoutPolicyVo> timeoutList = new ArrayList<>();
			for (ProcessStepTimeoutPolicyVo policyVo : processStepVo.getTimeoutPolicyList()) {
				policyVo.setProcessStepUuid(processStepVo.getUuid());
				ProcessTaskStepTimeoutPolicyVo processTaskStepTimeoutPolicyVo = new ProcessTaskStepTimeoutPolicyVo(policyVo);
				timeoutList.add(processTaskStepTimeoutPolicyVo);
			}
			this.setTimeoutPolicyList(timeoutList);
		}
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActived) {
		this.isActive = isActived;
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

	public List<ProcessTaskStepUserVo> getUserList() {
		return userList;
	}

	public void setUserList(List<ProcessTaskStepUserVo> userList) {
		this.userList = userList;
	}

	public List<ProcessTaskStepTeamVo> getTeamList() {
		return teamList;
	}

	public void setTeamList(List<ProcessTaskStepTeamVo> teamList) {
		this.teamList = teamList;
	}

	public List<ProcessTaskStepRelVo> getRelList() {
		return relList;
	}

	public void setRelList(List<ProcessTaskStepRelVo> relList) {
		this.relList = relList;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getEditPage() {
		if (StringUtils.isBlank(editPage) && StringUtils.isNotBlank(handler)) {
			IProcessStepHandler stepHandler = ProcessStepHandlerFactory.getHandler(handler);
			if (stepHandler != null) {
				editPage = stepHandler.getEditPage();
			}
		}
		return editPage;
	}

	public void setEditPage(String editPage) {
		this.editPage = editPage;
	}

	public String getViewPage() {
		return viewPage;
	}

	public void setViewPage(String viewPage) {
		this.viewPage = viewPage;
	}

	public List<ProcessTaskStepAttributeVo> getAttributeList() {
		if (!isAttributeListSorted && this.attributeList != null && this.attributeList.size() > 0) {
			Collections.sort(this.attributeList);
			isAttributeListSorted = true;
		}
		return attributeList;
	}

	public void setAttributeList(List<ProcessTaskStepAttributeVo> attributeList) {
		this.attributeList = attributeList;
	}

	public String getProcessStepUuid() {
		return processStepUuid;
	}

	public void setProcessStepUuid(String processStepUuid) {
		this.processStepUuid = processStepUuid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public Long getContentId() {
		return contentId;
	}

	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void appendError(String error) {
		if (this.error != null) {
			this.error += "\n";
			this.error += error;
		} else {
			this.error = error;
		}
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Long getFromProcessTaskStepId() {
		return fromProcessTaskStepId;
	}

	public void setFromProcessTaskStepId(Long fromProcessTaskStepId) {
		this.fromProcessTaskStepId = fromProcessTaskStepId;
	}

	public Integer getIsCheck() {
		return isCheck;
	}

	public void setIsCheck(Integer isCheck) {
		this.isCheck = isCheck;
	}

	public List<ProcessTaskStepWorkerPolicyVo> getWorkerPolicyList() {
		if (!isWorkerPolicyListSorted && workerPolicyList != null && workerPolicyList.size() > 0) {
			Collections.sort(workerPolicyList);
			isWorkerPolicyListSorted = true;
		}
		return workerPolicyList;
	}

	public void setWorkerPolicyList(List<ProcessTaskStepWorkerPolicyVo> workerPolicyList) {
		this.workerPolicyList = workerPolicyList;
	}

	public JSONObject getParamObj() {
		return paramObj;
	}

	public void setParamObj(JSONObject paramObj) {
		this.paramObj = paramObj;
	}

	public Boolean getIsAllDone() {
		return isAllDone;
	}

	public void setIsAllDone(Boolean isAllDone) {
		if (isAllDone) {
			this.isCurrentUserDone = isAllDone;
		}
		this.isAllDone = isAllDone;
	}

	public Boolean getIsCurrentUserDone() {
		return isCurrentUserDone;
	}

	public void setIsCurrentUserDone(Boolean isCurrentUserDone) {
		this.isCurrentUserDone = isCurrentUserDone;
	}

	public List<ProcessTaskStepTimeoutPolicyVo> getTimeoutPolicyList() {
		if (!isTimeoutPolicyListSorted && timeoutPolicyList != null && timeoutPolicyList.size() > 0) {
			Collections.sort(timeoutPolicyList);
			isTimeoutPolicyListSorted = true;
		}
		return timeoutPolicyList;
	}

	public void setTimeoutPolicyList(List<ProcessTaskStepTimeoutPolicyVo> timeoutPolicyList) {
		this.timeoutPolicyList = timeoutPolicyList;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

	public Long getExpireTimeLong() {
		return expireTimeLong;
	}

	public void setExpireTimeLong(Long expireTimeLong) {
		this.expireTimeLong = expireTimeLong;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public List<ProcessTaskStepFormAttributeVo> getFormAttributeList() {
		return formAttributeList;
	}

	public void setFormAttributeList(List<ProcessTaskStepFormAttributeVo> formAttributeList) {
		this.formAttributeList = formAttributeList;
	}

	public Long getStartProcessTaskStepId() {
		return startProcessTaskStepId;
	}

	public void setStartProcessTaskStepId(Long startProcessTaskStepId) {
		this.startProcessTaskStepId = startProcessTaskStepId;
	}

	public String getConfigHash() {
		return configHash;
	}

	public void setConfigHash(String configHash) {
		this.configHash = configHash;
	}

}
