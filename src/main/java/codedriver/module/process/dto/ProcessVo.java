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

	public void makeupFromConfigObj() {
		if (this.getConfigObj() == null) {
			return;
		}
		/** 组装表单属性 **/
		Map<String, List<ProcessStepFormAttributeVo>> processStepFormAttributeMap = new HashMap<>();
		if (this.getConfigObj().containsKey("formConfig")) {
			JSONObject formConfig = this.getConfigObj().getJSONObject("formConfig");
			if (formConfig.containsKey("uuid")) {
				String formUuid = formConfig.getString("uuid");
				this.setFormUuid(formUuid);
			}
			if (formConfig.containsKey("authorityList")) {
				JSONArray authorityList = formConfig.getJSONArray("authorityList");
				for (int i = 0; i < authorityList.size(); i++) {
					JSONObject authorityObj = authorityList.getJSONObject(i);
					if (authorityObj.containsKey("processStepUuidList")) {
						String attributeUuid = authorityObj.getString("attributeUuid");
						String action = authorityObj.getString("action");
						JSONArray processStepUuidList = authorityObj.getJSONArray("processStepUuidList");
						for (int j = 0; j < processStepUuidList.size(); j++) {
							String processStepUuid = processStepUuidList.getString(j);
							ProcessStepFormAttributeVo processStepFormAttributeVo = new ProcessStepFormAttributeVo();
							processStepFormAttributeVo.setProcessUuid(this.getUuid());
							processStepFormAttributeVo.setFormUuid(this.getFormUuid());
							processStepFormAttributeVo.setProcessStepUuid(processStepUuid);
							processStepFormAttributeVo.setAttributeUuid(attributeUuid);
							processStepFormAttributeVo.setAction(action);

							List<ProcessStepFormAttributeVo> processStepFormAttributeList = processStepFormAttributeMap
									.get(processStepUuid);
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

		if (this.getConfigObj().containsKey("slaList")) {
			this.slaList = new ArrayList<>();
			JSONArray slaList = this.getConfigObj().getJSONArray("slaList");
			for (int i = 0; i < slaList.size(); i++) {
				JSONObject slaObj = slaList.getJSONObject(i);
				ProcessSlaVo processSlaVo = new ProcessSlaVo();
				processSlaVo.setProcessUuid(this.getUuid());
				processSlaVo.setUuid(slaObj.getString("uuid"));
				processSlaVo.setName(slaObj.getString("name"));
				processSlaVo.setConfig(slaObj.toJSONString());
				this.slaList.add(processSlaVo);
			}
		}

		if (this.getConfigObj().containsKey("stepList")) {
			this.stepList = new ArrayList<>();
			JSONArray stepList = this.getConfigObj().getJSONArray("stepList");
			for (int i = 0; i < stepList.size(); i++) {
				JSONObject stepObj = stepList.getJSONObject(i);
				ProcessStepVo processStepVo = new ProcessStepVo();
				this.stepList.add(processStepVo);
				processStepVo.setProcessUuid(this.getUuid());
				processStepVo.setConfig(stepObj.toJSONString());

				if (stepObj.containsKey("uuid")) {
					String uuid = stepObj.getString("uuid");
					processStepVo.setUuid(uuid);
					processStepVo.setFormAttributeList(processStepFormAttributeMap.get(uuid));
				}
				if (stepObj.containsKey("name")) {
					processStepVo.setName(stepObj.getString("name"));
				}
				if (stepObj.containsKey("type")) {
					String handler = stepObj.getString("type");
					processStepVo.setHandler(handler);
					processStepVo.setType(ProcessStepHandler.getType(handler));
				}
				/** 组装通知模板 **/
				if (stepObj.containsKey("notifyList")) {
					JSONArray notifyList = stepObj.getJSONArray("notifyList");
					List<String> templateUuidList = new ArrayList<>();
					for (int j = 0; j < notifyList.size(); j++) {
						JSONObject notifyObj = notifyList.getJSONObject(j);
						if (notifyObj.containsKey("template")) {
							templateUuidList.add(notifyObj.getString("template"));
						}
					}
					processStepVo.setTemplateUuidList(templateUuidList);
				}
				/** 组装分配策略 **/
				if (stepObj.containsKey("workerPolicyConfig")) {
					JSONObject workerPolicyConfig = stepObj.getJSONObject("workerPolicyConfig");
					if (workerPolicyConfig.containsKey("policyList")) {
						JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
						List<ProcessStepWorkerPolicyVo> workerPolicyList = new ArrayList<>();
						for (int k = 0; k < policyList.size(); k++) {
							JSONObject policyObj = policyList.getJSONObject(k);
							ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
							processStepWorkerPolicyVo.setProcessUuid(this.getUuid());
							processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
							processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
							processStepWorkerPolicyVo.setSort(k + 1);
							processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
							workerPolicyList.add(processStepWorkerPolicyVo);
						}
						processStepVo.setWorkerPolicyList(workerPolicyList);
					}
				}
			}
		}


		if (this.getConfigObj().containsKey("connectionList")) {
			this.stepRelList = new ArrayList<>();
			JSONArray relList = this.getConfigObj().getJSONArray("connectionList");
			for (int i = 0; i < relList.size(); i++) {
				JSONObject relObj = relList.getJSONObject(i);
				ProcessStepRelVo processStepRelVo = new ProcessStepRelVo();
				processStepRelVo.setFromStepUuid(relObj.getString("fromStepUuid"));
				processStepRelVo.setToStepUuid(relObj.getString("toStepUuid"));
				processStepRelVo.setUuid(relObj.getString("uuid"));
				processStepRelVo.setProcessUuid(this.getUuid());
				if (relObj.containsKey("conditionConfig")) {
					processStepRelVo.setCondition(relObj.getString("conditionConfig"));
				}
				stepRelList.add(processStepRelVo);
			}
		}

		// TODO linbq 解析chart
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
