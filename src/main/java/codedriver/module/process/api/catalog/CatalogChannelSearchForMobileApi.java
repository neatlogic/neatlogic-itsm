package codedriver.module.process.api.catalog;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogChannelSearchForMobileApi extends PrivateApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogService catalogService;
	
	@Override
	public String getToken() {
		return "/catalog/channel/search/mobile";
	}

	@Override
	public String getName() {
		return "查看目录及服务列表(移动端)";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "catalogUuid", type = ApiParamType.STRING, desc = "服务目录uuid,0：所有"),
		@Param(name = "isFavorite", type = ApiParamType.ENUM, rule = "0,1", desc = "是否收藏(1:是;0:否)")
	})
	@Output({
		@Param(explode=BasePageVo.class),
		@Param(name="favoriteList",explode=ChannelVo[].class,desc="收藏的服务列表"),
		@Param(name="list",explode=CatalogVo[].class,desc="目录及服务列表")
	})
	@Description(desc = "查看目录及服务列表(移动端)")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		JSONArray listArray = new JSONArray();
		String catalogUuid = jsonObj.getString("catalogUuid");
		Integer isFavorite = jsonObj.getInteger("isFavorite");
		ChannelVo paramChannel = new ChannelVo();
		/** 如果isFavorite=1，则直接查询所有收藏的服务 **/
		if(isFavorite != null && Objects.equals(isFavorite,1)){
			paramChannel.setUserUuid(UserContext.get().getUserUuid(true));
			//查出当前用户已授权的服务
			paramChannel.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
			paramChannel.setIsActive(1);
			List<ChannelVo> favoriteChannelList = channelMapper.getFavoriteChannelList(paramChannel);
			resultObj.put("favoriteList", favoriteChannelList);
			return resultObj;
		}
		//如果服务目录catalogUuid == 0，则展示所有一级服务目录,否则仅展示对应的catalogUuid
		if(catalogUuid.equals(CatalogVo.ROOT_UUID)) {
			catalogUuid = null;
    		//找到root目录下所有有权限的一级服务目录
    		List<CatalogVo> firstCatalogList = catalogService.getCatalogByCatalogParentUuid(CatalogVo.ROOT_UUID);
    		//获取以上一级服务目录下的所有有权限的服务目录&&服务
    		for(CatalogVo firstCatalog: firstCatalogList) {
    			listArray.add(catalogService.getCatalogChannelByCatalogUuid(firstCatalog,true));
    		}
		}else {
		    CatalogVo catalog = catalogMapper.getCatalogByUuid(catalogUuid);
		    if(catalog == null) {
	            throw new CatalogNotFoundException(catalogUuid);
	        }
		    listArray = catalogService.getCatalogChannelByCatalogUuid(catalog,true).getJSONArray("list");
		}
		resultObj.put("list", listArray);
		return resultObj;
	}

}
