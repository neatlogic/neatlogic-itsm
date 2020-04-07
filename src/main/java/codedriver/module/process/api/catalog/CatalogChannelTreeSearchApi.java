package codedriver.module.process.api.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ITree;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
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
		@Param(name = "channelUuid", type = ApiParamType.STRING, desc = "已选中的服务通道uuid")
		})
	@Output({
		@Param(name="Return",explode=CatalogVo[].class,desc="服务目录及通道树")
	})
	@Description(desc = "服务目录及通道树查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String channelUuid = null;
		if(jsonObj.containsKey("channelUuid")) {
			channelUuid = jsonObj.getString("channelUuid");
			if(channelMapper.checkChannelIsExists(channelUuid) == 0) {
				throw new ChannelNotFoundException(channelUuid);
			}
		}				
		
		Map<String, ITree> uuidKeyMap = new HashMap<>();
		String parentUuid = null;
		ITree parent = null;
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(null);
		if(catalogList != null && catalogList.size() > 0) {
			for(CatalogVo catalogVo : catalogList) {
				uuidKeyMap.put(catalogVo.getUuid(), catalogVo);			
			}
			for(CatalogVo catalogVo : catalogList) {
				parentUuid = catalogVo.getParentUuid();
				parent = uuidKeyMap.get(parentUuid);
				if(parent != null) {
					catalogVo.setParent(parent);
				}				
			}
		}
		
		List<ChannelVo> channelList = channelMapper.getChannelListForTree(null);
		if(channelList != null && channelList.size() > 0) {
			for(ChannelVo channelVo : channelList) {
				parentUuid = channelVo.getParentUuid();
				parent = uuidKeyMap.get(parentUuid);
				if(parent != null) {
					channelVo.setParent(parent);
					if(channelVo.getUuid().equals(channelUuid)) {
						channelVo.setSelected(true);
						channelVo.setOpenCascade(true);
					}
				}
			}
		}
		
		ITree root = uuidKeyMap.get(ITree.ROOT_UUID);		
		List<ITree> resultChildren = root.getChildren();
		return resultChildren;
	}
}
