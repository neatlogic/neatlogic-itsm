package codedriver.module.process.api.catalog;

import java.util.List;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogGetApi extends PrivateApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Override
	public String getToken() {
		return "process/catalog/get";
	}

	@Override
	public String getName() {
		return "服务目录获取信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired= true, desc = "服务目录uuid")
		})
	@Output({
		@Param(explode=CatalogVo.class,desc="服务目录信息")
	})
	@Description(desc = "服务目录获取信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		CatalogVo catalog = catalogMapper.getCatalogByUuid(uuid);
		if(catalog == null) {
			throw new CatalogNotFoundException(uuid);
		}
//		CatalogVo catalogVo = new CatalogVo();
//		catalogVo.setParentUuid(uuid);
//		List<CatalogVo> catalogList = catalogMapper.getCatalogList(catalogVo);
//		ChannelVo channelVo = new ChannelVo();
//		channelVo.setParentUuid(uuid);
//		int count = channelMapper.searchChannelCount(channelVo);
//		catalog.setChildrenCount(count + catalogList.size());
		List<AuthorityVo> authorityVoList = catalogMapper.getCatalogAuthorityListByCatalogUuid(uuid);
		catalog.setAuthorityVoList(authorityVoList);
		return catalog;
	}

}
