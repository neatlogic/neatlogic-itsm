package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.DataCubeVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessVo;

public interface AttributeService {

	public AttributeVo getAttributeByUuid(String uuid);

	public List<AttributeVo> searchAttribute(AttributeVo attributeVo);

	public DataCubeVo getDataCubeByUuid(String uuid);

}
