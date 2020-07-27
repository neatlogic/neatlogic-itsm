package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;

@Component
public class ProcessTaskChannelColumn extends ProcessTaskColumnBase  implements IProcessTaskColumn{

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
		String channelName = StringUtils.EMPTY;
		ChannelVo channelVo =channelMapper.getChannelByUuid(channelUuid);
		if(channelVo != null) {
			channelName = channelVo.getName();
		}
		return channelName;
	}
	@Override
	public JSONObject getMyValueText(JSONObject json) {
		String channelUuid = json.getString(this.getName());
		JSONObject channelJson = new JSONObject();
		ChannelVo channelVo =channelMapper.getChannelByUuid(channelUuid);
		if(channelVo != null) {
			channelJson.put("value", channelUuid);
			channelJson.put("text", channelVo.getName());
			channelJson.put("color", channelVo.getColor());
		}
		return channelJson;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 9;
	}
}
