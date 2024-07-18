package neatlogic.module.process.api.catalog;

import java.util.*;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.CatalogChannelAuthorityAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.CatalogService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.exception.catalog.CatalogNotFoundException;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogTreeSearchApi extends PrivateApiComponentBase {

	@Autowired
	private CatalogService catalogService;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/catalog/tree/search";
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
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired= true, desc = "term.itsm.cataloguuid")
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

		AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
		//已授权的目录uuid
		List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), CatalogChannelAuthorityAction.REPORT.getValue(), null);
		if(CollectionUtils.isEmpty(currentUserAuthorizedCatalogUuidList)) {
			return new ArrayList<>();
		}
		//已授权的服务uuid
		List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
		if(CollectionUtils.isEmpty(currentUserAuthorizedChannelUuidList)) {
		    return new ArrayList<>();
		}
		//查出有已启用且有授权服务的目录uuid
		List<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList(currentUserAuthorizedChannelUuidList);
		if(CollectionUtils.isEmpty(hasActiveChannelCatalogUuidList)) {
		    return new ArrayList<>();
		}
		Map<String, CatalogVo> uuidKeyMap = new HashMap<>();

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
				if(catalogVo.isAuthority()) {
				    if(CollectionUtils.isNotEmpty(catalogVo.getChildren())) {
				        continue;
				    }else if(hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {
				        continue;
				    }
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
