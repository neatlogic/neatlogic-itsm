package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

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
	private String editTime;
	private List<FormAttributeVo> formAttributeList;

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

	public String getEditTime() {
		return editTime;
	}

	public void setEditTime(String editTime) {
		this.editTime = editTime;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public List<FormAttributeVo> getFormAttributeList() {
		if (StringUtils.isNotBlank(this.content)) {
			try {
				JSONObject contentObj = JSONObject.parseObject(this.content);
				if (contentObj.containsKey("cells") && contentObj.get("cells") instanceof JSONObject) {
					JSONObject cellObj = contentObj.getJSONObject("cells");
					Iterator it = cellObj.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next().toString();
						JSONObject obj = cellObj.getJSONObject(key);
						if (obj.containsKey("data")) {
							JSONObject dataObj = obj.getJSONObject("data");
							if (dataObj.containsKey("uuid") && StringUtils.isNotBlank(dataObj.getString("uuid"))) {
								if (formAttributeList == null) {
									formAttributeList = new ArrayList<>();
								}
								formAttributeList.add(new FormAttributeVo(this.getFormUuid(), this.getUuid(), dataObj.getString("uuid"), dataObj.toString()));
							}
						}
					}
				}
			} catch (Exception ex) {

			}
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

}
