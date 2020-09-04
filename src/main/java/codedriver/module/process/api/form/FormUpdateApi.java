package codedriver.module.process.api.form;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.FormVo;
import codedriver.framework.process.exception.form.FormNameRepeatException;
import codedriver.framework.process.exception.form.FormNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class FormUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/update";
	}

	@Override
	public String getName() {
		return "表单基本信息更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "表单名称"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1",desc = "是否激活")
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid")
	})
	@Description(desc = "表单基本信息更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		FormVo formVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<FormVo>() {});
		//判断表单是否存在
		if(formMapper.checkFormIsExists(formVo.getUuid()) == 0) {
			throw new FormNotFoundException(formVo.getUuid());
		}
		//判断名称是否重复
		if(formVo.getName() != null && formMapper.checkFormNameIsRepeat(formVo) > 0) {
			throw new FormNameRepeatException(formVo.getName());
		}
		formMapper.updateForm(formVo);
		return formVo.getUuid();
	}

}
