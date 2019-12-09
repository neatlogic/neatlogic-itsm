package codedriver.module.process.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.attribute.dao.mapper.AttributeMapper;
import codedriver.framework.attribute.dto.AttributeVo;
import codedriver.framework.attribute.dto.DataCubeVo;
import codedriver.framework.common.util.PageUtil;

@Service
public class AttributeServiceImpl implements AttributeService {

	@Autowired
	private AttributeMapper attributeMapper;

	@Override
	public AttributeVo getAttributeByUuid(String uuid) {
		return attributeMapper.getAttributeByUuid(uuid);
	}

	@Override
	public List<AttributeVo> searchAttribute(AttributeVo attributeVo) {
		if (attributeVo.getNeedPage()) {
			int rowNum = attributeMapper.searchAttributeCount(attributeVo);
			attributeVo.setRowNum(rowNum);
			attributeVo.setPageCount(PageUtil.getPageCount(rowNum, attributeVo.getPageSize()));
		}
		return attributeMapper.searchAttribute(attributeVo);
	}

	@Override
	public DataCubeVo getDataCubeByUuid(String uuid) {
		return attributeMapper.getDataCubeByUuid(uuid);
	}
}
