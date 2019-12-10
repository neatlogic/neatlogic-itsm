package codedriver.module.process.api.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.FormVersionVo;
import codedriver.module.process.dto.FormVo;
import codedriver.module.process.service.FormService;

@Service
public class FormGetApi extends ApiComponentBase {

	@Autowired
	private FormService formService;

	@Override
	public String getToken() {
		return "form/get";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "单个表单查询接口";
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Input({
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "表单uuid",
					isRequired = true) })
	@Output({ @Param(explode = FormVo.class) })
	@Description(desc = "单个表单查询接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		FormVo formVo = formService.getFormDetailByUuid(uuid);
		if (formVo.getVersionList() != null && formVo.getVersionList().size() > 0) {
			for (FormVersionVo version : formVo.getVersionList()) {
				if (version.getIsActive().equals(1)) {
					formVo.setContent(version.getContent());
				}
			}
			formVo.setVersionList(null);
		}
		return formVo;
	}

}
