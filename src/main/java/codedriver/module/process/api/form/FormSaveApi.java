package codedriver.module.process.api.form;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.form.FormNameRepeatException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.FormAttributeVo;
import codedriver.module.process.dto.FormVersionVo;
import codedriver.module.process.dto.FormVo;

@Service
@Transactional
@AuthAction(name = "FORM_MODIFY")
@IsActived
public class FormSaveApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;

	@Override
	public String getToken() {
		return "process/form/save";
	}

	@Override
	public String getName() {
		return "表单保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid", isRequired = true),
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, length = 50, desc = "表单名称"),
			//@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "是否激活", isRequired = true),
			@Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "当前版本的uuid，为空代表创建一个新版本", isRequired = false),
			@Param(name = "formConfig", type = ApiParamType.JSONOBJECT, desc = "表单控件生成的json内容", isRequired = true) 
			})
	@Output({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid"),
			@Param(name = "formVersionUuid", type = ApiParamType.STRING, desc = "表单版本uuid")
			})
	@Description(desc = "表单保存接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		FormVo formVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<FormVo>() {});
		if(formMapper.checkFormNameIsRepeat(formVo) > 0) {
			throw new FormNameRepeatException(formVo.getName());
		}
		//判断表单是否存在
		if(formMapper.checkFormIsExists(formVo.getUuid()) == 0) {
			//插入表单信息
			formMapper.insertForm(formVo);		
		}else {
			//更新表单信息
			formMapper.updateForm(formVo);
		}
				
		//插入表单版本信息
		FormVersionVo formVersionVo = new FormVersionVo();
		formVersionVo.setFormConfig(formVo.getFormConfig());
		formVersionVo.setFormUuid(formVo.getUuid());
		//formMapper.resetFormVersionIsActiveByFormUuid(formVo.getUuid());
		//formVersionVo.setIsActive(1);
		if (StringUtils.isBlank(formVo.getCurrentVersionUuid())) {
			Integer version = formMapper.getMaxVersionByFormUuid(formVo.getUuid());
			if (version == null) {//当前表单没有激活版本时，设置当前版本号为1，且为激活版本
				version = 1;
				formVersionVo.setIsActive(1);
			} else {
				version += 1;
			}
			formVersionVo.setVersion(version);
			formMapper.insertFormVersion(formVersionVo);
		} else {
			FormVersionVo formVersion = formMapper.getFormVersionByUuid(formVo.getCurrentVersionUuid());
			if(formVersion == null) {
				throw new FormVersionNotFoundException(formVo.getCurrentVersionUuid());
			}
			if(!formVo.getUuid().equals(formVersion.getFormUuid())) {
				throw new FormIllegalParameterException("表单版本：'" + formVo.getCurrentVersionUuid() + "'不属于表单：'" + formVo.getUuid() + "'的版本");
			}
			formVersionVo.setUuid(formVo.getCurrentVersionUuid());
			formMapper.updateFormVersion(formVersionVo);
		}
		//更新表单属性信息
		formMapper.deleteFormAttributeByFormUuid(formVo.getUuid());
		List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
		if (formAttributeList != null && formAttributeList.size() > 0) {
			for (FormAttributeVo formAttributeVo : formAttributeList) {
				formMapper.insertFormAttribute(formAttributeVo);
			}
		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("uuid", formVo.getUuid());
		resultObj.put("currentVersionUuid", formVersionVo.getUuid());
		resultObj.put("currentVersion", formVersionVo.getVersion());
		return resultObj;
	}

}
