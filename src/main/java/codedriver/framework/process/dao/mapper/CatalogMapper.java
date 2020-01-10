package codedriver.framework.process.dao.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.CatalogVo;

public interface CatalogMapper {

	List<CatalogVo> getCatalogList(CatalogVo catalogVo);

	CatalogVo getCatalogByUuid(String uuid);
	
	int getMaxSortByParentUuid(String parentUuid);
	
	List<String> getCatalogRoleNameListByCatalogUuid(String catalogUuid);
	
	int checkCatalogIsExists(String catalogUuid);

	Set<String> getHasActiveChannelCatalogUuidList();

	int checkCatalogNameIsRepeat(CatalogVo catalogVo);

	List<CatalogVo> getCatalogListForTree(Integer isActive);
	
	int replaceCatalog(CatalogVo catalogVo);

	int insertCatalogRole(@Param("catalogUuid")String catalogUuid, @Param("roleName")String roleName);
	
	int updateAllNextCatalogSortForMove(@Param("sort")Integer sort, @Param("parentUuid")String parentUuid);

	int updateCatalogForMove(CatalogVo catalogVo);
	
	int deleteCatalogByUuid(String uuid);

	int deleteCatalogRoleByUuid(String uuid);

}
