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
import codedriver.framework.common.util.PageUtil;
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
		@Param(name="breadcrumbList[0].path", type=ApiParamType.JSONARRAY, desc="服务目录路径")
	})
	@Description(desc = "获取某个服务目录下的所有服务目录路径接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String catalogUuid = jsonObj.getString("catalogUuid");
		if(catalogMapper.checkCatalogIsExists(catalogUuid) == 0) {
			throw new CatalogNotFoundException(catalogUuid);
		}
		
		List<Map<String, Object>> calalogBreadcrumbList = new ArrayList<>();
		Map<String, Object> treePathMap = null;
		Map<String, ITree> uuidKeyMap = new HashMap<>();
		String parentUuid = null;
		ITree parent = null;
		//
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(1);
		if(catalogList != null && catalogList.size() > 0) {
			for(CatalogVo catalogVo : catalogList) {
				uuidKeyMap.put(catalogVo.getUuid(), catalogVo);		
			}
			//设置父级
			for(CatalogVo catalogVo : catalogList) {
				parentUuid = catalogVo.getParentUuid();
				parent = uuidKeyMap.get(parentUuid);
				if(parent != null) {
					catalogVo.setParent(parent);
				}				
			}
			//排序
			Collections.sort(catalogList);
			//查出有激活通道的服务目录uuid
			Set<String> hasActiveChannelCatalogUuidList = catalogMapper.getHasActiveChannelCatalogUuidList();
			
			for(CatalogVo catalogVo : catalogList) {
				System.out.println(catalogVo);
				if(hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid()) && !ITree.ROOT_UUID.equals(catalogVo.getUuid()) && catalogVo.isAncestorOrSelf(catalogUuid)) {
					treePathMap = new HashMap<>();
					treePathMap.put("uuid", catalogVo.getUuid());
					treePathMap.put("path", catalogVo.getNameList());
					calalogBreadcrumbList.add(treePathMap);
				}
			}
		}
		
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

}
