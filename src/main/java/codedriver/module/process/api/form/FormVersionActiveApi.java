package codedriver.module.process.api.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.FormVersionVo;
@Service
@Transactional
public class FormVersionActiveApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/version/active";
	}

	@Override
	public String getName() {
		return "表单版本激活接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
		@Param(name = "versionUuid", type = ApiParamType.STRING, isRequired = true, desc = "表单版本uuid")
	})
	@Description(desc = "表单版本激活接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(formMapper.checkFormIsExists(uuid) == 0) {
			throw new FormNotFoundException(uuid);
		}
		String versionUuid = jsonObj.getString("versionUuid");
		if(formMapper.checkFormVersionIsExists(versionUuid) == 0) {
			throw new FormVersionNotFoundException(versionUuid);
		}
		formMapper.resetFormVersionIsActiveByFormUuid(uuid);
		FormVersionVo formVersionVo = new FormVersionVo();
		formVersionVo.setUuid(versionUuid);
		formVersionVo.setIsActive(1);
		formMapper.updateFormVersion(formVersionVo);
		return null;
	}

}