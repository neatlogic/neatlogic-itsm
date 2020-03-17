package codedriver.module.process.dto;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ProcessTaskStepWorkerPolicyVo implements Comparable<ProcessTaskStepWorkerPolicyVo> {
	private Long processTaskId;
	private Long processTaskStepId;
	private String processStepUuid;
	private String policy;
	private Integer sort;
	private String config;
	private JSONObject configObj;
	private JSONArray configObjList;

	public ProcessTaskStepWorkerPolicyVo() {

	}

	public ProcessTaskStepWorkerPolicyVo(ProcessStepWorkerPolicyVo policyVo) {
		this.setPolicy(policyVo.getPolicy());
		this.setSort(policyVo.getSort());
		this.setConfig(policyVo.getConfig());
		this.setProcessStepUuid(policyVo.getProcessStepUuid());
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Long getProcessTaskStepId() {
		return processTaskStepId;
	}

	public void setProcessTaskStepId(Long processTaskStepId) {
		this.processTaskStepId = processTaskStepId;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public JSONObject getConfigObj() {
		if (StringUtils.isNotBlank(config) && configObj == null) {
			try {
				configObj = JSONObject.parseObject(config);
			} catch (Exception ex) {

			}
		}
		return configObj;
	}

	public JSONArray getConfigObjList() {
		if (StringUtils.isNotBlank(config) && configObjList == null) {
			try {
				configObjList = JSONArray.parseArray(config);
			} catch (Exception ex) {

			}
		}
		return configObjList;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	@Override
	public int compareTo(ProcessTaskStepWorkerPolicyVo o) {
		if (this.getSort() != null && o.getSort() != null) {
			return this.getSort().compareTo(o.getSort());
		}
		return 0;
	}

	public String getProcessStepUuid() {
		return processStepUuid;
	}

	public void setProcessStepUuid(String processStepUuid) {
		this.processStepUuid = processStepUuid;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

	@Override
	public String toString() {
		return "ProcessTaskStepWorkerPolicyVo [processTaskId=" + processTaskId + ", processTaskStepId=" + processTaskStepId + ", processStepUuid=" + processStepUuid + ", policy=" + policy + ", sort=" + sort + ", config=" + config + ", configObj=" + configObj + ", configObjList=" + configObjList + "]";
	}

}
