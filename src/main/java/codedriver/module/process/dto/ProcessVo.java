package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.module.process.constvalue.ProcessStepHandler;

public class ProcessVo extends BasePageVo implements Serializable {
	private static final long serialVersionUID = 4684015408674741157L;

	@EntityField(name = "流程uuid", type = ApiParamType.STRING)
	private String uuid;

	@EntityField(name = "流程名称", type = ApiParamType.STRING)
	private String name;

	@EntityField(name = "流程类型名称", type = ApiParamType.STRING)
	private String typeName;

	@EntityField(name = "是否激活", type = ApiParamType.INTEGER)
	private Integer isActive;

	@EntityField(name = "流程图配置", type = ApiParamType.STRING)
	private String config;

	@EntityField(name = "引用数量", type = ApiParamType.INTEGER)
	private int referenceCount;

	private transient JSONObject configObj;
	// @EntityField(name = "流程表单uuid", type = ApiParamType.STRING)
	private String formUuid;
	private List<ProcessStepVo> stepList;

	// @EntityField(name = "流程属性列表", type = ApiParamType.JSONARRAY)
	private List<ProcessStepRelVo> stepRelList;

	private List<ProcessSlaVo> slaList;

	private transient String fcu;
	private transient String keyword;

	public synchronized String getUuid() {
		if (StringUtils.isBlank(uuid)) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}
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

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
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

	public List<ProcessStepVo> getStepList() {
		return stepList;
	}

	public void makeupConfigObj() {
		if (this.getConfigObj() == null) {
			return;
		}
		JSONObject processObj = this.configObj.getJSONObject("process");
		if(processObj == null || processObj.isEmpty()) {
			return;
		}
		/** 组装表单属性 **/
		Map<String, List<ProcessStepFormAttributeVo>> processStepFormAttributeMap = new HashMap<>();
		JSONObject formConfig = processObj.getJSONObject("formConfig");
		if (formConfig != null && formConfig.size() > 0) {
			String formUuid = formConfig.getString("uuid");
			if (StringUtils.isNotBlank(formUuid)) {
				this.setFormUuid(formUuid);
			}
			JSONArray authorityList = formConfig.getJSONArray("authorityList");
			if (authorityList != null && authorityList.size() > 0) {
				for (int i = 0; i < authorityList.size(); i++) {
					JSONObject authorityObj = authorityList.getJSONObject(i);
					JSONArray processStepUuidList = authorityObj.getJSONArray("processStepUuidList");
					if (processStepUuidList != null && processStepUuidList.size() > 0) {
						String attributeUuid = authorityObj.getString("attributeUuid");
						String action = authorityObj.getString("action");
						for (int j = 0; j < processStepUuidList.size(); j++) {
							String processStepUuid = processStepUuidList.getString(j);
							ProcessStepFormAttributeVo processStepFormAttributeVo = new ProcessStepFormAttributeVo();
							processStepFormAttributeVo.setProcessUuid(this.getUuid());
							processStepFormAttributeVo.setFormUuid(this.getFormUuid());
							processStepFormAttributeVo.setProcessStepUuid(processStepUuid);
							processStepFormAttributeVo.setAttributeUuid(attributeUuid);
							processStepFormAttributeVo.setAction(action);

							List<ProcessStepFormAttributeVo> processStepFormAttributeList = processStepFormAttributeMap.get(processStepUuid);
							if (processStepFormAttributeList == null) {
								processStepFormAttributeList = new ArrayList<>();
								processStepFormAttributeMap.put(processStepUuid, processStepFormAttributeList);
							}
							processStepFormAttributeList.add(processStepFormAttributeVo);
						}
					}
				}
			}
		}
		JSONArray slaList = processObj.getJSONArray("slaList");
		if (slaList != null && slaList.size() > 0) {
			this.slaList = new ArrayList<>();
			for (int i = 0; i < slaList.size(); i++) {
				JSONObject slaObj = slaList.getJSONObject(i);
				/** 关联了步骤的sla策略才保存 **/
				JSONArray processStepUuidList = slaObj.getJSONArray("processStepUuidList");
				if (processStepUuidList != null && processStepUuidList.size() > 0) {
					ProcessSlaVo processSlaVo = new ProcessSlaVo();
					processSlaVo.setProcessUuid(this.getUuid());
					processSlaVo.setUuid(slaObj.getString("uuid"));
					processSlaVo.setName(slaObj.getString("name"));
					processSlaVo.setConfig(slaObj.toJSONString());
					this.slaList.add(processSlaVo);
					for (int p = 0; p < processStepUuidList.size(); p++) {
						processSlaVo.addProcessStepUuid(processStepUuidList.getString(p));
					}
				}
			}
		}
		
		JSONArray stepList = processObj.getJSONArray("stepList");
		if (stepList != null && stepList.size() > 0) {
			this.stepList = new ArrayList<>();
			for (int i = 0; i < stepList.size(); i++) {
				JSONObject stepObj = stepList.getJSONObject(i);
				
				ProcessStepVo processStepVo = new ProcessStepVo();
				this.stepList.add(processStepVo);
				processStepVo.setProcessUuid(this.getUuid());
				processStepVo.setConfig(stepObj.getString("stepConfig"));

				String uuid = stepObj.getString("uuid");
				if (StringUtils.isNotBlank(uuid)) {
					processStepVo.setUuid(uuid);
					processStepVo.setFormAttributeList(processStepFormAttributeMap.get(uuid));
				}
				String name = stepObj.getString("name");
				if (StringUtils.isNotBlank(name)) {
					processStepVo.setName(name);
				}
				String handler = stepObj.getString("handler");
				if (StringUtils.isNotBlank(handler)) {
					processStepVo.setHandler(handler);
					processStepVo.setType(ProcessStepHandler.getType(handler));
					IProcessStepHandler procssStepHandler = ProcessStepHandlerFactory.getHandler(handler);
					if (procssStepHandler != null) {
						JSONObject stepConfigObj = stepObj.getJSONObject("stepConfig");
						if(stepConfigObj != null) {
							procssStepHandler.makeupProcessStep(processStepVo, stepConfigObj);
						}
					} else {
						throw new ProcessStepHandlerNotFoundException(handler);
					}
				}
			}
		}
		
		JSONArray relList = processObj.getJSONArray("connectionList");
		if (relList != null && relList.size() > 0) {
			this.stepRelList = new ArrayList<>();
			for (int i = 0; i < relList.size(); i++) {
				JSONObject relObj = relList.getJSONObject(i);
				ProcessStepRelVo processStepRelVo = new ProcessStepRelVo();
				processStepRelVo.setFromStepUuid(relObj.getString("fromStepUuid"));
				processStepRelVo.setToStepUuid(relObj.getString("toStepUuid"));
				processStepRelVo.setUuid(relObj.getString("uuid"));
				processStepRelVo.setProcessUuid(this.getUuid());
				processStepRelVo.setCondition(relObj.getString("conditionConfig"));
				stepRelList.add(processStepRelVo);
			}
		}
	}

	public void setStepList(List<ProcessStepVo> stepList) {
		this.stepList = stepList;
	}

	public List<ProcessStepRelVo> getStepRelList() {
		return stepRelList;
	}

	public void setStepRelList(List<ProcessStepRelVo> stepRelList) {
		this.stepRelList = stepRelList;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getReferenceCount() {
		return referenceCount;
	}

	public void setReferenceCount(int referenceCount) {
		this.referenceCount = referenceCount;
	}

	public String getFcu() {
		return fcu;
	}

	public void setFcu(String fcu) {
		this.fcu = fcu;
	}

	public List<ProcessSlaVo> getSlaList() {
		return slaList;
	}

	public void setSlaList(List<ProcessSlaVo> slaList) {
		this.slaList = slaList;
	}
}
