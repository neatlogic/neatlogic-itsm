package codedriver.module.process.api.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ITree;
@Service
public class ChannelBreadcrumbSearchApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Override
	public String getToken() {
		return "process/channel/breadcrumb/search";
	}

	@Override
	public String getName() {
		return "服务通道搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="channelList",explode=ChannelVo[].class,desc="服务通道列表")
	})
	@Description(desc = "服务通道搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		ChannelVo channelVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelVo>() {});
		boolean needPage = channelVo.getNeedPage();
		if(needPage) {
			resultObj.put("currentPage", channelVo.getCurrentPage());
			resultObj.put("pageSize", channelVo.getPageSize());
			resultObj.put("pageCount", 0);
			resultObj.put("rowNum", 0);
		}
//		channelVo.setNeedPage(false);
		
		channelVo.setIsFavorite(0);
		channelVo.setIsActive(1);
//		List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
		Set<String> channelParentUuidList = channelMapper.searchChannelParentUuidList(channelVo);
		if(channelParentUuidList == null || channelParentUuidList.isEmpty()) {
			return resultObj;
		}
		
//		Map<String, List<ITree>> parentUuidKeyMap = new HashMap<>();
//		List<ITree> children = null;
//		String parentUuid = null;
//		
//		for(ITree tree : channelList) {
//			parentUuid = tree.getParentUuid();
//			children = parentUuidKeyMap.get(parentUuid);
//			if(children == null) {
//				children = new ArrayList<>();
//				parentUuidKeyMap.put(parentUuid, children);
//			}
//			children.add(tree);
//		}
//		
//		Set<String> filterUuidSet = parentUuidKeyMap.keySet();
		
//		CatalogVo catalogVo = new CatalogVo();
//		catalogVo.setIsActive(1);
//		List<CatalogVo> catalogList = catalogMapper.getCatalogList(catalogVo);
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
			
			for(CatalogVo catalogVo : catalogList) {
				if(channelParentUuidList.contains(catalogVo.getUuid())) {
					treePathMap = new HashMap<>();
					treePathMap.put("uuid", catalogVo.getUuid());
					treePathMap.put("path", catalogVo.getNameList());
					treePathMap.put("keyword", channelVo.getKeyword());
					calalogBreadcrumbList.add(treePathMap);
				}
			}
		}
		
//		Map<String, ITree> uuidKeyMap = new HashMap<>();
//		Map<String, List<ITree>> catalogParentUuidKeyMap = new HashMap<>();
//		List<ITree> catalogChildren = null;
//		String catalogParentUuid = null;
//		if(catalogList != null && catalogList.size() > 0) {
//			for(ITree tree : catalogList) {
//				uuidKeyMap.put(tree.getUuid(), tree);
//				catalogParentUuid = tree.getParentUuid();
//				catalogChildren = catalogParentUuidKeyMap.get(catalogParentUuid);
//				if(catalogChildren == null) {
//					catalogChildren = new ArrayList<>();
//					catalogParentUuidKeyMap.put(catalogParentUuid, catalogChildren);
//				}
//				catalogChildren.add(tree);				
//			}
//		}
//		parent = new CatalogVo(CatalogVo.ROOT_UUID);
//		String catatlogPath = "";
		
		
//		collectBreadcrumb(parent, catalogParentUuidKeyMap, calalogBreadcrumbList, catatlogPath, filterUuidSet, parentUuidKeyMap);
		
		if(needPage) {
			int currentPage = channelVo.getCurrentPage();
			int pageSize = channelVo.getPageSize();
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
	
//	private void collectBreadcrumb(
//			ITree parent, 
//			Map<String, List<ITree>> catalogParentUuidKeyMap, 
//			List<Map<String, Object>> treePathList, 
//			String parentTreePath, 
//			Set<String> filterUuidSet,
//			Map<String, List<ITree>> parentUuidKeyMap
//			) {
//		String uuid = parent.getUuid();
//		List<ITree> children = catalogParentUuidKeyMap.get(uuid);
//		if(children != null && children.size() > 0) {
//			Map<String, Object> treePathMap = null;
//			StringBuilder treePathBuilder = null;
//			String treePath = null;
//			for(ITree child : children) {				
//				treePathBuilder = new StringBuilder(parentTreePath);
//				if(!ITree.ROOT_UUID.equals(uuid)) {
//					treePathBuilder.append("->");
//				}
//				treePathBuilder.append(child.getName());
//				treePath = treePathBuilder.toString();
//				if(filterUuidSet.contains(child.getUuid())) {
//					treePathMap = new HashMap<>();
//					treePathMap.put("uuid", child.getUuid());
//					treePathMap.put("path", treePath);
//					treePathMap.put("channelList", parentUuidKeyMap.get(child.getUuid()));
//					treePathList.add(treePathMap);
//				}				
//				collectBreadcrumb(child, catalogParentUuidKeyMap, treePathList, treePath, filterUuidSet, parentUuidKeyMap);
//			}
//		}
//	}
}
