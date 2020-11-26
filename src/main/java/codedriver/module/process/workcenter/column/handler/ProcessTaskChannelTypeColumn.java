package codedriver.module.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeVo;

@Component
public class ProcessTaskChannelTypeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Autowired
	ChannelMapper channelMapper;
	
	@Override
	public String getName() {
		return "channeltype";
	}

	@Override
	public String getDisplayName() {
		return "服务类型";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String channelTypeUuid = json.getString(this.getName());
		JSONObject channelTypeJson = new JSONObject();
		ChannelTypeVo channelType = channelMapper.getChannelTypeByUuid(channelTypeUuid);
		channelTypeJson.put("value", channelTypeUuid);
		if(channelType != null) {
			channelTypeJson.put("text", channelType.getName());
			channelTypeJson.put("color", channelType.getColor());

		}
		return channelTypeJson;
	}

	@Override
	public JSONObject getMyValueText(JSONObject json) {
		return (JSONObject) getMyValue(json);
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
		return 8;
	}

	@Override
	public Object getSimpleValue(JSONObject json) {
		String channelType = json.getJSONObject(this.getName()).getString("text");
		return channelType;
	}
}
