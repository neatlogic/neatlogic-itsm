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
public class CatalogTreeSearchApi extends ApiComponentBase {
	
	@Autowired
	private CatalogMapper catatlogMapper;
	
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
		
		ITree root = uuidKeyMap.get(ITree.ROOT_UUID);
		buildTree(root, parentUuidKeyMap, catalogUuid);
		
		List<ITree> resultChildren = root.getChildren();
		root.setChildren(null);
		if(ITree.ROOT_UUID.equals(catalogUuid)) {
			root.setSelected(true);
		}else {
			root.setSelected(false);
		}
		resultChildren.add(root);
		return resultChildren;
	}

	/**
	 * 
	* @Description: 组装成树结构
	* @param @param parent
	* @param @param parentUuidKeyMap parentUuid为key的map
	* @param @param selectedId 选中的uuid
	* @return void
	 */
	public static void buildTree(ITree parent, Map<String, List<ITree>> parentUuidKeyMap, String selectedUuid) {
		List<ITree> children = parentUuidKeyMap.get(parent.getUuid());
		if(children != null && children.size() > 0) {
			parent.setChildren(children);
			for(ITree child : children) {
				child.setParent(parent);
				if(child.getUuid().equals(selectedUuid)) {
					child.setSelectedCascade(true);
				}
				buildTree(child, parentUuidKeyMap, selectedUuid);				
			}
		}		
	}
}
