package codedriver.module.process.api.catalog;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.auth.CATALOG_MODIFY;
import codedriver.module.process.service.CatalogService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = CATALOG_MODIFY.class)
public class RebuidLeftRightCodeApi extends PrivateApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private CatalogService catalogService;
	
	@Override
	public String getToken() {
		return "process/catalog/rebuidleftrightcode";
	}

	@Override
	public String getName() {
		return "用户组重建左右编码接口";
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
