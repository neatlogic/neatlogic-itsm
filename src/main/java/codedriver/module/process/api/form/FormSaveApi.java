package codedriver.module.process.api.form;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.matrix.ProcessMatrixMapper;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.FormVo;
import codedriver.framework.process.dto.matrix.ProcessMatrixFormComponentVo;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.form.FormNameRepeatException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@Transactional
@AuthAction(name = "FORM_MODIFY")
@OperationType(type = OperationTypeEnum.CREATE)
public class FormSaveApi extends PrivateApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Autowired
	private ProcessMatrixMapper matrixMapper;

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
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "表单名称"),
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
			if (version == null) {//如果表单没有激活版本时，设置当前版本号为1，且为激活版本
				version = 1;
				formVersionVo.setIsActive(1);
			} else {
				version += 1;
				formVersionVo.setIsActive(0);
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
			formVersionVo.setIsActive(formVersion.getIsActive());
			formMapper.updateFormVersion(formVersionVo);
		}
		//保存激活版本时，更新表单属性信息
		if(Objects.equal(formVersionVo.getIsActive(), 1)) {
			formMapper.deleteFormAttributeByFormUuid(formVo.getUuid());
			List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
			if (CollectionUtils.isNotEmpty(formAttributeList)) {
				for (FormAttributeVo formAttributeVo : formAttributeList) {
					formMapper.insertFormAttribute(formAttributeVo);
				}
			}
		}
		
		List<ProcessMatrixFormComponentVo> processMatrixFormComponentList = formVersionVo.getProcessMatrixFormComponentList();
		formMapper.deleteProcessMatrixFormComponentByFormVersionUuid(formVersionVo.getUuid());
		if(CollectionUtils.isNotEmpty(processMatrixFormComponentList)) {
			for(ProcessMatrixFormComponentVo processMatrixFormComponentVo : processMatrixFormComponentList) {
				matrixMapper.insertMatrixFormComponent(processMatrixFormComponentVo);
			}
		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("uuid", formVo.getUuid());
		resultObj.put("currentVersionUuid", formVersionVo.getUuid());
		resultObj.put("currentVersion", formVersionVo.getVersion());
		return resultObj;
	}

}
