package codedriver.module.process.api.catalog;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
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
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		//已授权的目录uuid
		List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
		//已授权的服务uuid
		List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
		//查出有已启用且有授权服务的目录uuid
		List<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(currentUserAuthorizedChannelUuidList);
		
		Map<String, CatalogVo> uuidKeyMap = new HashMap<>();		
		if(CollectionUtils.isNotEmpty(catalogList)) {
			for(CatalogVo catalogVo : catalogList) {
				uuidKeyMap.put(catalogVo.getUuid(), catalogVo);		
			}
			
			for(CatalogVo catalogVo : catalogList) {
				String parentUuid = catalogVo.getParentUuid();
				CatalogVo parent = uuidKeyMap.get(parentUuid);
				if(parent != null) {
					catalogVo.setParent(parent);
				}				
			}
			//排序
			Collections.sort(catalogList);
			
			for(int index = catalogList.size() - 1; index >= 0; index--) {
				CatalogVo catalogVo = catalogList.get(index);
//				if(catalogVo.getUuid().equals(catalogUuid)) {
//					catalogVo.setSelectedCascade(true);
//				}
				if(!currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())
						|| (!hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid()) && catalogVo.getChildrenCount() == 0)) {
					CatalogVo parentCatalog = catalogVo.getParent();
					if(parentCatalog != null) {
						parentCatalog.removeChildCatalog(catalogVo);
					}
				}
			}
		}
		
		CatalogVo root = uuidKeyMap.get(CatalogVo.ROOT_UUID);

		List<Object> resultChildren = root.getChildren();
		CatalogVo copyRoot = new CatalogVo();
		copyRoot.setUuid(root.getUuid());
		copyRoot.setName(root.getName());
		copyRoot.setParentUuid(root.getParentUuid());
		copyRoot.setLft(root.getLft());
//		if(Objects.equal(copyRoot.getUuid(), catalogUuid)) {
//			root.setSelected(true);
//		}else {
//			root.setSelected(false);
//		}
		resultChildren.add(copyRoot);
		return resultChildren;
	}

}
