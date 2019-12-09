package codedriver.module.process.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.FormVo;

public interface FormService {
	public FormVo getFormDetailByUuid(String formUuid);

	@Transactional
	public int saveForm(FormVo formVo);

	public FormVersionVo getFormVersionByUuid(String formVersionUuid);

	public List<FormVo> searchForm(FormVo formVo);

	public List<AttributeVo> getAttributeByFormUuid(String formUuid);
}
