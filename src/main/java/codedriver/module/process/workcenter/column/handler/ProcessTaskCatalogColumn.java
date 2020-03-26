package codedriver.module.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.dao.cache.WorkcenterColumnDataCache;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskCatalogColumn extends WorkcenterColumnBase  implements IWorkcenterColumn{
	@Autowired
	CatalogMapper catalogMapper;
	@Override
	public String getName() {
		return ProcessWorkcenterColumn.CATALOG.getValueEs();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.CATALOG.getName();
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String catalogUuid = json.getString(this.getName());
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

	@Override
	public String getType() {
		return ProcessWorkcenterColumnType.COMMON.getValue();
	}

}
