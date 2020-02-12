package codedriver.module.process.notify.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dto.UserVo;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.module.process.utils.FreemarkerUtil;

public class NotifyVo {
	private String title;
	private String content;
	private List<UserVo> toUserList;
	private List<String> toUserIdList;
	private List<String> toTeamIdList;
	private String fromUser;
	private String fromUserEmail;
	private JSONObject data = new JSONObject();
	private String templateContent;
	private String templateTitle;
	private NotifyTriggerType trigger;

	private NotifyVo(Builder builder) {
		this.templateTitle = builder.templateTitle;
		this.templateContent = builder.templateContent;
		this.data = builder.data;
		this.toUserIdList = toUserIdList;
		this.toTeamIdList = toTeamIdList;
	}

	private NotifyVo() {

	}

	public void addUser(UserVo userVo) {
		if (toUserList == null) {
			toUserList = new ArrayList<>();
		}
		if (!toUserList.contains(userVo)) {
			toUserList.add(userVo);
		}
	}

	public String getTitle() {
		if (StringUtils.isBlank(title) && StringUtils.isNotBlank(this.getTemplateTitle())) {
			title = FreemarkerUtil.getNotifyContent(this.getData(), this.getTemplateTitle());
		}
		return title;
	}

	public String getContent() {
		if (StringUtils.isBlank(content) && StringUtils.isNotBlank(this.getTemplateContent())) {
			content = FreemarkerUtil.getNotifyContent(this.getData(), this.getTemplateContent());
		}
		return content;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getTemplateContent() {
		if (StringUtils.isBlank(templateContent)) {
			if (trigger != null) {
				templateContent = trigger.getContentTemplate();
			}
		}
		return templateContent;
	}

	public String getTemplateTitle() {
		if (StringUtils.isBlank(templateTitle)) {
			if (trigger != null) {
				templateTitle = trigger.getTitleTemplate();
			}
		}
		return templateTitle;
	}

	public List<UserVo> getToUserList() {
		return toUserList;
	}

	public void setToUserList(List<UserVo> toUserList) {
		this.toUserList = toUserList;
	}

	public List<String> getToUserIdList() {
		return toUserIdList;
	}

	public JSONObject getData() {
		return data;
	}

	public String getFromUserEmail() {
		return fromUserEmail;
	}

	public void setFromUserEmail(String fromUserEmail) {
		this.fromUserEmail = fromUserEmail;
	}

	public List<String> getToTeamIdList() {
		return toTeamIdList;
	}

	public NotifyTriggerType getTrigger() {
		return trigger;
	}

	public static class Builder {
		// 必要参数
		private final NotifyTriggerType trigger;

		// 可选参数
		private String templateContent;
		private String templateTitle;
		private JSONObject data = new JSONObject();
		private List<String> toUserIdList;
		private List<String> toTeamIdList;

		public Builder(NotifyTriggerType _trigger) {
			this.trigger = _trigger;
		}

		public Builder withContentTemplate(String contentTemplate) {
			templateContent = contentTemplate;
			return this;
		}

		public Builder withTitleTemplate(String titleTemplate) {
			templateTitle = titleTemplate;
			return this;
		}

		public Builder addData(String key, Object value) {
			data.put(key, value);
			return this;
		}

		public NotifyVo build() {
			return new NotifyVo(this);
		}

		public Builder addUserId(String userId) {
			if (toUserIdList == null) {
				toUserIdList = new ArrayList<>();
			}
			if (!toUserIdList.contains(userId)) {
				toUserIdList.add(userId);
			}
			return this;
		}

		public Builder addTeamId(String teamId) {
			if (toTeamIdList == null) {
				toTeamIdList = new ArrayList<>();
			}
			if (!toTeamIdList.contains(teamId)) {
				toTeamIdList.add(teamId);
			}
			return this;
		}
	}
}
