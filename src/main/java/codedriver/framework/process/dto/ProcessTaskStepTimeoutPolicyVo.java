package codedriver.framework.process.dto;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class ProcessTaskStepTimeoutPolicyVo implements Comparable<ProcessTaskStepTimeoutPolicyVo> {
	private Long processTaskId;
	private Long processTaskStepId;
	private String processStepUuid;
	private String policy;
	private Integer sort;
	private Integer time;
	private String config;
	private JSONObject configObj;

	public ProcessTaskStepTimeoutPolicyVo() {

	}

	public ProcessTaskStepTimeoutPolicyVo(ProcessStepTimeoutPolicyVo policyVo) {
		this.setPolicy(policyVo.getPolicy());
		this.setSort(policyVo.getSort());
		this.setConfig(policyVo.getConfig());
		this.setTime(policyVo.getTime());
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
		if (StringUtils.isNotBlank(config) && configObj != null) {
			try {
				configObj = JSONObject.parseObject(config);
			} catch (Exception ex) {

			}
		}
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	@Override
	public int compareTo(ProcessTaskStepTimeoutPolicyVo o) {
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

	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

}
