package codedriver.module.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterCondition;
import codedriver.framework.process.dao.cache.WorkcenterColumnDataCache;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskCatalogColumn implements IWorkcenterColumn{
	@Autowired
	CatalogMapper catalogMapper;
	@Override
	public String getName() {
		return ProcessWorkcenterCondition.CATALOG.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.CATALOG.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String catalogUuid = el.getString(this.getName());
		String catalogName = (String) WorkcenterColumnDataCache.getItem(catalogUuid);
		if(catalogName == null) {
			CatalogVo catalogVo =catalogMapper.getCatalogByUuid(catalogUuid);
			if(catalogVo != null) {
				catalogName = catalogVo.getName();
				WorkcenterColumnDataCache.addItem(catalogUuid, catalogName);
			}
		}
		return catalogName;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
