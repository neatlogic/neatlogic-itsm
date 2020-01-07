package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.module.process.constvalue.ProcessStepType;

public class ProcessVo extends BasePageVo implements Serializable {
	private static final long serialVersionUID = 4684015408674741157L;

	@EntityField(name = "流程uuid",
			type = ApiParamType.STRING)
	private String uuid;

	@EntityField(name = "流程名称",
			type = ApiParamType.STRING)
	private String name;

	@EntityField(name = "流程类型id",
			type = ApiParamType.LONG)
	private Long type;

	@EntityField(name = "流程类型名称",
			type = ApiParamType.STRING)
	private String typeName;

	@EntityField(name = "是否激活",
			type = ApiParamType.STRING)
	private Integer isActive = 1;

	@EntityField(name = "流程图配置",
			type = ApiParamType.STRING)
	private String config;

	private String belong;

	private JSONObject configObj;
	// @EntityField(name = "流程表单uuid", type = ApiParamType.STRING)
	private String formUuid;
	private List<ProcessStepVo> stepList;

	// @EntityField(name = "流程属性列表", type = ApiParamType.JSONARRAY)
	private List<ProcessStepRelVo> stepRelList;

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

	public Long getType() {
		return type;
	}

	public void setType(Long type) {
		this.type = type;
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
		if ( this.getConfigObj() != null) {
			if (this.getConfigObj().containsKey("userData")) {
				JSONObject userData = this.getConfigObj().getJSONObject("userData");
				if (userData.containsKey("formId")) {
					this.setFormUuid(userData.getString("formId"));
				}
			}

			if (this.getConfigObj().containsKey("elementList")) {
				this.stepList = new ArrayList<>();
				JSONArray elementList = this.getConfigObj().getJSONArray("elementList");
				for (int i = 0; i < elementList.size(); i++) {
					JSONObject elementObj = elementList.getJSONObject(i);
					if (elementObj.containsKey("userData")) {
						IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(elementObj.getString("type"));
						if (handler != null) {
							/** 组装步骤信息 **/
							JSONObject userData = elementObj.getJSONObject("userData");
							ProcessStepVo processStepVo = new ProcessStepVo();
							processStepVo.setProcessUuid(this.getUuid());
							processStepVo.setUuid(elementObj.getString("id"));
							processStepVo.setName(userData.getString("name"));
							userData.remove("name");
							processStepVo.setHandler(elementObj.getString("type"));
							processStepVo.setDescription(userData.getString("description"));
							userData.remove("description");
							userData.remove("viewPage");
							if (elementObj.getString("type").equals("end")) {
								processStepVo.setType(ProcessStepType.END.getValue());
							} else {
								// if(!userData.containsKey("condition")) {
								if (userData.containsKey("isStartNode") && userData.getString("isStartNode").equals("1")) {
									processStepVo.setType(ProcessStepType.START.getValue());
								} else {
									processStepVo.setType(ProcessStepType.PROCESS.getValue());
								}
								// }else {
								// processStepVo.setType(ProcessStepType.CONDITION.getValue());
								// }
							}
							userData.remove("attributeList");
							/** 组装表单属性 **/
							if (userData.containsKey("formAttributeList")) {
								JSONArray attributeObjList = userData.getJSONArray("formAttributeList");
								List<ProcessStepFormAttributeVo> processStepFormAttributeList = new ArrayList<>();
								for (int j = 0; j < attributeObjList.size(); j++) {
									JSONObject attributeObj = attributeObjList.getJSONObject(j);
									ProcessStepFormAttributeVo processStepFormAttributeVo = new ProcessStepFormAttributeVo();
									processStepFormAttributeVo.setProcessUuid(this.getUuid());
									processStepFormAttributeVo.setFormUuid(this.getFormUuid());
									processStepFormAttributeVo.setProcessStepUuid(processStepVo.getUuid());
									processStepFormAttributeVo.setAttributeUuid(attributeObj.getString("uuid"));
									processStepFormAttributeVo.setConfig(attributeObj.getString("config"));
									processStepFormAttributeVo.setData(attributeObj.getString("data"));
									processStepFormAttributeVo.setIsEditable(attributeObj.getInteger("isEditable"));
									// processStepFormAttributeVo.setIsRequired(attributeObj.optInt("isRequired"));

									processStepFormAttributeList.add(processStepFormAttributeVo);
								}
								processStepVo.setFormAttributeList(processStepFormAttributeList);
							}
							userData.remove("formAttributeList");
							/** 组装分配策略 **/
							if (userData.containsKey("workerPolicyList")) {
								JSONArray workerPolicyObjList = userData.getJSONArray("workerPolicyList");
								List<ProcessStepWorkerPolicyVo> processStepWorkerPolicyList = new ArrayList<>();
								for (int j = 0; j < workerPolicyObjList.size(); j++) {
									JSONObject workerPolicyObj = workerPolicyObjList.getJSONObject(j);
									ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
									processStepWorkerPolicyVo.setProcessUuid(this.getUuid());
									processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
									processStepWorkerPolicyVo.setPolicy(workerPolicyObj.getString("policy"));
									processStepWorkerPolicyVo.setSort(j);
									processStepWorkerPolicyVo.setConfig(workerPolicyObj.getString("config"));
									processStepWorkerPolicyList.add(processStepWorkerPolicyVo);
								}
								processStepVo.setWorkerPolicyList(processStepWorkerPolicyList);
							}
							userData.remove("workerPolicyList");
							/** 装配超时策略 **/
							if (userData.containsKey("timeoutPolicyList")) {
								JSONArray timeoutPolicyObjList = userData.getJSONArray("timeoutPolicyList");
								List<ProcessStepTimeoutPolicyVo> processStepTimeoutPolicyList = new ArrayList<>();
								for (int j = 0; j < timeoutPolicyObjList.size(); j++) {
									JSONObject timeoutPolicyObj = timeoutPolicyObjList.getJSONObject(j);
									ProcessStepTimeoutPolicyVo processStepTimeoutPolicyVo = new ProcessStepTimeoutPolicyVo();
									processStepTimeoutPolicyVo.setProcessUuid(this.getUuid());
									processStepTimeoutPolicyVo.setProcessStepUuid(processStepVo.getUuid());
									processStepTimeoutPolicyVo.setPolicy(timeoutPolicyObj.getString("policy"));
									processStepTimeoutPolicyVo.setSort(j);
									processStepTimeoutPolicyVo.setTime(timeoutPolicyObj.getInteger("time"));
									processStepTimeoutPolicyVo.setConfig(timeoutPolicyObj.getString("config"));
									processStepTimeoutPolicyList.add(processStepTimeoutPolicyVo);
								}
								processStepVo.setTimeoutPolicyList(processStepTimeoutPolicyList);

							}
							userData.remove("timeoutPolicyList");
							processStepVo.setConfig(userData.toJSONString());
							this.stepList.add(processStepVo);

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
					processStepRelVo.setFromStepUuid(relObj.getString("from"));
					processStepRelVo.setToStepUuid(relObj.getString("to"));
					processStepRelVo.setUuid(relObj.getString("id"));
					processStepRelVo.setProcessUuid(this.getUuid());
					if (relObj.containsKey("condition")) {
						processStepRelVo.setCondition(relObj.getString("condition"));
					} else if (relObj.containsKey("userData")) {
						if (relObj.get("userData") instanceof JSONObject) {
							if (relObj.getJSONObject("userData").containsKey("value")) {
								processStepRelVo.setCondition(relObj.getJSONObject("userData").getString("value"));
							}
						}
					}
					stepRelList.add(processStepRelVo);
				}
			}

		}

	}

	public void setStepList(List<ProcessStepVo> stepList) {
		this.stepList = stepList;
	}

	public String getBelong() {
		return belong;
	}

	public void setBelong(String belong) {
		this.belong = belong;
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
}
