package codedriver.framework.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.dao.cache.WorkcenterColumnDataCache;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.dto.ChannelVo;

@Component
public class ProcessTaskChannelColumn implements IWorkcenterColumn{

	@Autowired
	ChannelMapper channelMapper;
	@Override
	public String getName() {
		return "channel";
	}

	@Override
	public String getDisplayName() {
		return "服务";
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String channelUuid = el.getString(this.getName());
		String channelName = (String) WorkcenterColumnDataCache.getItem(channelUuid);
		if(channelName == null) {
			ChannelVo channelVo =channelMapper.getChannelByUuid(channelUuid);
			if(channelVo != null) {
				channelName = channelVo.getName();
				WorkcenterColumnDataCache.addItem(channelUuid, channelName);
			}
		}
		return channelName;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
