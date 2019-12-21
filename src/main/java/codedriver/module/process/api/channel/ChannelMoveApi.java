package codedriver.module.process.api.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.exception.CatalogNotFoundException;
import codedriver.framework.process.exception.ChannelIllegalParameterException;
import codedriver.framework.process.exception.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ChannelVo;

@Service
@Transactional
public class ChannelMoveApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Override
	public String getToken() {
		return "process/channel/move";
	}

	@Override
	public String getName() {
		return "服务通道移动位置接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "被移动的服务通道uuid"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, isRequired = true, desc = "移动后的父级uuid"),
		@Param(name = "nextUuid", type = ApiParamType.STRING, desc = "移动后的下一个兄弟的节点uuid")
	})
	@Description(desc = "服务通道移动位置接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(channelMapper.checkChannelIsExists(uuid) == 0) {
			throw new ChannelNotFoundException(uuid);
		}
		String parentUuid = jsonObj.getString("parentUuid");
		if(catalogMapper.checkCatalogIsExists(parentUuid) == 0) {
			throw new CatalogNotFoundException(parentUuid);
		}
		ChannelVo channelVo = new ChannelVo();
		channelVo.setParentUuid(parentUuid);		
		Integer sort;
		if(jsonObj.containsKey("nextUuid")) {
			String nextUuid = jsonObj.getString("nextUuid");
			ChannelVo nextChannel = channelMapper.getChannelByUuid(nextUuid);
			if(nextChannel == null) {
				throw new ChannelNotFoundException(nextUuid);
			}
			if(!parentUuid.equals(nextChannel.getParentUuid())) {
				throw new ChannelIllegalParameterException("服务通道：'" + nextUuid + "'不是服务目录：'" + parentUuid + "'的子通道");
			}
			sort = nextChannel.getSort();
			channelMapper.updateAllNextChannelSortForMove(sort, parentUuid);
		}else {
			sort = channelMapper.getMaxSortByParentUuid(parentUuid) + 1;
		}
		channelVo.setUuid(uuid);
		channelVo.setSort(sort);
		channelMapper.updateChannelForMove(channelVo);
		return null;
	}

}
