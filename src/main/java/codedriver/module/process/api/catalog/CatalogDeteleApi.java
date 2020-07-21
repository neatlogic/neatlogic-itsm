package codedriver.module.process.api.catalog;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogIllegalParameterException;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.CatalogService;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class CatalogDeteleApi extends ApiComponentBase {
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private CatalogService catalogService;
	
	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/catalog/delete";
	}

	@Override
	public String getName() {
		return "服务目录删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired= true, desc = "服务目录uuid")
		})
	@Description(desc = "服务目录删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		catalogMapper.getCatalogCountOnLock();
		if(!catalogService.checkLeftRightCodeIsExists()) {
			catalogService.rebuildLeftRightCode();
		}
		String uuid = jsonObj.getString("uuid");
		CatalogVo existsCatalog = catalogMapper.getCatalogByUuid(uuid);
		if(existsCatalog == null) {
			throw new CatalogNotFoundException(uuid);
		}else if(CatalogVo.UNCATEGORIZED_CATALOG_UUID.equals(uuid)) {
			throw new CatalogIllegalParameterException("未分类目录不能删除");
		}

		CatalogVo catalogVo = new CatalogVo();
		catalogVo.setParentUuid(uuid);
		List<CatalogVo> catalogList = catalogMapper.getCatalogList(catalogVo);
		if(CollectionUtils.isNotEmpty(catalogList)) {
			throw new CatalogIllegalParameterException("服务目录：'" + uuid + "'还存在子目录");
		}
		ChannelVo channelVo = new ChannelVo();
		channelVo.setParentUuid(uuid);
		channelVo.setNeedPage(true);
		List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
		if(CollectionUtils.isNotEmpty(channelList)) {
			throw new CatalogIllegalParameterException("服务目录：'" + uuid + "'还存在子服务");
		}
		catalogMapper.deleteCatalogByUuid(uuid);
		catalogMapper.deleteCatalogAuthorityByCatalogUuid(uuid);
		//更新删除位置右边的左右编码值
		catalogMapper.batchUpdateCatalogLeftCode(existsCatalog.getLft(), -2);
		catalogMapper.batchUpdateCatalogRightCode(existsCatalog.getLft(), -2);
		return null;
	}

}
