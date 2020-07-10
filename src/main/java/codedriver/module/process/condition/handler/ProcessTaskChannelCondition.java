package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;

@Component
public class ProcessTaskChannelCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

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
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.SELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
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
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
		if(value != null) {
			if(value instanceof String) {
				ChannelVo channelVo = channelMapper.getChannelByUuid(value.toString());
				if(channelVo != null) {
					return channelVo.getName();
				}
			}else if(value instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
				List<String> textList = new ArrayList<>();
				for(String valueStr : valueList) {
					ChannelVo channelVo = channelMapper.getChannelByUuid(valueStr);
					if(channelVo != null) {
						textList.add(channelVo.getName());					
					}else {
						textList.add(valueStr);
					}
				}
				return String.join("、", textList);
			}
		}		
		return value;
	}

}
