package codedriver.framework.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.module.process.constvalue.ProcessTaskEvent;

/**
 * 
 * @Author:chenqiwei
 * @Time:2018年9月20日
 * @ClassName: FlowJobStepEventVo
 * @Description: 流程步骤事件实体类
 */
public class ProcessTaskEventVo implements Serializable {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 1245435681393986797L;
	private Long id;
	private String uid;
	private Long flowId;
	private Long stepId;
	private Long templateId;
	private String templateName;
	private String event;
	private String executerName;
	private String executerShowName;
	private String content;
	private String title;
	private String fromUser;
	private List<ProcessTaskEventUserVo> flowStepEventUserList;
	private List<ProcessTaskEventTeamVo> flowStepEventTeamList;
	private List<String> userIdList;
	private List<String> userTeamIdList;
	private List<String> teamUserIdList;
	private JSONObject flowStepEventJSONObj = new JSONObject();

	public List<String> getUserIdList() {
		if (teamUserIdList != null && teamUserIdList.size() > 0) {
			List<String> userList = new ArrayList<>();
			userList.addAll(userIdList);
			userList.addAll(teamUserIdList);
			List<String> aa = userList.stream().distinct().collect(Collectors.toList());
			return aa;
		}
		return userIdList;
	}

	public void setUserIdList(List<String> userIdList) {
		this.userIdList = userIdList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ProcessTaskEventUserVo> getFlowStepEventUserList() {
		return flowStepEventUserList;
	}

	public void setFlowStepEventUserList(List<ProcessTaskEventUserVo> flowStepEventUserList) {
		this.flowStepEventUserList = flowStepEventUserList;
	}

	public List<ProcessTaskEventTeamVo> getFlowStepEventTeamList() {
		return flowStepEventTeamList;
	}

	public void setFlowStepEventTeamList(List<ProcessTaskEventTeamVo> flowStepEventTeamList) {
		this.flowStepEventTeamList = flowStepEventTeamList;
	}

	public Long getFlowId() {
		return flowId;
	}

	public void setFlowId(Long flowId) {
		this.flowId = flowId;
	}

	public Long getStepId() {
		return stepId;
	}

	public void setStepId(Long stepId) {
		this.stepId = stepId;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getExecuterName() {
		return executerName;
	}

	public void setExecuterName(String executerName) {
		this.executerName = executerName;
	}

	public String getExecuterShowName() {
		// TODO 补充原来的逻辑
		
		/*if (executerName != null) {
			List<ActionExecutorVo> actionExecutorList = ActionExecutorFactory.getActionExecutorVoList();
			for (ActionExecutorVo actionExecutorVo : actionExecutorList) {
				if (executerName.equals(actionExecutorVo.getName())) {
					return actionExecutorVo.getDisplayName();
				}
			}
		}*/
		return null;
	}

	public void setExecuterShowName(String executerShowName) {
		this.executerShowName = executerShowName;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<String> getUserTeamIdList() {
		return userTeamIdList;
	}

	public void setUserTeamIdList(List<String> userTeamIdList) {
		this.userTeamIdList = userTeamIdList;
	}

	public List<String> getTeamUserIdList() {
		return teamUserIdList;
	}

	public void setTeamUserIdList(List<String> teamUserIdList) {
		this.teamUserIdList = teamUserIdList;
	}

	/*
	 * public void transformObjectFromJSON() { if
	 * (!(flowStepEventJSONObj.isNullObject())) { this.fromUser =
	 * flowStepEventJSONObj.getString("sender"); this.event =
	 * flowStepEventJSONObj.getString("eventValue"); this.templateId =
	 * flowStepEventJSONObj.getLong("templateId"); this.executerName =
	 * flowStepEventJSONObj.getString("pluginValue"); this.uid =
	 * flowStepEventJSONObj.getString("stepId"); this.id =
	 * flowStepEventJSONObj.getLong("eventId"); JSONArray userIdArray =
	 * flowStepEventJSONObj.optJSONArray("receiverValue"); JSONArray
	 * userTeamIdArray = flowStepEventJSONObj.optJSONArray("receiveGroupValue");
	 * this.userIdList = new ArrayList<>(); this.userTeamIdList = new
	 * ArrayList<>(); if (userIdArray != null) { for (int i = 0; i <
	 * userIdArray.size(); i++) {
	 * this.userIdList.add(userIdArray.get(i).toString()); } } if
	 * (userTeamIdArray != null) { for (int i = 0; i < userTeamIdArray.size();
	 * i++) { this.userTeamIdList.add(userTeamIdArray.get(i).toString()); } } }
	 * }
	 */

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof ProcessTaskEventVo)) {
			return false;
		}

		ProcessTaskEventVo flowStepEventVo = (ProcessTaskEventVo) other;
		return id.equals(flowStepEventVo.getId());
	}

	@Override
	public int hashCode() {
		int result = 17;
		if (event != null && stepId != null && executerName != null && templateName != null) {
			result = result * 31 + event.hashCode();
			result = result * 31 + stepId.intValue();
			result = result * 31 + executerName.hashCode();
			result = result * 31 + templateId.intValue();
		}
		return result;
	}

	public void setFlowStepEventJSONObj(JSONObject flowStepEventJSONObj) {
		this.flowStepEventJSONObj = flowStepEventJSONObj;
	}

	public JSONObject getFlowStepEventJSONObj() {
		flowStepEventJSONObj.put("sender", fromUser);
		flowStepEventJSONObj.put("eventValue", event);
		flowStepEventJSONObj.put("eventText", ProcessTaskEvent.getText(event));
		flowStepEventJSONObj.put("templateId", templateId);
		flowStepEventJSONObj.put("templateText", templateName);
		flowStepEventJSONObj.put("pluginValue", executerName);
		flowStepEventJSONObj.put("pluginText", getExecuterShowName());
		flowStepEventJSONObj.put("stepId", uid);
		flowStepEventJSONObj.put("eventId", id);
		JSONArray eventUserIdArray = new JSONArray();
		JSONArray eventUserShowNameArray = new JSONArray();
		StringBuffer eventUserShowNameBuffer = new StringBuffer();
		boolean userSign = true;
		for (ProcessTaskEventUserVo flowStepEventUserVo : this.flowStepEventUserList) {
			String userShowName = flowStepEventUserVo.getUserShowName();
			String userId = flowStepEventUserVo.getUserId();
			if (userId != null) {
				eventUserIdArray.add(userId);
				eventUserShowNameArray.add(userShowName);
				if (eventUserShowNameBuffer.length() > 0) {
					eventUserShowNameBuffer.append(";" + userShowName);
				} else {
					eventUserShowNameBuffer.append(userShowName);
				}
			} else {
				userSign = false;
			}
		}
		if (userSign && this.flowStepEventUserList.size() > 0) {
			flowStepEventJSONObj.put("receiverValue", eventUserIdArray);
			flowStepEventJSONObj.put("receiverText", eventUserShowNameBuffer.toString());
		}
		JSONArray eventUserTeamIdArray = new JSONArray();
		JSONArray eventUserTeamNameArray = new JSONArray();
		StringBuffer eventUserTeamNameBuffer = new StringBuffer();
		boolean teamSign = true;
		for (ProcessTaskEventTeamVo teamVo : this.flowStepEventTeamList) {
			String userTeamName = teamVo.getTeamName();
			Long teamId = teamVo.getTeamId();
			if (teamId != null) {
				eventUserTeamIdArray.add(teamId);
				eventUserTeamNameArray.add(userTeamName);
				if (eventUserTeamNameBuffer.length() > 0) {
					eventUserTeamNameBuffer.append(";" + userTeamName);
				} else {
					eventUserTeamNameBuffer.append(userTeamName);
				}
			} else {
				teamSign = false;
			}
		}
		if (teamSign && this.flowStepEventTeamList.size() > 0) {
			flowStepEventJSONObj.put("receiveGroupValue", eventUserTeamIdArray);
			flowStepEventJSONObj.put("receiveGroupText", eventUserTeamNameArray);
		}
		this.flowStepEventJSONObj = flowStepEventJSONObj;
		return flowStepEventJSONObj;
	}
}
