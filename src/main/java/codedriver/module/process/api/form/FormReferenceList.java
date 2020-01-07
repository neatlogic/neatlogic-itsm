package codedriver.module.process.api.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessVo;

@Service
public class FormReferenceList extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "form/reference/list";
	}

	@Override
	public String getName() {
		return "表单引用列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "formUuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid")
	})
	@Output({
		@Param(name = "Return", explode = ProcessVo[].class, desc = "流程列表")
	})
	@Description(desc = "表单引用列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String formUuid = jsonObj.getString("formUuid");
		List<ProcessVo> processList = formMapper.getFormReferenceList(formUuid);
		return processList;
	}

}
