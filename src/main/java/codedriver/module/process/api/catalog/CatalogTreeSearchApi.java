package codedriver.module.process.api.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
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
public class CatalogTreeSearchApi extends ApiComponentBase {
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "process/catalog/tree/search";
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
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired= true, desc = "已选中的服务目录uuid")
		})
	@Output({
		@Param(name="Return",explode=CatalogVo[].class,desc="服务目录列表")
	})
	@Description(desc = "获取所有服务目录（包含层级关系）接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String catalogUuid = jsonObj.getString("catalogUuid");
		if(catalogMapper.checkCatalogIsExists(catalogUuid) == 0) {
			throw new CatalogNotFoundException(catalogUuid);
		}

		//查出所有已启用的目录
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(1);
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
		List<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(currentUserAuthorizedCatalogUuidList, currentUserAuthorizedChannelUuidList);
		
		Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
		String parentUuid = null;
		CatalogVo parent = null;
		
		if(catalogList != null && catalogList.size() > 0) {
			for(CatalogVo catalogVo : catalogList) {
//				if(hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {
//					catalogVo.setChildrenCount(1);
//				}
				uuidKeyMap.put(catalogVo.getUuid(), catalogVo);		
			}
			
			for(CatalogVo catalogVo : catalogList) {
				parentUuid = catalogVo.getParentUuid();
				parent = uuidKeyMap.get(parentUuid);
				if(parent != null) {
					catalogVo.setParent(parent);
				}				
			}
			//排序
			Collections.sort(catalogList);
			
			for(int index = catalogList.size() - 1; index >= 0; index--) {
				CatalogVo catalogVo = catalogList.get(index);
				if(catalogVo.getUuid().equals(catalogUuid)) {
					catalogVo.setSelectedCascade(true);
				}
				if(!currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())
						|| (!hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid()) && catalogVo.getChildrenCount() == 0)) {
					ITree parentCatalog = catalogVo.getParent();
					if(parentCatalog != null) {
						((CatalogVo)parentCatalog).removeChild(catalogVo);
					}
				}
			}
			
		}
		
		ITree root = uuidKeyMap.get(ITree.ROOT_UUID);

		List<ITree> resultChildren = root.getChildren();
		root.setChildren(null);
		if(ITree.ROOT_UUID.equals(catalogUuid)) {
			root.setSelected(true);
		}else {
			root.setSelected(false);
		}
		if(resultChildren == null) {
			resultChildren = new ArrayList<>();
		}
		resultChildren.add(root);
		return resultChildren;
	}

}
