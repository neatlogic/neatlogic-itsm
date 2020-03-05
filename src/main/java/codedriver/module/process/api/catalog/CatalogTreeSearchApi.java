package codedriver.module.process.api.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ITree;
@Service
@Transactional
public class CatalogTreeSearchApi extends ApiComponentBase {
	
	@Autowired
	private CatalogMapper catalogMapper;
	
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
		//查出有激活通道的服务目录uuid
		Set<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList();
		
		Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
		String parentUuid = null;
		CatalogVo parent = null;
		//
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(1);
		if(catalogList != null && catalogList.size() > 0) {
			for(CatalogVo catalogVo : catalogList) {
				if(hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {
					catalogVo.setChildrenCount(1);
				}
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
				if(catalogVo.getChildrenCount() == 0) {
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
