package codedriver.framework.process.dao.mapper;

import java.util.List;

import codedriver.module.process.dto.CatalogVo;

public interface CatalogMapper {

	List<CatalogVo> getCatalogList(CatalogVo catalogVo);

	CatalogVo getCatalogByUuid(String uuid);

}
