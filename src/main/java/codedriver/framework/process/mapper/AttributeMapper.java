package codedriver.framework.process.mapper;

import java.util.List;

import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.DataCubeVo;

public interface AttributeMapper {
	public DataCubeVo getDataCubeByUuid(String uuid);

	public int searchAttributeCount(AttributeVo attributeVo);

	public List<AttributeVo> searchAttribute(AttributeVo attributeVo);

	public AttributeVo getAttributeByUuid(String uuid);

	public int resetAttributeActive();

	public int replaceAttribute(AttributeVo attributeVo);
}
