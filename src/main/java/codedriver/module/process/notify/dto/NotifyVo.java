package codedriver.module.process.notify.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dto.UserVo;
import codedriver.framework.process.notify.core.INotifyHandler;
import codedriver.framework.process.notify.core.NotifyHandlerFactory;

public class NotifyVo {
	private String title;
	private String content;
	private String handler;
	private List<UserVo> toUserList;
	private List<String> toUserIdList;
	private String fromUser;
	private String fromUserEmail;
	private JSONObject data = new JSONObject();
	private String templateContent;
	private String templateTitle;

	public void addUser(UserVo userVo) {
		if (toUserList == null) {
			toUserList = new ArrayList<>();
		}
		if (!toUserList.contains(userVo)) {
			toUserList.add(userVo);
		}
	}

	public void addUserId(String userId) {
		if (toUserIdList == null) {
			toUserIdList = new ArrayList<>();
		}
		if (!toUserIdList.contains(userId)) {
			toUserIdList.add(userId);
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public void addData(String key, Object value) {
		data.put(key, value);
	}

	public String getTemplateContent() {
		if (StringUtils.isBlank(templateContent) && StringUtils.isNotBlank(handler)) {
			INotifyHandler notifyhandler = NotifyHandlerFactory.getHandler(handler);
			templateContent = notifyhandler.getTemplateContent();
		}
		return templateContent;
	}

	public void setTemplateContent(String templateContent) {
		this.templateContent = templateContent;
	}

	public String getTemplateTitle() {
		if (StringUtils.isBlank(templateTitle) && StringUtils.isNotBlank(handler)) {
			INotifyHandler notifyhandler = NotifyHandlerFactory.getHandler(handler);
			templateTitle = notifyhandler.getTemplateTitle();
		}
		return templateTitle;
	}

	public void setTemplateTitle(String templateTitle) {
		this.templateTitle = templateTitle;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
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

	public void setToUserIdList(List<String> toUserIdList) {
		this.toUserIdList = toUserIdList;
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
}
