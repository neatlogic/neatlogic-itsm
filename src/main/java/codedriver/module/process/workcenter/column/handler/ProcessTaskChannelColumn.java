package codedriver.module.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
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
		return "channel";
	}

	@Override
	public String getDisplayName() {
		return  "服务";
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
		return ProcessFieldType.COMMON.getValue();
	}
}
