package codedriver.module.process.api.catalog.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelRelationVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import codedriver.module.process.service.CatalogService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CalalogBreadcrumbApi extends PrivateApiComponentBase {

	@Autowired
	private CatalogService catalogService;

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
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
        if(channelTypeRelationId != null && channelMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
            throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
        }
        ChannelRelationVo channelRelationVo = new ChannelRelationVo();
        channelRelationVo.setSource(channelUuid);
        channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
        List<String> channelRelationTargetList = channelMapper.getChannelRelationTargetList(channelRelationVo);
        if(CollectionUtils.isEmpty(channelRelationTargetList)) {
            return resultObj;
        }
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		//已授权的服务uuid
		List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
		if(CollectionUtils.isEmpty(currentUserAuthorizedChannelUuidList)) {
			return resultObj;
		}

		BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
		ChannelVo channel = new ChannelVo();
		channel.setKeyword(basePageVo.getKeyword());
		channel.setIsActive(1);
		channel.setAuthorizedUuidList(currentUserAuthorizedChannelUuidList);
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
			List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);

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
			List<String>hasActiveChannelCatalogUuidList = channelList.stream().map(ChannelVo::getParentUuid).collect(Collectors.toList());
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
					resultObj.put("breadcrumbList", calalogBreadcrumbList.subList(fromIndex, toIndex));
				}
			}else {
				resultObj.put("breadcrumbList", calalogBreadcrumbList);
			}
		}
        return resultObj;
	}

}
