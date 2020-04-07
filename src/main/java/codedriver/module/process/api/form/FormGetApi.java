package codedriver.module.process.api.form;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.FormVo;
import codedriver.framework.process.exception.form.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

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
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"), 
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
		String currentVersionUuid = jsonObj.getString("currentVersionUuid");
		if(StringUtils.isNotBlank(currentVersionUuid)) {					
			formVersion = formMapper.getFormVersionByUuid(currentVersionUuid);
			//判断表单版本是否存在
			if(formVersion == null) {
				throw new FormVersionNotFoundException(uuid);
			}
			if(!uuid.equals(formVersion.getFormUuid())) {
				throw new FormIllegalParameterException("表单版本：'" + currentVersionUuid + "'不属于表单：'" + uuid + "'的版本");
			}
			formVo.setCurrentVersionUuid(currentVersionUuid);
		}else {//获取激活版本
			formVersion = formMapper.getActionFormVersionByFormUuid(uuid);
			if(formVersion == null) {
				throw new FormActiveVersionNotFoundExcepiton(uuid);
			}
			formVo.setCurrentVersionUuid(formVersion.getUuid());
		}
		//表单内容
		formVo.setFormConfig(formVersion.getFormConfig());
		//表单版本列表
		List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(uuid);
		formVo.setVersionList(formVersionList);
		//引用数量
		int count = formMapper.getFormReferenceCount(uuid);
		formVo.setReferenceCount(count);
		return formVo;
	}

}
