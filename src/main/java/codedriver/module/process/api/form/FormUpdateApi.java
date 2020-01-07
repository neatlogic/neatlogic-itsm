package codedriver.module.process.api.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.FormVo;

@Service
public class FormUpdateApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "form/update";
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
		@Param(name = "name", type = ApiParamType.STRING, xss = true, length = 30, desc = "表单名称"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1",desc = "是否激活")
	})
	@Description(desc = "表单基本信息更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		FormVo formVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<FormVo>() {});
		formMapper.updateForm(formVo);
		return null;
	}

}
