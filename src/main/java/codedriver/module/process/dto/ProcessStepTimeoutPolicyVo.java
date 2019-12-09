package codedriver.module.process.dto;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class ProcessStepTimeoutPolicyVo implements Serializable, Comparable<ProcessStepTimeoutPolicyVo> {
	private static final long serialVersionUID = 4329643639648632535L;
	private String processUuid;
	private String processStepUuid;
	private String policy;
	private Integer sort;
	private Integer time;
	private String config;
	private JSONObject configObj;

	public String getProcessStepUuid() {
		return processStepUuid;
	}

	public void setProcessStepUuid(String processStepUuid) {
		this.processStepUuid = processStepUuid;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
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

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	@Override
	public int compareTo(ProcessStepTimeoutPolicyVo o) {
		if (this.getSort() != null && o.getSort() != null) {
			return this.getSort().compareTo(o.getSort());
		}
		return 0;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
