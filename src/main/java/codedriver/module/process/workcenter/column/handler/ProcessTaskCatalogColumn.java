package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;

@Component
public class ProcessTaskCatalogColumn extends ProcessTaskColumnBase  implements IProcessTaskColumn{
	@Autowired
	CatalogMapper catalogMapper;
	@Override
	public String getName() {
		return "catalog";
	}

	@Override
	public String getDisplayName() {
		return "服务目录";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String catalogUuid = json.getString(this.getName());
		String catalogName = StringUtils.EMPTY;
		CatalogVo catalogVo =catalogMapper.getCatalogByUuid(catalogUuid);
		if(catalogVo != null) {
			catalogName = catalogVo.getName();
		}
		return catalogName;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 7;
	}

	@Override
	public Object getSimpleValue(JSONObject json) {
		String catalogName = json.getString(this.getName());
		return catalogName;
	}
}
