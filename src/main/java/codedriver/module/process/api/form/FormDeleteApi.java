package codedriver.module.process.api.form;

import java.util.List;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.FORM_MODIFY;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormReferencedCannotBeDeletedException;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = FORM_MODIFY.class)
public class FormDeleteApi extends PrivateApiComponentBase {

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
		List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(uuid);
		if(CollectionUtils.isNotEmpty(formVersionList)) {
			for(FormVersionVo formVersionVo : formVersionList) {
				formMapper.deleteProcessMatrixFormComponentByFormVersionUuid(formVersionVo.getUuid());
			}
		}
		formMapper.deleteFormByUuid(uuid);
		formMapper.deleteFormVersionByFormUuid(uuid);
		formMapper.deleteFormAttributeByFormUuid(uuid);
		return uuid;
	}

}
