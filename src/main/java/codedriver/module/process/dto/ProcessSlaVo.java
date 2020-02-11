package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class ProcessSlaVo implements Serializable {
	private static final long serialVersionUID = 2183891795903221664L;
	private String processUuid;
	private String uuid;
	private String name;
	private String config;
	private JSONObject configObj;
	private List<String> processStepUuidList;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public JSONObject getConfigObj() {
		if (configObj == null && StringUtils.isNotBlank(config)) {
			configObj = JSONObject.parseObject(config);
		}
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	public List<String> getProcessStepUuidList() {
		return processStepUuidList;
	}

	public void setProcessStepUuidList(List<String> processStepUuidList) {
		this.processStepUuidList = processStepUuidList;
	}

	public void addProcessStepUuid(String processStepUuid) {
		if (this.processStepUuidList == null) {
			this.processStepUuidList = new ArrayList<>();
		}
		if (StringUtils.isNotBlank(processStepUuid) && !this.processStepUuidList.contains(processStepUuid)) {
			this.processStepUuidList.add(processStepUuid);
		}
	}

}
