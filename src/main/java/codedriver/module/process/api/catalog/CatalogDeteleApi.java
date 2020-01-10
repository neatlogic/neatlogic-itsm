package codedriver.module.process.api.catalog;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.exception.catalog.CatalogIllegalParameterException;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ChannelVo;

@Service
@Transactional
public class CatalogDeteleApi extends ApiComponentBase {
	
	@Autowired
	private CatalogMapper catalogMapper;
	
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
		String uuid = jsonObj.getString("uuid");
		CatalogVo existsCatalog = catalogMapper.getCatalogByUuid(uuid);
		if(existsCatalog == null) {
			throw new CatalogNotFoundException(uuid);
		}
		
		CatalogVo catalogVo = new CatalogVo();
		catalogVo.setParentUuid(uuid);
		List<CatalogVo> catalogList = catalogMapper.getCatalogList(catalogVo);
		if(catalogList != null && catalogList.size() > 0) {
			throw new CatalogIllegalParameterException("服务目录：'" + uuid + "'还存在子目录");
		}
		ChannelVo channelVo = new ChannelVo();
		channelVo.setParentUuid(uuid);
		channelVo.setNeedPage(true);
		List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
		if(channelList != null && channelList.size() > 0) {
			throw new CatalogIllegalParameterException("服务目录：'" + uuid + "'还存在子通道");
		}
		catalogMapper.deleteCatalogByUuid(uuid);
		catalogMapper.deleteCatalogRoleByUuid(uuid);
		catalogMapper.updateSortDecrement(existsCatalog.getParentUuid(), existsCatalog.getSort(), null);
//		channelMapper.updateSortDecrement(existsCatalog.getSort(), null);
		return null;
	}

}
