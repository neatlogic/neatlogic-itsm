package codedriver.module.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.dao.cache.WorkcenterColumnDataCache;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskChannelColumn extends WorkcenterColumnBase  implements IWorkcenterColumn{

	@Autowired
	ChannelMapper channelMapper;
	@Override
	public String getName() {
		return  ProcessWorkcenterColumn.CHANNEL.getValueEs();
	}

	@Override
	public String getDisplayName() {
		return  ProcessWorkcenterColumn.CHANNEL.getName();
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String channelUuid = json.getString(this.getName());
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

	@Override
	public String getType() {
		return ProcessWorkcenterColumnType.COMMON.getValue();
	}
}
