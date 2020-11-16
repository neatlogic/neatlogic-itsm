package codedriver.module.process.api.catalog;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
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
	
	@Autowired
	private TeamMapper teamMapper;

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
		@Param(name = "catalogUuid", type = ApiParamType.STRING, desc = "服务目录uuid,0：所有" , isRequired = true)
	})
	@Output({
		@Param(explode=BasePageVo.class),
		@Param(name="treeList",explode=CatalogVo[].class,desc="目录及服务列表")
	})
	@Description(desc = "查看目录及服务列表(移动端)")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		JSONArray listArray = new JSONArray();
		String catalogUuid = jsonObj.getString("catalogUuid");
		ChannelVo paramChannel = new ChannelVo();
		//查出所有授权的服务目录
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		//如果服务目录catalogUuid == 0，则展示所有一级服务目录,否则仅展示对应的catalogUuid
		if(catalogUuid.equals(CatalogVo.ROOT_UUID)) {
			catalogUuid = null;
    		//找到root目录下所有有权限的一级服务目录
    		List<CatalogVo> firstCatalogList = catalogMapper.getAuthorizedCatalogList(
    				UserContext.get().getUserUuid(),
    				teamUuidList,
    				UserContext.get().getRoleUuidList()
    				,CatalogVo.ROOT_UUID,
    				catalogUuid);
    		//获取以上一级服务目录下的所有有权限的服务目录&&服务
    		for(CatalogVo firstCatalog: firstCatalogList) {
    			listArray.add(catalogService.getCatalogChannelByCatalogUuid(firstCatalog));
    		}
		}else {
		    CatalogVo catalog = catalogMapper.getCatalogByUuid(catalogUuid);
		    if(catalog == null) {
	            throw new CatalogNotFoundException(catalogUuid);
	        }
		    listArray = catalogService.getCatalogChannelByCatalogUuid(catalog).getJSONArray("list");
		}
		resultObj.put("list", listArray);
		/** 获取对应目录下的收藏服务 **/
		paramChannel.setIsFavorite(1);
		paramChannel.setUserUuid(UserContext.get().getUserUuid(true));
		//查出当前用户已授权的服务
		paramChannel.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
		paramChannel.setIsActive(1);
		resultObj.put("favoriteList", channelMapper.searchChannelList(paramChannel));
			
		return resultObj;
	}

}
