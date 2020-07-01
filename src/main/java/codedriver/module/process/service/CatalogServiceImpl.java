package codedriver.module.process.service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;

@Service
public class CatalogServiceImpl implements CatalogService {

	@Autowired
	private CatalogMapper catalogMapper;

	@Override
	public boolean checkLeftRightCodeIsExists() {
		int count = 0;
		count = catalogMapper.getCatalogCount(new CatalogVo());
//		CatalogVo rootCatalog = catalogMapper.getCatalogByUuid(CatalogVo.ROOT_UUID);
//		if(rootCatalog == null) {
//			throw new TeamNotFoundException(CatalogVo.ROOT_UUID);
//		}
		//获取最大的右编码值maxRhtCode
		CatalogVo vo = catalogMapper.getMaxRhtCode();
		int maxRhtCode;
		if(vo != null && vo.getRht() != null){
			maxRhtCode = vo.getRht();
			if(Objects.equals(maxRhtCode, count * 2 + 1) || count == 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Integer rebuildLeftRightCode(String parentUuid, int parentLft) {
		List<CatalogVo> catalogList = catalogMapper.getCatalogListByParentUuid(parentUuid);
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

	@Override
	public CatalogVo buildRootCatalog() {
		int count = catalogMapper.getCatalogCount(new CatalogVo()) + 1;
		CatalogVo rootCatalog = new CatalogVo();
		rootCatalog.setUuid("0");
		rootCatalog.setName("root");
		rootCatalog.setParentUuid("-1");
		rootCatalog.setLft(1);
		rootCatalog.setRht(count == 0 ? 2 : count * 2);
		return rootCatalog;
	}

}
