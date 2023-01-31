package neatlogic.module.process.service;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.DeviceType;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.crossover.ICatalogCrossoverService;
import neatlogic.framework.process.dao.mapper.CatalogMapper;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.ChannelTypeMapper;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelRelationVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.service.AuthenticationInfoService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class CatalogServiceImpl implements CatalogService, ICatalogCrossoverService {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Resource
	private AuthenticationInfoService authenticationInfoService;

	@Override
	public void rebuildLeftRightCode() {
		rebuildLeftRightCode(CatalogVo.ROOT_UUID, 1);
		
	}

	private Integer rebuildLeftRightCode(String parentUuid, int parentLft) {
		List<CatalogVo> catalogList= catalogMapper.getCatalogListByParentUuid(parentUuid);
		for(CatalogVo catalog : catalogList) {
			if(catalog.getChildrenCount() == 0) {
				catalogMapper.updateCatalogLeftRightCode(catalog.getUuid(), parentLft + 1, parentLft + 2);
				parentLft += 2;
			}else {
				int lft = parentLft + 1;
				parentLft = rebuildLeftRightCode(catalog.getUuid(), lft);
				catalogMapper.updateCatalogLeftRightCode(catalog.getUuid(), lft, parentLft + 1);
				parentLft += 1;
			}
		}
		return parentLft;
	}

	@Override
	public List<String> getCurrentUserAuthorizedChannelUuidList() {
		List<String> resultList = new ArrayList<>();
		AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
		/** 查出当前用户所有已授权的目录uuid集合  **/
		List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
		if(CollectionUtils.isNotEmpty(currentUserAuthorizedCatalogUuidList)) {
			/** 查出当前用户所有已授权的服务uuid集合  **/
			List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
			if(CollectionUtils.isNotEmpty(currentUserAuthorizedChannelUuidList)) {
				Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
				//构造一个虚拟的root节点
				CatalogVo rootCatalog = buildRootCatalog();
				/** 查出所有目录 **/
				List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(rootCatalog.getLft(), rootCatalog.getRht());				
				for(CatalogVo catalogVo : catalogList) {
					if(currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())) {
						catalogVo.setAuthority(true);
					}
					uuidKeyMap.put(catalogVo.getUuid(), catalogVo);		
				}
				
				for(CatalogVo catalogVo : catalogList) {
					String parentUuid = catalogVo.getParentUuid();
					CatalogVo parent = uuidKeyMap.get(parentUuid);
					if(parent != null) {
						catalogVo.setParent(parent);
					}				
				}
				
				List<ChannelVo> channelList = channelMapper.getChannelListForTree(1);
				for(ChannelVo channelVo : channelList) {
					if(currentUserAuthorizedChannelUuidList.contains(channelVo.getUuid())) {
						channelVo.setAuthority(true);
					}
					String parentUuid = channelVo.getParentUuid();
					CatalogVo parent = uuidKeyMap.get(parentUuid);
					if(parent != null) {
						channelVo.setParent(parent);
					}
				}
				/** 找出有上报权限的服务uuid集合 **/
				for(ChannelVo channelVo : channelList) {
					if(channelVo.isAuthority()) {
						resultList.add(channelVo.getUuid());
					}
				}
			}		
		}
		
		return resultList;
	}

	@Override
	public boolean channelIsAuthority(String channelUuid, String userUuid) {
		AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
		/** 查出当前用户所有已授权的服务uuid集合  **/
		List<String> channelUuidList = channelMapper.getActiveAuthorizedChannelUuidList(userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), channelUuid);
		/** 服务已授权 **/
		if(channelUuidList.contains(channelUuid)) {
			ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
			if(channel == null) {
				return false;
			}
			CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channel.getParentUuid());
			if(catalogVo == null) {
				return false;
			}
			List<String> catalogUuidList = new ArrayList<>();
			List<CatalogVo> ancestorsAndSelfList = catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
			for (CatalogVo catalog : ancestorsAndSelfList) {
				if(Objects.equals(catalog.getIsActive(), 0)) {
					return false;
				}
				catalogUuidList.add(catalog.getUuid());
			}
			if (CollectionUtils.isNotEmpty(catalogUuidList)) {
				/** 查出当前用户所有已授权的目录uuid集合  **/
				List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidListByCatalogUuidList(userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), catalogUuidList);
				catalogUuidList.removeAll(currentUserAuthorizedCatalogUuidList);
				if (CollectionUtils.isEmpty(catalogUuidList)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
    public List<CatalogVo> getCatalogByCatalogParentUuid(String catalogUuid) {
	    List<CatalogVo> cataLogVoList = new ArrayList<>();
		AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
        //已授权的服务uuid
        List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
        if(CollectionUtils.isEmpty(currentUserAuthorizedChannelUuidList)) {
            return cataLogVoList;
        }
        ChannelVo channel = new ChannelVo();
        channel.setIsActive(1);
        channel.setAuthorizedUuidList(currentUserAuthorizedChannelUuidList);
        channel.setNeedPage(false);
        List<ChannelVo> channelList = channelMapper.searchChannelList(channel);
        //查出有已启用且有授权服务的目录uuid
        Set<String>hasActiveChannelCatalogUuidSet = new HashSet<>();
        for(ChannelVo channelVo :channelList) {
            String parentUuids = channelVo.getParentUuids();
            if(StringUtils.isNotBlank(parentUuids)) {
                hasActiveChannelCatalogUuidSet.addAll(Arrays.asList(channelVo.getParentUuids().split(",")));
            }
        }
       
        //catalog
        List<CatalogVo> catalogList = catalogMapper.getAuthorizedCatalogList(
                UserContext.get().getUserUuid(),
				authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(),
                catalogUuid,
                null);
        for(CatalogVo catalogVo : catalogList) {
            if(hasActiveChannelCatalogUuidSet.contains(catalogVo.getUuid())) {
                cataLogVoList.add(catalogVo);
            }
        }
        return cataLogVoList;
	}

	@Override
	public JSONObject getCatalogChannelByCatalogUuid(CatalogVo catalog,Boolean isNeedChannel) {
	    JSONArray sonListArray = new JSONArray();
        JSONObject catalogParentJson = new JSONObject();
		AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
	    //已授权的服务uuid
        List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
        if(CollectionUtils.isEmpty(currentUserAuthorizedChannelUuidList)) {
            return catalogParentJson;
        }
        ChannelVo channel = new ChannelVo();
        channel.setIsActive(1);
        channel.setAuthorizedUuidList(currentUserAuthorizedChannelUuidList);
        channel.setNeedPage(false);
        List<ChannelVo> channelList = channelMapper.searchChannelList(channel);
        //查出有已启用且有授权服务的目录uuid
        Set<String>hasActiveChannelCatalogUuidSet = new HashSet<>();
        for(ChannelVo channelVo :channelList) {
            String parentUuids = channelVo.getParentUuids();
            if(StringUtils.isNotBlank(parentUuids)) {
                hasActiveChannelCatalogUuidSet.addAll(Arrays.asList(channelVo.getParentUuids().split(",")));
            }
        }
        
        //
		catalogParentJson.put("uuid", catalog.getUuid());
		catalogParentJson.put("name", catalog.getName());
		catalogParentJson.put("list", sonListArray);
		//catalog
		List<CatalogVo> catalogList = catalogMapper.getAuthorizedCatalogList(
				UserContext.get().getUserUuid(),
				authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(),
				catalog.getUuid(),
				null);
		for(CatalogVo catalogVo : catalogList) {
			JSONObject catalogJson = (JSONObject) JSONObject.toJSON(catalogVo);
			catalogJson.put("type", "catalog");
			if(hasActiveChannelCatalogUuidSet.contains(catalogJson.getString("uuid"))) {
			    sonListArray.add(catalogJson);
			}
		}
		//channel
		if(isNeedChannel ==null || isNeedChannel) {
    		channelList = channelMapper.getAuthorizedChannelListByParentUuid(UserContext.get().getUserUuid(),authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(),catalog.getUuid());
    		for(ChannelVo channelVo : channelList) {
    			if (Objects.equals(channelVo.getSupport(), DeviceType.PC.getValue())) {
    				continue;
				}
    			JSONObject channelJson = (JSONObject) JSONObject.toJSON(channelVo);
    			channelJson.put("type", "channel");
    			sonListArray.add(channelJson);
    		}
		}
		return catalogParentJson;
	}

    @Override
    public CatalogVo buildRootCatalog() {
        Integer maxRhtCode = catalogMapper.getMaxRhtCode();
        CatalogVo rootCatalog = new CatalogVo();
        rootCatalog.setUuid(CatalogVo.ROOT_UUID);
        rootCatalog.setName("所有");
        rootCatalog.setParentUuid(CatalogVo.ROOT_PARENTUUID);
        rootCatalog.setLft(1);
        rootCatalog.setRht(maxRhtCode == null ? 2 : maxRhtCode.intValue() + 1);
        return rootCatalog;
    }

    private List<String> getChannelUuidListInTheCatalogUuidList(List<String> catalogUuidList, List<String> channelTypeUuidList) {
        if(CollectionUtils.isNotEmpty(catalogUuidList)) {
            List<String> parentUuidList = new ArrayList<>();
            for(String catalogUuid : catalogUuidList) {
                if(!parentUuidList.contains(catalogUuid)) {
                    CatalogVo catalogVo = catalogMapper.getCatalogByUuid(catalogUuid);
                    if(catalogVo != null) {
                        List<String> uuidList = catalogMapper.getCatalogUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
                        for(String uuid : uuidList) {
                            if(!parentUuidList.contains(uuid)) {
                                parentUuidList.add(uuid);
                            }
                        }
                    }
                }
            }
            return channelTypeMapper.getChannelUuidListByParentUuidListAndChannelTypeUuidList(parentUuidList, channelTypeUuidList);
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getChannelRelationTargetChannelUuidList(String channelUuid, Long channelTypeRelationId) {
        ChannelRelationVo channelRelationVo = new ChannelRelationVo();
        channelRelationVo.setSource(channelUuid);
        channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
        List<ChannelRelationVo> channelRelationTargetList = channelMapper.getChannelRelationTargetList(channelRelationVo);
        if(CollectionUtils.isNotEmpty(channelRelationTargetList)) {
            List<String> targetChannelUuidList = new ArrayList<>();
            List<String> targetCatalogUuidList = new ArrayList<>();
            for(ChannelRelationVo channelRelation : channelRelationTargetList) {
                if("channel".equals(channelRelation.getType())) {
                    targetChannelUuidList.add(channelRelation.getTarget());
                }else if("catalog".equals(channelRelation.getType())) {
                    targetCatalogUuidList.add(channelRelation.getTarget());
                }
            }
            if(CollectionUtils.isNotEmpty(targetCatalogUuidList)) {
                List<String> channelTypeUuidList = channelTypeMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
                if(channelTypeUuidList.contains("all")) {
                    channelTypeUuidList.clear();
                }
                List<String> channelUuidList = getChannelUuidListInTheCatalogUuidList(targetCatalogUuidList, channelTypeUuidList);
                if(CollectionUtils.isNotEmpty(channelUuidList)) {
                    for(String targetChannelUuid : channelUuidList) {
                        if(!targetChannelUuidList.contains(targetChannelUuid)) {
                            targetChannelUuidList.add(targetChannelUuid);
                        }
                    }
                }
            }
            return targetChannelUuidList;
        }
        return new ArrayList<>();
    }
}
