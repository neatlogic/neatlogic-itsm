package codedriver.module.process.api.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ITree;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class CalalogBreadcrumbSearchApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "process/calalog/breadcrumb/search";
	}

	@Override
	public String getName() {
		return "获取某个服务目录下的所有服务目录路径接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务目录uuid"),
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称", xss = true),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="breadcrumbList", type=ApiParamType.JSONARRAY, desc="服务目录路径列表"),
		@Param(name="breadcrumbList[0].uuid", type=ApiParamType.STRING, desc="服务目录uuid"),
		@Param(name="breadcrumbList[0].path", type=ApiParamType.JSONARRAY, desc="服务目录路径")
	})
	@Description(desc = "获取某个服务目录下的所有服务目录路径接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String catalogUuid = jsonObj.getString("catalogUuid");
		if(catalogMapper.checkCatalogIsExists(catalogUuid) == 0) {
			throw new CatalogNotFoundException(catalogUuid);
		}

		Set<String> channelParentUuidList = null;
		String keyword = jsonObj.getString("keyword");
		if(keyword != null) {
			ChannelVo channelVo = new ChannelVo();
			channelVo.setKeyword(keyword);
			channelVo.setNeedPage(false);
			List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
			if(CollectionUtils.isNotEmpty(channelList)) {
				channelParentUuidList = channelList.stream().map(ChannelVo::getParentUuid).collect(Collectors.toSet());
			}else {
				return null;
			}
		}
		List<Map<String, Object>> calalogBreadcrumbList = new ArrayList<>();
		Map<String, Object> treePathMap = null;
		Map<String, ITree> uuidKeyMap = new HashMap<>();
		String parentUuid = null;
		ITree parent = null;
		//
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(1);
		if(catalogList != null && catalogList.size() > 0) {
			for(CatalogVo catalogVo : catalogList) {
				uuidKeyMap.put(catalogVo.getUuid(), catalogVo);		
			}
			//设置父级
			for(CatalogVo catalogVo : catalogList) {
				parentUuid = catalogVo.getParentUuid();
				parent = uuidKeyMap.get(parentUuid);
				if(parent != null) {
					catalogVo.setParent(parent);
				}				
			}
			//排序
			Collections.sort(catalogList);
			
			List<String> teamUuidList = teamMapper.getTeamUuidListByUserId(UserContext.get().getUserId(true));
			//
			List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserId(true), teamUuidList, UserContext.get().getRoleNameList());
			//已启用的目录uuid列表
			List<String> activatedCatalogUuidList = catalogList.stream().map(CatalogVo::getUuid).collect(Collectors.toList());
			//只留下已启用的目录uuid，去掉已禁用的
			currentUserAuthorizedCatalogUuidList.retainAll(activatedCatalogUuidList);
			//有设置过授权的目录uuid列表
			List<String> authorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList();
			//得到没有设置过授权的目录uuid列表，默认所有人都有权限
			activatedCatalogUuidList.removeAll(authorizedCatalogUuidList);
			currentUserAuthorizedCatalogUuidList.addAll(activatedCatalogUuidList);
					
			List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserId(true), teamUuidList, UserContext.get().getRoleNameList());
			//查出所有已启用的服务
			List<ChannelVo> channelList = channelMapper.getChannelListForTree(1);
			//已启用的服务uuid列表
			List<String> activatedChannelUuidList = channelList.stream().map(ChannelVo::getUuid).collect(Collectors.toList());
			//只留下已启用的服务uuid，去掉已禁用的
			currentUserAuthorizedChannelUuidList.retainAll(activatedChannelUuidList);
			//有设置过授权的服务uuid列表
			List<String> authorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList();
			//得到没有设置过授权的服务uuid列表，默认所有人都有权限
			activatedChannelUuidList.removeAll(authorizedChannelUuidList);
			currentUserAuthorizedChannelUuidList.addAll(activatedChannelUuidList);
			//查出有激活通道的服务目录uuid
			List<String>hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(currentUserAuthorizedCatalogUuidList, currentUserAuthorizedChannelUuidList);
		
			for(CatalogVo catalogVo : catalogList) {
				if(ITree.ROOT_UUID.equals(catalogVo.getUuid())) {
					continue;
				}
				if(!catalogVo.isAncestorOrSelf(catalogUuid)) {
					continue;
				}
				if(keyword != null && !channelParentUuidList.contains(catalogVo.getUuid())) {
					continue;
				}
				if(!currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())
						|| !hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {
					continue;
				}
							
				treePathMap = new HashMap<>();
				treePathMap.put("uuid", catalogVo.getUuid());
				treePathMap.put("path", catalogVo.getNameList());
				treePathMap.put("keyword", keyword);
				calalogBreadcrumbList.add(treePathMap);
			}
		}
		
		JSONObject resultObj = new JSONObject();
		boolean needPage = true;
		if(jsonObj.containsKey("needPage")) {
			needPage = jsonObj.getBooleanValue("needPage");
		}
		if(needPage) {
			int currentPage = 1;
			if(jsonObj.containsKey("currentPage")) {
				currentPage = jsonObj.getIntValue("currentPage");
			}
			int pageSize = 10;
			if(jsonObj.containsKey("pageSize")) {
				pageSize = jsonObj.getIntValue("pageSize");
			}
			int rowNum = calalogBreadcrumbList.size();
			int pageCount = PageUtil.getPageCount(rowNum, pageSize);
			int startNum = Math.max((currentPage - 1) * pageSize, 0);
			int endNum = startNum + pageSize;
			endNum = endNum >  rowNum ? rowNum : endNum;
			resultObj.put("breadcrumbList", calalogBreadcrumbList.subList(startNum, endNum));
			resultObj.put("currentPage", currentPage);
			resultObj.put("pageSize", pageSize);
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}else {
			resultObj.put("breadcrumbList", calalogBreadcrumbList);
		}
		
		return resultObj;
	}

}
