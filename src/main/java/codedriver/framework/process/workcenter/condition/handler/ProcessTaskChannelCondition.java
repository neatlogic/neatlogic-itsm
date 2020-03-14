package codedriver.framework.process.workcenter.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionType;

@Component
public class ProcessTaskChannelCondition implements IWorkcenterCondition{

	@Override
	public String getName() {
		return "channel";
	}

	@Override
	public String getDisplayName() {
		return "服务";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return ProcessFormHandlerType.SELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessWorkcenterConditionType.COMMON.toString();
	}

	@Override
	public JSONObject getConfig() {
		/*ChannelVo channel = new ChannelVo();
		channel.setIsActive(1);
		channel.setNeedPage(false);
 		List<ChannelVo> channelList = channelMapper.searchChannelList(channel);
		JSONArray jsonList = new JSONArray();
		for (ChannelVo channelVo : channelList) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("value", channelVo.getUuid());
			jsonObj.put("text", channelVo.getName());
			jsonList.add(jsonObj);
		}*/
		JSONObject returnObj = new JSONObject();
		returnObj.put("url", "api/rest/process/channel/search");
		returnObj.put("isMultiple", true);
		returnObj.put("rootName", "channelList");
		JSONObject mappingObj = new JSONObject();
		mappingObj.put("value", "uuid");
		mappingObj.put("text", "name");
		returnObj.put("mapping", mappingObj);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 9;
	}

	@Override
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE);
	}

}
