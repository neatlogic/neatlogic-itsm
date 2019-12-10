package codedriver.module.process.api.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.FormVo;
import codedriver.module.process.service.FormService;

@Service
@Transactional
@AuthAction(name = "FORM_MODIFY")
public class FormSaveApi extends ApiComponentBase {

	@Autowired
	private FormService formService;

	@Override
	public String getToken() {
		return "form/save";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "表单保存接口";
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
					desc = "表单uuid，为空表示创建表单",
					isRequired = false),
			@Param(name = "name",
					type = ApiParamType.STRING,
					desc = "表单名称",
					isRequired = true),
			@Param(name = "isActive",
					type = ApiParamType.INTEGER,
					desc = "是否激活",
					isRequired = true),
			@Param(name = "activeVersionUuid",
					type = ApiParamType.STRING,
					desc = "激活版本的uuid，为空代表创建一个新版本",
					isRequired = false),
			@Param(name = "content",
					type = ApiParamType.STRING,
					desc = "表单控件生成的json内容",
					isRequired = true) })
	@Output({
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "表单uuid") })
	@Description(desc = "表单保存接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		FormVo formVo = JSON.toJavaObject(jsonObj, FormVo.class);
		formService.saveForm(formVo);
		return formVo.getUuid();
	}

}
