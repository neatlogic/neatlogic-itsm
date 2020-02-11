package codedriver.module.process.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSONObject;

public class ProcessTaskSlaNotifyVo {
	private Long slaId;
	private String triggerTime;
	private String hash;
	private String config;
	private JSONObject configObj;

	public Long getSlaId() {
		return slaId;
	}

	public void setSlaId(Long slaId) {
		this.slaId = slaId;
	}

	public String getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(String triggerTime) {
		this.triggerTime = triggerTime;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public JSONObject getConfigObj() {
		if (configObj == null && StringUtils.isNotBlank(config)) {
			try {
				configObj = JSONObject.parseObject(config);
			} finally {

			}
		}
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	public String getHash() {
		if (StringUtils.isBlank(hash) && StringUtils.isNotBlank(config)) {
			hash = DigestUtils.md5DigestAsHex(config.getBytes());
		}
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
