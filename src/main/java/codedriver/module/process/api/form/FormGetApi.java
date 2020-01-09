package codedriver.module.process.api.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.FormVersionVo;
import codedriver.module.process.dto.FormVo;

@Service
public class FormGetApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;

	@Override
	public String getToken() {
		return "process/form/get";
	}

	@Override
	public String getName() {
		return "单个表单查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid", isRequired = true), 
			@Param(name = "selectVersionUuid", type = ApiParamType.STRING, desc = "选择表单版本uuid"), 
			})
	@Output({ @Param(explode = FormVo.class) })
	@Description(desc = "单个表单查询接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		FormVo formVo = formMapper.getFormByUuid(uuid);
		if (formVo == null) {
			throw new FormNotFoundException(uuid);
		}
		String selectVersionUuid = null;
		if(jsonObj.containsKey("selectVersionUuid")) {
			selectVersionUuid = jsonObj.getString("selectVersionUuid");
		}
		List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);
		
		if (formVersionList != null && formVersionList.size() > 0) {
			for (FormVersionVo version : formVersionList) {
				if(selectVersionUuid != null) {
					if (selectVersionUuid.equals(version.getUuid())) {
						formVo.setContent(version.getContent());
					}
				}else {
					if (version.getIsActive().equals(1)) {
						formVo.setContent(version.getContent());
					}
				}
				if (version.getIsActive().equals(1)) {
					formVo.setActiveVersionUuid(version.getUuid());
				}
				version.setContent(null);
				version.setEditTime(null);
				version.setFormAttributeList(null);
				version.setFormName(null);
				version.setFormUuid(null);
			}
			formVo.setVersionList(formVersionList);
		}
		int count = formMapper.getFormReferenceCount(uuid);
		formVo.setReferenceCount(count);
		return formVo;
	}

}
