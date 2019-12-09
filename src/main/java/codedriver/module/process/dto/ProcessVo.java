package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.module.process.constvalue.ProcessStepType;

public class ProcessVo implements Serializable {
	private static final long serialVersionUID = 4684015408674741157L;
	private String uuid;
	private String name;
	private String type;
	private String typeName;
	private Integer isActive = 1;
	private String config;
	private String belong;
	private JSONObject configObj;
	private JSONArray attributeObjList;
	private String formUuid;
	private List<ProcessStepVo> stepList;
	private List<ProcessAttributeVo> attributeList;
	private List<ProcessStepRelVo> stepRelList;
	private boolean isAttributeListSorted = false;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
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
		if (this.attributeList == null && this.getConfigObj() != null) {
			if (this.getConfigObj().containsKey("userData")) {
				JSONObject userData = this.getConfigObj().getJSONObject("userData");
				if (userData.containsKey("attributeList")) {
					this.attributeList = new ArrayList<>();
					JSONArray attributeObjList = userData.getJSONArray("attributeList");
					for (int i = 0; i < attributeObjList.size(); i++) {
						JSONObject attributeObj = attributeObjList.getJSONObject(i);
						ProcessAttributeVo processAttributeVo = new ProcessAttributeVo();
						processAttributeVo.setProcessUuid(this.getUuid());
						processAttributeVo.setAttributeUuid(attributeObj.getString("uuid"));
						processAttributeVo.setLabel(attributeObj.getString("label"));
						processAttributeVo.setGroup(attributeObj.getString("group"));
						processAttributeVo.setSort(i);
						this.attributeList.add(processAttributeVo);
					}
				}
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
							if (StringUtils.isNotBlank(userData.getString("editPage"))) {
								processStepVo.setEditPage(userData.getString("editPage"));
							}
							userData.remove("editPage");
							if (StringUtils.isNotBlank(userData.getString("viewPage"))) {
								processStepVo.setViewPage(userData.getString("viewPage"));
							}
							userData.remove("viewPage");
							if (elementObj.getString("type").equals("end")) {
								processStepVo.setType(ProcessStepType.END.getValue());
							} else {
								if (userData.getString("isStartNode").equals("1")) {
									processStepVo.setType(ProcessStepType.START.getValue());
								} else {
									processStepVo.setType(ProcessStepType.PROCESS.getValue());
								}
							}
							/** 组装自定义属性信息 **/
							if (userData.containsKey("attributeList")) {
								JSONArray attributeObjList = userData.getJSONArray("attributeList");
								List<ProcessStepAttributeVo> processStepAttributeList = new ArrayList<>();
								for (int j = 0; j < attributeObjList.size(); j++) {
									JSONObject attributeObj = attributeObjList.getJSONObject(j);
									ProcessStepAttributeVo processStepAttributeVo = new ProcessStepAttributeVo();
									processStepAttributeVo.setProcessUuid(this.getUuid());
									processStepAttributeVo.setProcessStepUuid(processStepVo.getUuid());
									processStepAttributeVo.setAttributeUuid(attributeObj.getString("uuid"));
									processStepAttributeVo.setConfig(attributeObj.getString("config"));
									processStepAttributeVo.setData(attributeObj.getString("data"));
									processStepAttributeVo.setIsEditable(attributeObj.getInteger("isEditable"));
									// processStepAttributeVo.setIsRequired(attributeObj.optInt("isRequired"));

									processStepAttributeList.add(processStepAttributeVo);
								}
								processStepVo.setAttributeList(processStepAttributeList);
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
							processStepVo.setConfig(userData.toJSONString(4));
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

	public List<ProcessAttributeVo> getAttributeList() {
		if (attributeList != null && attributeList.size() > 0 && !this.isAttributeListSorted) {
			Collections.sort(attributeList);
			this.isAttributeListSorted = true;
		}
		return attributeList;
	}

	public void setAttributeList(List<ProcessAttributeVo> attributeList) {
		this.attributeList = attributeList;
	}

	public JSONArray getAttributeObjList() {
		if (this.attributeList != null && this.attributeList.size() > 0) {
			this.attributeObjList = new JSONArray();
			for (ProcessAttributeVo attributeVo : this.attributeList) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("uuid", attributeVo.getAttributeUuid());
				jsonObj.put("name", attributeVo.getName());
				jsonObj.put("label", attributeVo.getLabel());
				jsonObj.put("width", attributeVo.getWidth());
				jsonObj.put("group", attributeVo.getGroup());
				jsonObj.put("sort", attributeVo.getSort());
				jsonObj.put("typeName", attributeVo.getTypeName());
				jsonObj.put("handlerName", attributeVo.getHandlerName());
				this.attributeObjList.add(jsonObj);

			}
		}
		return attributeObjList;
	}

	public void setAttributeObjList(JSONArray attributeObjList) {
		this.attributeObjList = attributeObjList;
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
}
