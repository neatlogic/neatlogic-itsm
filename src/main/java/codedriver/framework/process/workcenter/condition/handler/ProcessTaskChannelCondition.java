package codedriver.framework.process.workcenter.condition.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.workcenter.dto.WorkcenterConditionVo;

@Component
public class ProcessTaskChannelCondition implements IWorkcenterCondition{
	@Autowired
	private ChannelMapper channelMapper;
	
	@Override
	public String getName() {
		return "channel";
	}

	@Override
	public String getDisplayName() {
		return "服务";
	}

	@Override
	public String getHandler() {
		return WorkcenterConditionVo.Handler.SELECT.toString();
	}
	
	@Override
	public String getType() {
		return WorkcenterConditionVo.Type.COMMON.toString();
	}

	@Override
	public JSONObject getConfig() {
		ChannelVo channel = new ChannelVo();
		channel.setIsActive(1);
 		List<ChannelVo> channelList = channelMapper.searchChannelList(channel);
		JSONArray jsonList = new JSONArray();
		for (ChannelVo channelVo : channelList) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("value", channelVo.getUuid());
			jsonObj.put("text", channelVo.getName());
			jsonList.add(jsonObj);
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 9;
	}

	@Override
	public String[] getExpressionList() {
		return new String[] { WorkcenterConditionVo.ProcessExpressionEs.EQUAL.getExpressionName() };
	}

}
