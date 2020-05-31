package codedriver.module.process.api.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.exception.form.FormActiveVersionCannotBeDeletedException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class FormVersionDeleteApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/version/delete";
	}

	@Override
	public String getName() {
		return "表单版本删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单版本uuid")
	})
	@Description(desc = "表单版本删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		FormVersionVo formVersion = formMapper.getFormVersionByUuid(uuid);
		//判断被删除的表单版本是否存在
		if(formVersion == null) {
			throw new FormVersionNotFoundException(uuid);
		}else if(formVersion.getIsActive().intValue() == 1){//当前激活版本不能删除
			throw new FormActiveVersionCannotBeDeletedException(uuid);
		}
		//删除表单版本
		formMapper.deleteFormVersionByUuid(uuid);
		formMapper.deleteProcessMatrixFormComponentByFormVersionUuid(uuid);
		return null;
	}

}
