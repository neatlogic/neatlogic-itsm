package codedriver.module.process.dto;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessTaskSlaVo {
	@EntityField(name = "id", type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "流程任务id", type = ApiParamType.LONG)
	private Long processTaskId;
	@EntityField(name = "名称", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "配置", type = ApiParamType.JSONOBJECT)
	private String config;
	private transient JSONObject configObj;
	private List<Long> processTaskStepIdList;
	private ProcessTaskSlaTimeVo slaTimeVo;

	public ProcessTaskSlaVo() {

	}

	public ProcessTaskSlaVo(ProcessSlaVo processSlaVo) {
		this.config = processSlaVo.getConfig();
		this.name = processSlaVo.getName();
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

	public List<Long> getProcessTaskStepIdList() {
		return processTaskStepIdList;
	}

	public void setProcessTaskStepIdList(List<Long> processTaskStepIdList) {
		this.processTaskStepIdList = processTaskStepIdList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProcessTaskSlaTimeVo getSlaTimeVo() {
		return slaTimeVo;
	}

	public void setSlaTimeVo(ProcessTaskSlaTimeVo slaTimeVo) {
		this.slaTimeVo = slaTimeVo;
	}

}
