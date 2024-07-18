package neatlogic.module.process.api.catalog.relation;

import java.util.*;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.CatalogChannelAuthorityAction;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.CatalogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.exception.catalog.CatalogNotFoundException;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogTreeApi extends PrivateApiComponentBase {

	@Resource
	private CatalogService catalogService;
	
	@Resource
	private CatalogMapper catalogMapper;
	
	@Resource
	private ChannelMapper channelMapper;

	@Resource
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/catalog/tree";
	}

	@Override
	public String getName() {
		return "nmpacr.catalogtreeapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired= true, desc = "term.itsm.cataloguuid"),
		@Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "term.itsm.channeltyperelationid"),
		@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "term.itsm.channeluuid")
	})
	@Output({
		@Param(name="Return",explode=CatalogVo[].class,desc="common.tbodylist")
	})
	@Description(desc = "nmpacr.catalogtreeapi.getname")
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
		if(channelTypeRelationId != null && channelTypeMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
		    throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
		}
		List<String> channelRelationTargetChannelUuidList = catalogService.getChannelRelationTargetChannelUuidList(channelUuid, channelTypeRelationId);
		if(CollectionUtils.isNotEmpty(channelRelationTargetChannelUuidList)) {
			AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
	        //已授权的目录uuid
	        List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), CatalogChannelAuthorityAction.REPORT.getValue(), null);
			/** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
//			String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
//			if(StringUtils.isNotBlank(agentUuid)){
//				AuthenticationInfoVo agentAuthenticationInfoVo = authenticationInfoService.getAuthenticationInfo(agentUuid);
//				for(String authorizedCatalogUuid : catalogMapper.getAuthorizedCatalogUuidList(agentUuid, agentAuthenticationInfoVo.getTeamUuidList(), agentAuthenticationInfoVo.getRoleUuidList(), null)){
//					if(currentUserAuthorizedCatalogUuidList.contains(authorizedCatalogUuid)){
//						continue;
//					}
//					currentUserAuthorizedCatalogUuidList.add(authorizedCatalogUuid);
//				}
//			}
	        if(CollectionUtils.isNotEmpty(currentUserAuthorizedCatalogUuidList)) {
	          //已授权的服务uuid
	            List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
				/** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
//	            if(StringUtils.isNotBlank(agentUuid)){
//					AuthenticationInfoVo agentAuthenticationInfoVo = authenticationInfoService.getAuthenticationInfo(agentUuid);
//					for(String authorizedChannelUuid : channelMapper.getAuthorizedChannelUuidList(agentUuid, agentAuthenticationInfoVo.getTeamUuidList(), agentAuthenticationInfoVo.getRoleUuidList(), null)){
//						if(currentUserAuthorizedChannelUuidList.contains(authorizedChannelUuid)){
//							continue;
//						}
//						currentUserAuthorizedChannelUuidList.add(authorizedChannelUuid);
//					}
//				}
	            List<String> authorizedUuidList = ListUtils.retainAll(currentUserAuthorizedChannelUuidList, channelRelationTargetChannelUuidList);
	            if(CollectionUtils.isNotEmpty(authorizedUuidList)) {
	              //查出有已启用且有授权服务的目录uuid
	                List<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(authorizedUuidList);

	                //构建一个虚拟的root目录
	                CatalogVo rootCatalogVo = catalogService.buildRootCatalog();
	                //查出所有目录
	                List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(rootCatalogVo.getLft(), rootCatalogVo.getRht());
	                if(CollectionUtils.isNotEmpty(catalogList)) {	                    
	                    Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
	                    //将root目录加入到catalogList中
	                    catalogList.add(0, rootCatalogVo);
	                    for(CatalogVo catalogVo : catalogList) {
	                        if(currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())) {
	                            catalogVo.setAuthority(true);
	                        }
	                        uuidKeyMap.put(catalogVo.getUuid(), catalogVo);     
	                    }	                    

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
