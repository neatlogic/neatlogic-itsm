package codedriver.module.process.api.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
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
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogTreeSearchApi extends ApiComponentBase {

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
		//如果catalogUuid为0则放行
		if(!CatalogVo.ROOT_UUID.equals(catalogUuid) && catalogMapper.checkCatalogIsExists(catalogUuid) == 0) {
			throw new CatalogNotFoundException(catalogUuid);
		}

		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		//已授权的目录uuid
		List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
		if(CollectionUtils.isEmpty(currentUserAuthorizedCatalogUuidList)) {
			return new ArrayList<>();
		}
		//已授权的服务uuid
		List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
		//查出有已启用且有授权服务的目录uuid
		List<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(currentUserAuthorizedChannelUuidList);
		
		Map<String, CatalogVo> uuidKeyMap = new HashMap<>();

//		CatalogVo rootCatalog = catalogMapper.getCatalogByUuid(CatalogVo.ROOT_UUID);
		//构建一个虚拟的root目录
		CatalogVo rootCatalogVo = catalogService.buildRootCatalog();
		//查出所有目录
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(rootCatalogVo.getLft(), rootCatalogVo.getRht());
		if(CollectionUtils.isNotEmpty(catalogList)) {
			//将root目录加入到catalogList中
			catalogList.add(rootCatalogVo);
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
			//排序
			Collections.sort(catalogList);
			
			for(int index = catalogList.size() - 1; index >= 0; index--) {
				CatalogVo catalogVo = catalogList.get(index);
				if(CatalogVo.ROOT_UUID.equals(catalogVo.getUuid())) {
					continue;
				}
				if(catalogVo.isAuthority() && (CollectionUtils.isNotEmpty(catalogVo.getChildren()) || hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid()))) {//
					continue;
				}
				CatalogVo parentCatalog = catalogVo.getParent();
				if(parentCatalog != null) {
					parentCatalog.removeChildCatalog(catalogVo);
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
