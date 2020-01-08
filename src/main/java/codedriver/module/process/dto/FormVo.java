package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.EntityField;

public class FormVo extends BasePageVo implements Serializable {
	
	private static final long serialVersionUID = -2319081254327257337L;
	
	@EntityField(name = "表单uuid", type = ApiParamType.STRING)
	private String uuid;
	@EntityField(name = "表单名称", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "是否激活", type = ApiParamType.INTEGER)
	private Integer isActive;
	@EntityField(name = "激活版本", type = ApiParamType.INTEGER)
	private Integer activeVersion;
	@EntityField(name = "激活版本uuid", type = ApiParamType.STRING)
	private String activeVersionUuid;
	@EntityField(name = "表单内容（表单编辑器使用）", type = ApiParamType.STRING)
	private String content;
	
	private List<FormVersionVo> versionList;
	private transient String keyword;
	
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

	public Integer getActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(Integer activeVersion) {
		this.activeVersion = activeVersion;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
