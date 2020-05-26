package codedriver.module.process.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;

@Service
public class CatalogServiceImpl implements CatalogService {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Override
	public boolean checkLeftRightCodeIsExists() {
		int count = catalogMapper.getCatalogCount(new CatalogVo());
		CatalogVo rootCatalog = catalogMapper.getCatalogByUuid(TeamVo.ROOT_UUID);
		if(rootCatalog == null) {
			throw new TeamNotFoundException(TeamVo.ROOT_UUID);
		}
		if(Objects.equals(rootCatalog.getLft(), 1) && Objects.equals(rootCatalog.getRht(), count * 2)) {
			return true;
		}
		return false;
	}

	@Override
	public Integer rebuildLeftRightCode(String parentUuid, int parentLft) {
		List<CatalogVo> catalogList = catalogMapper.getCatalogByParentUuid(parentUuid);
		for(CatalogVo catalog : catalogList) {
			if(catalog.getChildrenCount() == 0) {
				catalogMapper.updateCatalogLeftRightCode(catalog.getUuid(), parentLft + 1, parentLft + 2);
				parentLft += 2;
			}else {
				int lft = parentLft + 1;
				parentLft = rebuildLeftRightCode(catalog.getUuid(), lft);
				catalogMapper.updateCatalogLeftRightCode(catalog.getUuid(), lft, parentLft + 1);
				parentLft += 1;
			}
		}
		return parentLft;
	}

}
