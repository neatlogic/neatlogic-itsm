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
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.exception.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ITree;

@Service
@Transactional
public class CatalogChannelTreeSearchApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Override
	public String getToken() {
		return "process/catalog/channel/tree/search";
	}

	@Override
	public String getName() {
		return "服务目录及通道树查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired= true, desc = "已选中的服务目录uuid")
		})
	@Output({
		@Param(name="Return",explode=CatalogVo[].class,desc="服务目录及通道树")
	})
	@Description(desc = "服务目录及通道树查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String catalogUuid = jsonObj.getString("catalogUuid");
		if(catalogMapper.checkCatalogIsExists(catalogUuid) == 0) {
			throw new CatalogNotFoundException(catalogUuid);
		}
		List<CatalogVo> catalogList = catalogMapper.getCatalogList(null);
		List<ChannelVo> channelList = channelMapper.searchChannelList(null);
		List<ITree> treeList = new ArrayList<>(catalogList.size() + channelList.size());
		treeList.addAll(catalogList);
		treeList.addAll(channelList);
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
					child.setSelected(true);
					child.setOpenCascade(true);
				}
				buildTree(child, parentUuidKeyMap, selectedUuid);				
			}
		}		
	}
}
