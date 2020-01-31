package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.dto.BasePageVo;

public class FormVersionVo extends BasePageVo implements Serializable {
	private static final long serialVersionUID = 8345592242508980127L;
	private String uuid;
	private String formName;
	private String formUuid;
	private Integer version;
	private Integer isActive;
	private String content;
	private String editor;
	private Date editTime;
	private transient List<FormAttributeVo> formAttributeList;

	public String getUuid() {
		if (StringUtils.isBlank(uuid)) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEditor() {
		if (StringUtils.isBlank(editor)) {
			editor = UserContext.get().getUserId();
		}
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public Date getEditTime() {
		return editTime;
	}

	public void setEditTime(Date editTime) {
		this.editTime = editTime;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public List<FormAttributeVo> getFormAttributeList() {
		if(formAttributeList != null) {
			return formAttributeList;
		}
		if(StringUtils.isBlank(this.content)) {
			return null;
		}
		JSONObject contentObj = JSONObject.parseObject(this.content);
		if(contentObj == null || contentObj.isEmpty()) {
			return null;
		}
		
		if(!contentObj.containsKey("formConfig")) {
			return null;
		}
		JSONObject formConfig = contentObj.getJSONObject("formConfig");
		if(formConfig == null || !formConfig.containsKey("pluginList")) {
			return null;
		}
		JSONArray pluginList = formConfig.getJSONArray("pluginList");
		if(pluginList == null || pluginList.isEmpty()) {
			return null;
		}
		formAttributeList = new ArrayList<>();
		for(int i = 0; i < pluginList.size(); i++) {
			JSONObject pluginObj = pluginList.getJSONObject(i);
			if(!pluginObj.containsKey("config")) {
				continue;
			}
			JSONObject config = pluginObj.getJSONObject("config");
			if(config == null || config.isEmpty()) {
				continue;
			}
			formAttributeList.add(new FormAttributeVo(this.getFormUuid(), this.getUuid(), uuid, config.getString("label"), "system", config.getString("type"), pluginObj.toJSONString()));
		}
		return formAttributeList;
	}

	public void setFormAttributeList(List<FormAttributeVo> formAttributeList) {
		this.formAttributeList = formAttributeList;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	@Override
	public String toString() {
		return "FormVersionVo [uuid=" + uuid + ", formName=" + formName + ", formUuid=" + formUuid + ", version=" + version + ", isActive=" + isActive + ", content=" + content + ", editor=" + editor + ", editTime=" + editTime + "]";
	}

}
