package codedriver.module.process.api.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.FormVersionVo;
import codedriver.module.process.dto.FormVo;

@Service
@Transactional
public class FormCopyApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/copy";
	}

	@Override
	public String getName() {
		return "表单复制接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
		@Param(name = "name", type = ApiParamType.STRING, desc = "表单名称", isRequired = true, xss = true, length = 30),
		@Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "要复制的版本uuid,空表示复制所有版本")
	})
	@Output({
		@Param(name = "Return", explode=FormVo.class, desc = "新表单信息")
	})
	@Description(desc = "表单复制接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		FormVo formVo = formMapper.getFormByUuid(uuid);
		if(formVo == null) {
			throw new FormNotFoundException(uuid);
		}
		String name = jsonObj.getString("name");
		formVo.setUuid(null);
		formVo.setName(name);
		formMapper.insertForm(formVo);
		String currentVersionUuid = null;
		if(jsonObj.containsKey("currentVersionUuid")) {
			currentVersionUuid = jsonObj.getString("currentVersionUuid");
			FormVersionVo formVersion = formMapper.getFormVersionByUuid(currentVersionUuid);
			if(formVersion == null) {
				throw new FormVersionNotFoundException(currentVersionUuid);
			}
			if(!formVersion.getFormUuid().equals(uuid)) {
				throw new FormIllegalParameterException("表单版本：'" + currentVersionUuid + "'不属于表单：'" + uuid + "'的版本");
			}
			FormVersionVo formVersionVo = new FormVersionVo();
			formVersionVo.setFormUuid(formVo.getUuid());
			formVersionVo.setVersion(1);
			formVersionVo.setIsActive(1);
			formVersionVo.setContent(formVersion.getContent());
			formVersionVo.setEditor(UserContext.get().getUserId());
			formMapper.insertFormVersion(formVersionVo);
			formVo.setCurrentVersion(formVersionVo.getVersion());
			formVo.setCurrentVersionUuid(formVersionVo.getUuid());
		}else {
			List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);
			if(formVersionList == null || formVersionList.isEmpty()) {
				throw new FormIllegalParameterException("表单：'" + uuid + "'没有版本");
			}
			for(FormVersionVo formVersionVo : formVersionList) {
				formVersionVo.setUuid(null);
				formVersionVo.setFormUuid(formVo.getUuid());
				formMapper.insertFormVersion(formVersionVo);
				if(formVersionVo.getIsActive().equals(1)) {
					formVo.setCurrentVersion(formVersionVo.getVersion());
					formVo.setCurrentVersionUuid(formVersionVo.getUuid());
				}				
			}
		}
		return formVo;
	}

}
