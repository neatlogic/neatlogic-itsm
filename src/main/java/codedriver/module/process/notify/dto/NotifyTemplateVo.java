package codedriver.module.process.notify.dto;

import java.util.UUID;

import codedriver.framework.common.dto.BasePageVo;

public class NotifyTemplateVo extends BasePageVo {

	private String uuid;
	private String name;
	private String title;
	private String content;
	private String type;

	private transient String keyword;

	public String getUuid() {
		if (uuid == null) {
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
