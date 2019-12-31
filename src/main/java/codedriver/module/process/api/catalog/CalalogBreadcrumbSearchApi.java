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
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.exception.CatalogNotFoundException;
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
	private CatalogMapper catalogMapper;
	
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
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务目录uuid"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="breadcrumbList", type=ApiParamType.JSONARRAY, desc="服务目录路径列表"),
		@Param(name="breadcrumbList[0].uuid", type=ApiParamType.STRING, desc="服务目录uuid"),
		@Param(name="breadcrumbList[0].path", type=ApiParamType.STRING, desc="服务目录路径")
	})
	@Description(desc = "获取某个服务目录下的所有服务目录路径接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String catalogUuid = jsonObj.getString("catalogUuid");
		if(catalogMapper.checkCatalogIsExists(catalogUuid) == 0) {
			throw new CatalogNotFoundException(catalogUuid);
		}
		CatalogVo catalogVo = new CatalogVo();
		catalogVo.setIsActive(1);
		List<CatalogVo> catalogList = catalogMapper.getCatalogList(catalogVo);
		List<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList();
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
		collectBreadcrumb(parent, parentUuidKeyMap, calalogBreadcrumbList, catatlogPath, hasActiveChannelCatalogUuidList);
		
		JSONObject resultObj = new JSONObject();
		boolean needPage = true;
		if(jsonObj.containsKey("needPage")) {
			needPage = jsonObj.getBooleanValue("needPage");
		}
		if(needPage) {
			int currentPage = 1;
			if(jsonObj.containsKey("currentPage")) {
				currentPage = jsonObj.getIntValue("currentPage");
			}
			int pageSize = 10;
			if(jsonObj.containsKey("pageSize")) {
				pageSize = jsonObj.getIntValue("pageSize");
			}
			int rowNum = calalogBreadcrumbList.size();
			int pageCount = PageUtil.getPageCount(rowNum, pageSize);
			int startNum = Math.max((currentPage - 1) * pageSize, 0);
			int endNum = startNum + pageSize;
			endNum = endNum >  rowNum ? rowNum : endNum;
			resultObj.put("breadcrumbList", calalogBreadcrumbList.subList(startNum, endNum));
			resultObj.put("currentPage", currentPage);
			resultObj.put("pageSize", pageSize);
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}else {
			resultObj.put("breadcrumbList", calalogBreadcrumbList);
		}
		
		return resultObj;
	}
	
	private void collectBreadcrumb(
			ITree parent, 
			Map<String, List<ITree>> parentUuidKeyMap, 
			List<Map<String, String>> treePathList, 
			String parentTreePath, 
			List<String> hasActiveChannelCatalogUuidList
			) {
		String uuid = parent.getUuid();
		List<ITree> children = parentUuidKeyMap.get(uuid);
		if(children != null && children.size() > 0) {
			Map<String, String> treePathMap = null;
			StringBuilder treePathBuilder = null;
			String treePath = null;
			for(ITree child : children) {				
				treePathBuilder = new StringBuilder(parentTreePath);
				if(!ITree.ROOT_UUID.equals(uuid)) {
					treePathBuilder.append("->");
				}
				treePathBuilder.append(child.getName());
				treePath = treePathBuilder.toString();
				if(hasActiveChannelCatalogUuidList.contains(child.getUuid())) {
					treePathMap = new HashMap<>();
					treePathMap.put("uuid", child.getUuid());
					treePathMap.put("path", treePath);
					treePathList.add(treePathMap);
				}				
				collectBreadcrumb(child, parentUuidKeyMap, treePathList, treePath, hasActiveChannelCatalogUuidList);
			}
		}
	}

}
