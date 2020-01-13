package codedriver.module.process.api.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
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
			@Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "选择表单版本uuid"), 
			})
	@Output({ @Param(explode = FormVo.class) })
	@Description(desc = "单个表单查询接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		FormVo formVo = formMapper.getFormByUuid(uuid);
		//判断表单是否存在
		if (formVo == null) {
			throw new FormNotFoundException(uuid);
		}
		FormVersionVo formVersion = null;
		if(jsonObj.containsKey("currentVersionUuid")) {
			String currentVersionUuid = jsonObj.getString("currentVersionUuid");			
			formVersion = formMapper.getFormVersionByUuid(currentVersionUuid);
			//判断表单版本是否存在
			if(formVersion == null) {
				throw new FormVersionNotFoundException(uuid);
			}
			formVo.setCurrentVersionUuid(currentVersionUuid);
		}else {//获取激活版本
			formVersion = formMapper.getActionFormVersionByFormUuid(uuid);
			formVo.setUuid(formVersion.getUuid());
		}
		//表单内容
		formVo.setContent(formVersion.getContent());
		//表单版本列表
		List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);		
		if (formVersionList != null && formVersionList.size() > 0) {
			for (FormVersionVo version : formVersionList) {
				version.setContent(null);
				version.setEditTime(null);
				version.setFormAttributeList(null);
				version.setFormName(null);
				version.setFormUuid(null);
			}
			formVo.setVersionList(formVersionList);
		}
		//引用数量
		int count = formMapper.getFormReferenceCount(uuid);
		formVo.setReferenceCount(count);
		return formVo;
	}

}
