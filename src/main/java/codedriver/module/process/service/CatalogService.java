package codedriver.module.process.service;

import codedriver.framework.process.dto.CatalogVo;

public interface CatalogService {

	public boolean checkLeftRightCodeIsExists();

	public Integer rebuildLeftRightCode(String parentUuid, int parentLft);

	public CatalogVo buildRootCatalog();

}
