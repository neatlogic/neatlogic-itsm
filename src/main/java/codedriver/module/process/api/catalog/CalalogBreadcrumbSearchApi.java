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

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
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
		if(CollectionUtils.isNotEmpty(catalogList)) {
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
			
			List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
			//已授权的目录uuid
			List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
			//已授权的服务uuid
			List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
			//查出有已启用且有授权服务的目录uuid
			List<String>hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(currentUserAuthorizedChannelUuidList);
		
			for(CatalogVo catalogVo : catalogList) {
				if(!ITree.ROOT_UUID.equals(catalogVo.getUuid())) {//root根目录不返回
					if(catalogVo.isAncestorOrSelf(catalogUuid)) {//只返回catalogUuid的本身及子目录
						if(keyword == null || channelParentUuidList.contains(catalogVo.getUuid())) {//符合搜索条件
							if(currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())//有权限
									&& hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {//有已启用且有授权服务
								treePathMap = new HashMap<>();
								treePathMap.put("uuid", catalogVo.getUuid());
								treePathMap.put("path", catalogVo.getNameList());
								treePathMap.put("keyword", keyword);
								calalogBreadcrumbList.add(treePathMap);
							}
						}
					}
				}
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
