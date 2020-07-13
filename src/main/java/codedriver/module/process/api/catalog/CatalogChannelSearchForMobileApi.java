package codedriver.module.process.api.catalog;

import java.util.List;

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
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.CatalogService;

@Service
public class CatalogChannelSearchForMobileApi extends ApiComponentBase {

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
		resultObj.put("list", listArray);
		String catalogUuid = jsonObj.getString("catalogUuid");
		ChannelVo paramChannel = new ChannelVo();
		//查出所有授权的服务目录
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		//如果服务目录catalogUuid == 0，则展示所有一级服务目录,否则仅展示对应的catalogUuid
		if(catalogUuid.equals(CatalogVo.ROOT_UUID)) {
			catalogUuid = null;
		}
		//找到root目录下所有有权限的一级服务目录
		List<CatalogVo> firstCatalogList = catalogMapper.getAuthorizedCatalogList(
				UserContext.get().getUserUuid(),
				teamUuidList,
				UserContext.get().getRoleUuidList()
				,CatalogVo.ROOT_UUID,
				catalogUuid);
		//获取以上一级服务目录下的所有有权限的服务目录&&服务
		for(CatalogVo firstCatalog: firstCatalogList) {
			JSONArray sonListArray = new JSONArray();
			JSONObject catalogParentJson = new JSONObject();
			catalogParentJson.put("uuid", firstCatalog.getUuid());
			catalogParentJson.put("name", firstCatalog.getName());
			catalogParentJson.put("list", sonListArray);
			//catalog
			List<CatalogVo> catalogList = catalogMapper.getAuthorizedCatalogList(
					UserContext.get().getUserUuid(),
					teamUuidList,
					UserContext.get().getRoleUuidList(),
					firstCatalog.getUuid(),
					null);
			for(CatalogVo catalogVo : catalogList) {
				JSONObject catalogJson = (JSONObject) JSONObject.toJSON(catalogVo);
				catalogJson.put("type", "catalog");
				sonListArray.add(catalogJson);
			}
			//channel
			List<ChannelVo> channelList = channelMapper.getAuthorizedChannelListByParentUuid(UserContext.get().getUserUuid(),teamUuidList,UserContext.get().getRoleUuidList(),firstCatalog.getUuid());
			for(ChannelVo channelVo : channelList) {
				JSONObject channelJson = (JSONObject) JSONObject.toJSON(channelVo);
				channelJson.put("type", "channel");
				sonListArray.add(channelJson);
			}
			listArray.add(catalogParentJson);
		}
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
