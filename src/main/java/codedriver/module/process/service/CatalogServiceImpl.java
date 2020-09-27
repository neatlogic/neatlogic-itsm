package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelRelationVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;

@Service
public class CatalogServiceImpl implements CatalogService {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private TeamMapper teamMapper;

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
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		/** 查出当前用户所有已授权的目录uuid集合  **/
		List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
		if(CollectionUtils.isNotEmpty(currentUserAuthorizedCatalogUuidList)) {
			/** 查出当前用户所有已授权的服务uuid集合  **/
			List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
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
	public boolean channelIsAuthority(String channelUuid) {
		ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
		if(channel == null ) {
			throw new ChannelNotFoundException(channelUuid);
		}
		/** 服务状态必须是激活**/
		if(Objects.equals(channel.getIsActive(), 1)) {
			List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
			/** 查出当前用户所有已授权的服务uuid集合  **/
			List<String> channelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), channelUuid);
			/** 服务已授权 **/
			if(channelUuidList.contains(channelUuid)) {
				CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channel.getParentUuid());
				if(catalogVo != null && !CatalogVo.ROOT_UUID.equals(catalogVo.getUuid())) {
					/** 查出当前用户所有已授权的目录uuid集合  **/
					List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
					List<CatalogVo> ancestorsAndSelfList = catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
					for(CatalogVo catalog : ancestorsAndSelfList) {
						if(!CatalogVo.ROOT_UUID.equals(catalog.getUuid())) {
							if(Objects.equals(catalog.getIsActive(), 0)) {
								return false;
							}else if(!currentUserAuthorizedCatalogUuidList.contains(catalog.getUuid())) {
								return false;
							}
						}
					}
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public JSONArray getCatalogChannelByCatalogUuid(CatalogVo catalog) {
		JSONArray listArray = new JSONArray();
		JSONArray sonListArray = new JSONArray();
		JSONObject catalogParentJson = new JSONObject();
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		catalogParentJson.put("uuid", catalog.getUuid());
		catalogParentJson.put("name", catalog.getName());
		catalogParentJson.put("list", sonListArray);
		//catalog
		List<CatalogVo> catalogList = catalogMapper.getAuthorizedCatalogList(
				UserContext.get().getUserUuid(),
				teamUuidList,
				UserContext.get().getRoleUuidList(),
				catalog.getUuid(),
				null);
		for(CatalogVo catalogVo : catalogList) {
			JSONObject catalogJson = (JSONObject) JSONObject.toJSON(catalogVo);
			catalogJson.put("type", "catalog");
			sonListArray.add(catalogJson);
		}
		//channel
		List<ChannelVo> channelList = channelMapper.getAuthorizedChannelListByParentUuid(UserContext.get().getUserUuid(),teamUuidList,UserContext.get().getRoleUuidList(),catalog.getUuid());
		for(ChannelVo channelVo : channelList) {
			JSONObject channelJson = (JSONObject) JSONObject.toJSON(channelVo);
			channelJson.put("type", "channel");
			sonListArray.add(channelJson);
		}
		listArray.add(catalogParentJson);
		return listArray;
	}

    @Override
    public CatalogVo buildRootCatalog() {
        Integer maxRhtCode = catalogMapper.getMaxRhtCode();
        CatalogVo rootCatalog = new CatalogVo();
        rootCatalog.setUuid(CatalogVo.ROOT_UUID);
        rootCatalog.setName("root");
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
            return channelMapper.getChannelUuidListByParentUuidListAndChannelTypeUuidList(parentUuidList, channelTypeUuidList);
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
                List<String> channelTypeUuidList = channelMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
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
