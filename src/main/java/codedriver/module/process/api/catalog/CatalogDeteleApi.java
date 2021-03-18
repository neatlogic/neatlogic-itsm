package codedriver.module.process.api.catalog;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.lrcode.LRCodeManager;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogIllegalParameterException;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.CATALOG_MODIFY;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = CATALOG_MODIFY.class)
public class CatalogDeteleApi extends PrivateApiComponentBase {
	
	@Resource
	private CatalogMapper catalogMapper;

	@Resource
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
		String uuid = jsonObj.getString("uuid");
		CatalogVo existsCatalog = catalogMapper.getCatalogByUuid(uuid);
		if(existsCatalog == null) {
			throw new CatalogNotFoundException(uuid);
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
		LRCodeManager.deleteTreeNode("catalog", "uuid", "parent_uuid", uuid);
		catalogMapper.deleteCatalogByUuid(uuid);
		catalogMapper.deleteCatalogAuthorityByCatalogUuid(uuid);
		return null;
	}

//	private Object backup(JSONObject jsonObj){
//		catalogMapper.getCatalogCountOnLock();
//		if(catalogMapper.checkLeftRightCodeIsWrong() > 0) {
//			catalogService.rebuildLeftRightCode();
//		}
//		String uuid = jsonObj.getString("uuid");
//		CatalogVo existsCatalog = catalogMapper.getCatalogByUuid(uuid);
//		if(existsCatalog == null) {
//			throw new CatalogNotFoundException(uuid);
//		}
//
//		CatalogVo catalogVo = new CatalogVo();
//		catalogVo.setParentUuid(uuid);
//		List<CatalogVo> catalogList = catalogMapper.getCatalogList(catalogVo);
//		if(CollectionUtils.isNotEmpty(catalogList)) {
//			throw new CatalogIllegalParameterException("服务目录：'" + uuid + "'还存在子目录");
//		}
//		ChannelVo channelVo = new ChannelVo();
//		channelVo.setParentUuid(uuid);
//		channelVo.setNeedPage(true);
//		List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
//		if(CollectionUtils.isNotEmpty(channelList)) {
//			throw new CatalogIllegalParameterException("服务目录：'" + uuid + "'还存在子服务");
//		}
//		catalogMapper.deleteCatalogByUuid(uuid);
//		catalogMapper.deleteCatalogAuthorityByCatalogUuid(uuid);
//		//更新删除位置右边的左右编码值
//		catalogMapper.batchUpdateCatalogLeftCode(existsCatalog.getLft(), -2);
//		catalogMapper.batchUpdateCatalogRightCode(existsCatalog.getLft(), -2);
//		return null;
//	}
}
