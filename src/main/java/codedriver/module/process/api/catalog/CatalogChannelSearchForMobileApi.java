package codedriver.module.process.api.catalog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.AuthorityVo;
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
		List<CatalogVo> firstCatalogList = new ArrayList<CatalogVo>();
		String catalogUuid = jsonObj.getString("catalogUuid");
		ChannelVo paramChannel = new ChannelVo();
		//查出所有授权的服务目录
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		CatalogVo param = new CatalogVo();
		param.setUserUuid(UserContext.get().getUserUuid());
		param.setRoleUuidList(UserContext.get().getRoleUuidList());
		param.setTeamUuidList(teamUuidList);
		List<CatalogVo> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogList(param);
		//如果服务目录uuid == 0，则展示所有一级服务目录
		if(catalogUuid == CatalogVo.ROOT_UUID) {
		
		}else {
			//paramChannel.setCatalogUuid(catalogUuid);
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
