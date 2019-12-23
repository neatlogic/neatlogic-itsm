package codedriver.module.process.api.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ITree;
@Service
@Transactional
public class CalalogBreadcrumbSearchApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catatlogMapper;
	
	@Override
	public String getToken() {
		return "process/calalog/breadcrumb/search";
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
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务目录uuid")
		})
	@Output({
		@Param(name="Return", type=ApiParamType.JSONARRAY, desc="服务目录路径列表"),
		@Param(name="Return[0].uuid", type=ApiParamType.STRING, desc="服务目录uuid"),
		@Param(name="Return[0].path", type=ApiParamType.STRING, desc="服务目录路径")
	})
	@Description(desc = "获取某个服务目录下的所有服务目录路径接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String catalogUuid = jsonObj.getString("catalogUuid");
		CatalogVo catalogVo = new CatalogVo();
		catalogVo.setIsActive(1);
		List<CatalogVo> catalogList = catatlogMapper.getCatalogList(catalogVo);
		List<ITree> treeList = new ArrayList<>(catalogList);
		Map<String, ITree> uuidKeyMap = new HashMap<>();
		Map<String, List<ITree>> parentUuidKeyMap = new HashMap<>();
		List<ITree> children = null;
		String parentUuid = null;
		if(treeList != null && treeList.size() > 0) {
			for(ITree tree : treeList) {
				uuidKeyMap.put(tree.getUuid(), tree);
				parentUuid = tree.getParentUuid();
				children = parentUuidKeyMap.get(parentUuid);
				if(children == null) {
					children = new ArrayList<>();
					parentUuidKeyMap.put(parentUuid, children);
				}
				children.add(tree);				
			}
		}
		ITree parent = null;
		String catatlogPath = "";
		List<Map<String, String>> calalogBreadcrumbList = new ArrayList<>();
		if(!CatalogVo.ROOT_UUID.equals(catalogUuid)) {
			parent = uuidKeyMap.get(catalogUuid);
			catatlogPath = parent.getName();
			Map<String, String> catalogPathMap = new HashMap<>();
			catalogPathMap.put("uuid", catalogUuid);
			catalogPathMap.put("path", catatlogPath);
			calalogBreadcrumbList.add(catalogPathMap);
		}else {
			parent = new CatalogVo(catalogUuid);
		}					
		collectBreadcrumb(parent, parentUuidKeyMap, calalogBreadcrumbList, catatlogPath);
		return calalogBreadcrumbList;
	}
	
	private void collectBreadcrumb(ITree parent, Map<String, List<ITree>> parentUuidKeyMap, List<Map<String, String>> treePathList, String parentTreePath) {
		String uuid = parent.getUuid();
		List<ITree> children = parentUuidKeyMap.get(uuid);
		if(children != null && children.size() > 0) {
			Map<String, String> treePathMap = null;
			StringBuilder treePathBuilder = null;
			String treePath = null;
			for(ITree child : children) {
				treePathMap = new HashMap<>();
				treePathMap.put("uuid", child.getUuid());
				treePathBuilder = new StringBuilder(parentTreePath);
				if(!ITree.ROOT_UUID.equals(uuid)) {
					treePathBuilder.append("->");
				}
				treePathBuilder.append(child.getName());
				treePath = treePathBuilder.toString();
				treePathMap.put("path", treePath);
				treePathList.add(treePathMap);
				collectBreadcrumb(child, parentUuidKeyMap, treePathList, treePath);
			}
		}
	}

}
