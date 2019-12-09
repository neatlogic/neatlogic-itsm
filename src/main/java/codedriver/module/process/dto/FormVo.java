package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import codedriver.framework.common.dto.BasePageVo;

public class FormVo extends BasePageVo implements Serializable {
	private static final long serialVersionUID = -2319081254327257337L;
	private String uuid;
	private String name;
	private Integer isActive;
	private String content;
	private List<FormVersionVo> versionList;
	private String activeVersionUuid;
	private String description;

	public String getUuid() {
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

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	public List<FormVersionVo> getVersionList() {
		return versionList;
	}

	public void setVersionList(List<FormVersionVo> versionList) {
		this.versionList = versionList;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getActiveVersionUuid() {
		return activeVersionUuid;
	}

	public void setActiveVersionUuid(String activeVersionUuid) {
		this.activeVersionUuid = activeVersionUuid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
