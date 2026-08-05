package neatlogic.module.process.api.catalog;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.CATALOG_MODIFY;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.service.CatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = CATALOG_MODIFY.class)
public class RebuidLeftRightCodeApi extends PrivateApiComponentBase {

	@Resource
	private CatalogMapper catalogMapper;
	
	@Resource
	private CatalogService catalogService;
	
	@Override
	public String getToken() {
		return "process/catalog/rebuidleftrightcode";
	}

	@Override
	public String getName() {
		return "nmpac.rebuidleftrightcodeapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		catalogMapper.getCatalogCountOnLock();
		catalogService.rebuildLeftRightCode();
		return null;
	}

}
