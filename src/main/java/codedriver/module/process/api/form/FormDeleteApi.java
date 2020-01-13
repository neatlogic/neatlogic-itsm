package codedriver.module.process.api.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormReferencedCannotBeDeletedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class FormDeleteApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/delete";
	}

	@Override
	public String getName() {
		return "表单删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid")
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid")
	})
	@Description(desc = "表单删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		//判断表单是否存在
		if(formMapper.checkFormIsExists(uuid) == 0) {
			throw new FormNotFoundException(uuid);
		}
		if(formMapper.getFormReferenceCount(uuid) > 0) {
			throw new FormReferencedCannotBeDeletedException(uuid);
		}
		formMapper.deleteFormByUuid(uuid);
		formMapper.deleteFormVersionByFormUuid(uuid);
		formMapper.deleteFormAttributeByFormUuid(uuid);
		return uuid;
	}

}
