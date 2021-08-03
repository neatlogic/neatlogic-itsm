package codedriver.module.process.api.catalog.relation;

import java.util.*;
import java.util.stream.Collectors;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.service.AuthenticationInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import codedriver.module.process.service.CatalogService;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CalalogBreadcrumbApi extends PrivateApiComponentBase {

	@Autowired
	private CatalogService catalogService;

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Autowired
	private UserMapper userMapper;

	@Resource
	private AuthenticationInfoService authenticationInfoService;

	@Override
	public String getToken() {
		return "process/catalog/breadcrumb";
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
        @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "服务类型关系id"),
        @Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务uuid"),
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称", xss = true),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name="breadcrumbList", type=ApiParamType.JSONARRAY, desc="服务目录路径列表")
	})
	@Description(desc = "获取某个服务目录下的所有服务目录路径接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        resultObj.put("breadcrumbList", new ArrayList<>());
		String catalogUuid = jsonObj.getString("catalogUuid");
		CatalogVo catalog = null;
		//如果catalogUuid为0，则构建一个虚拟的root目录
		if(CatalogVo.ROOT_UUID.equals(catalogUuid)){
			catalog = catalogService.buildRootCatalog();
		}else {
			catalog = catalogMapper.getCatalogByUuid(catalogUuid);
			if(catalog == null) {
				throw new CatalogNotFoundException(catalogUuid);
			}
		}
		String channelUuid = jsonObj.getString("channelUuid");
        if(channelMapper.checkChannelIsExists(channelUuid) == 0) {
            throw new ChannelNotFoundException(channelUuid);
        }
        Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
        if(channelTypeRelationId != null && channelTypeMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
            throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
        }
        List<String> channelRelationTargetChannelUuidList = catalogService.getChannelRelationTargetChannelUuidList(channelUuid, channelTypeRelationId);
        if(CollectionUtils.isEmpty(channelRelationTargetChannelUuidList)) {
            return resultObj;
        }
		AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
		//已授权的服务uuid
		List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
		String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
		if(StringUtils.isNotBlank(agentUuid)){
			AuthenticationInfoVo agentAuthenticationInfoVo = authenticationInfoService.getAuthenticationInfo(agentUuid);
			for(String authorizedChannelUuid : channelMapper.getAuthorizedChannelUuidList(agentUuid, agentAuthenticationInfoVo.getTeamUuidList(), agentAuthenticationInfoVo.getRoleUuidList(), null)){
				if(currentUserAuthorizedChannelUuidList.contains(authorizedChannelUuid)){
					continue;
				}
				currentUserAuthorizedChannelUuidList.add(authorizedChannelUuid);
			}
		}
		if(CollectionUtils.isEmpty(currentUserAuthorizedChannelUuidList)) {
			return resultObj;
		}
		List<String> authorizedUuidList = ListUtils.retainAll(currentUserAuthorizedChannelUuidList, channelRelationTargetChannelUuidList);
		if(CollectionUtils.isEmpty(authorizedUuidList)) {
		    return resultObj;
		}
		BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
		ChannelVo channel = new ChannelVo();
		channel.setKeyword(basePageVo.getKeyword());
		channel.setIsActive(1);
		channel.setAuthorizedUuidList(authorizedUuidList);
		channel.setNeedPage(false);
		List<ChannelVo> channelList = channelMapper.searchChannelList(channel);
		if(CollectionUtils.isEmpty(channelList)) {
			return resultObj;
		}

		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(catalog.getLft(), catalog.getRht());
		if(CollectionUtils.isNotEmpty(catalogList)) {
			if(CatalogVo.ROOT_UUID.equals(catalogUuid)){
				catalogList.add(catalog);
			}
			//已授权的目录uuid
			List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
			if(StringUtils.isNotBlank(agentUuid)){
				AuthenticationInfoVo agentAuthenticationInfoVo = authenticationInfoService.getAuthenticationInfo(agentUuid);
				for(String authorizedCatalogUuid : catalogMapper.getAuthorizedCatalogUuidList(agentUuid, agentAuthenticationInfoVo.getTeamUuidList(), agentAuthenticationInfoVo.getRoleUuidList(), null)){
					if(currentUserAuthorizedCatalogUuidList.contains(authorizedCatalogUuid)){
						continue;
					}
					currentUserAuthorizedCatalogUuidList.add(authorizedCatalogUuid);
				}
			}
			Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
			for(CatalogVo catalogVo : catalogList) {
				if(currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())) {
					catalogVo.setAuthority(true);
				}
				uuidKeyMap.put(catalogVo.getUuid(), catalogVo);		
			}
			//设置父级
			for(CatalogVo catalogVo : catalogList) {
				String parentUuid = catalogVo.getParentUuid();
				CatalogVo parent = uuidKeyMap.get(parentUuid);
				if(parent != null) {
					catalogVo.setParent(parent);
				}				
			}

			//排序
			Collections.sort(catalogList);
			//查出有已启用且有授权服务的目录uuid
			List<String> hasActiveChannelCatalogUuidList = channelList.stream().map(ChannelVo::getParentUuid).collect(Collectors.toList());
			List<Map<String, Object>> calalogBreadcrumbList = new ArrayList<>();
			for(CatalogVo catalogVo : catalogList) {
				if(!CatalogVo.ROOT_UUID.equals(catalogVo.getUuid())) {//root根目录不返回
					if(catalogVo.isAuthority() && hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {
						Map<String, Object> treePathMap = new HashMap<>();
						treePathMap.put("uuid", catalogVo.getUuid());
						treePathMap.put("path", catalogVo.getNameList());
						treePathMap.put("keyword", basePageVo.getKeyword());
						calalogBreadcrumbList.add(treePathMap);
					}
				}
			}
			
			if(basePageVo.getNeedPage()) {
				int rowNum = calalogBreadcrumbList.size();
				int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
				resultObj.put("currentPage", basePageVo.getCurrentPage());
				resultObj.put("pageSize", basePageVo.getPageSize());
				resultObj.put("pageCount", pageCount);
				resultObj.put("rowNum", rowNum);
				int fromIndex = basePageVo.getStartNum();
				if(fromIndex < rowNum) {
					int toIndex = fromIndex + basePageVo.getPageSize();
					toIndex = toIndex >  rowNum ? rowNum : toIndex;
					calalogBreadcrumbList = calalogBreadcrumbList.subList(fromIndex, toIndex);
//					resultObj.put("breadcrumbList", calalogBreadcrumbList.subList(fromIndex, toIndex));
				}
			}
			if(CollectionUtils.isNotEmpty(calalogBreadcrumbList)) {
			    ChannelVo channelVo = new ChannelVo();
			    channelVo.setKeyword(basePageVo.getKeyword());
			    channelVo.setIsActive(1);
			    channelVo.setAuthorizedUuidList(authorizedUuidList);
			    channelVo.setPageSize(8);
			    for(Map<String, Object> calalogBreadcrumb : calalogBreadcrumbList) {
			        channelVo.setParentUuid((String)calalogBreadcrumb.get("uuid"));
			        calalogBreadcrumb.put("channelData", getChannelData(channelVo));
			    }
			    resultObj.put("breadcrumbList", calalogBreadcrumbList);
			}
		}
        return resultObj;
	}

	private JSONObject getChannelData(ChannelVo channelVo) {
	    JSONObject resultObj = new JSONObject();
	    int rowNum = channelMapper.searchChannelCount(channelVo);
        int pageCount = PageUtil.getPageCount(rowNum,channelVo.getPageSize());
        resultObj.put("currentPage",channelVo.getCurrentPage());
        resultObj.put("pageSize",channelVo.getPageSize());
        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);       
        resultObj.put("channelList", channelList);
	    return resultObj;
	}
}
