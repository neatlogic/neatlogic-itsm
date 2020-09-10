package codedriver.module.process.api.catalog.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelRelationVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogTreeApi extends PrivateApiComponentBase {

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
		return "process/catalog/tree";
	}

	@Override
	public String getName() {
		return "获取所有服务目录（包含层级关系）接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired= true, desc = "已选中的服务目录uuid"),
		@Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "服务类型关系id"),
		@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务uuid")
	})
	@Output({
		@Param(name="Return",explode=CatalogVo[].class,desc="服务目录列表")
	})
	@Description(desc = "获取所有服务目录（包含层级关系）接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String catalogUuid = jsonObj.getString("catalogUuid");
		//如果catalogUuid为0则放行
		if(!CatalogVo.ROOT_UUID.equals(catalogUuid) && catalogMapper.checkCatalogIsExists(catalogUuid) == 0) {
			throw new CatalogNotFoundException(catalogUuid);
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
		if(CollectionUtils.isNotEmpty(channelRelationTargetList)) {
		    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
	        //已授权的目录uuid
	        List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
	        if(CollectionUtils.isNotEmpty(currentUserAuthorizedCatalogUuidList)) {
	          //已授权的服务uuid
	            List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
	            currentUserAuthorizedChannelUuidList.retainAll(channelRelationTargetList);
	            if(CollectionUtils.isNotEmpty(currentUserAuthorizedChannelUuidList)) {
	              //查出有已启用且有授权服务的目录uuid
	                List<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(currentUserAuthorizedChannelUuidList);
	                
	                Map<String, CatalogVo> uuidKeyMap = new HashMap<>();

	                //构建一个虚拟的root目录
	                CatalogVo rootCatalogVo = catalogService.buildRootCatalog();
	                //查出所有目录
	                List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(rootCatalogVo.getLft(), rootCatalogVo.getRht());
	                if(CollectionUtils.isNotEmpty(catalogList)) {
	                    //将root目录加入到catalogList中
	                    catalogList.add(0, rootCatalogVo);
	                    for(CatalogVo catalogVo : catalogList) {
	                        if(currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())) {
	                            catalogVo.setAuthority(true);
	                        }
	                        uuidKeyMap.put(catalogVo.getUuid(), catalogVo);     
	                    }
	                    
//	                    for(CatalogVo catalogVo : catalogList) {
//	                        String parentUuid = catalogVo.getParentUuid();
//	                        CatalogVo parent = uuidKeyMap.get(parentUuid);
//	                        if(parent != null) {
//	                            catalogVo.setParent(parent);
//	                        }               
//	                    }
//	                    //排序
//	                    Collections.sort(catalogList);
//	                    
//	                    for(int index = catalogList.size() - 1; index >= 0; index--) {
//	                        CatalogVo catalogVo = catalogList.get(index);
//	                        if(CatalogVo.ROOT_UUID.equals(catalogVo.getUuid())) {
//	                            continue;
//	                        }
//	                        if(catalogVo.isAuthority() && (CollectionUtils.isNotEmpty(catalogVo.getChildren()) || hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid()))) {//
//	                            continue;
//	                        }
//	                        CatalogVo parentCatalog = catalogVo.getParent();
//	                        if(parentCatalog != null) {
//	                            parentCatalog.removeChildCatalog(catalogVo);
//	                        }
//	                    }
	                    for(int index = catalogList.size() - 1; index >= 0; index--) {
	                        CatalogVo catalogVo = catalogList.get(index);
	                        if(catalogVo.getParent() == null) {
	                            String parentUuid = catalogVo.getParentUuid();
	                            if(!CatalogVo.ROOT_PARENTUUID.equals(parentUuid)) {
	                                if(catalogVo.isAuthority()) {
	                                    if(CollectionUtils.isNotEmpty(catalogVo.getChildren()) || hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {
	                                        CatalogVo parent = uuidKeyMap.get(parentUuid);
	                                        if(parent != null) {
	                                            catalogVo.setParent(parent);
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }

	                List<Object> resultChildren = rootCatalogVo.getChildren();
	                CatalogVo copyRoot = new CatalogVo();
	                copyRoot.setUuid(rootCatalogVo.getUuid());
	                copyRoot.setName(rootCatalogVo.getName());
	                copyRoot.setParentUuid(rootCatalogVo.getParentUuid());
	                copyRoot.setLft(rootCatalogVo.getLft());
	                resultChildren.add(copyRoot);
	                return resultChildren;
	            }
	        }
		}
		return new ArrayList<>();
	}

}
