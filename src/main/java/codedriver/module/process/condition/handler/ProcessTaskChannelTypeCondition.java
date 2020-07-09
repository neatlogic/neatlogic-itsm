package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeVo;

@Component
public class ProcessTaskChannelTypeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

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
	public String getHandler(String processWorkcenterConditionType) {
		if(ProcessConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
			return FormHandlerType.CHECKBOX.toString();
		}else {
			return FormHandlerType.SELECT.toString();
		}
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		List<ChannelTypeVo>  channellist = channelMapper.searchChannelTypeList(new ChannelTypeVo());
		JSONArray jsonList = new JSONArray();
		for(ChannelTypeVo channelType:channellist) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("value", channelType.getUuid());
			jsonObj.put("text", channelType.getName());
			jsonList.add(jsonObj);
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 6;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
		if(value != null) {
			if(value instanceof String) {
				ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(value.toString());
				if(channelTypeVo != null) {
					return channelTypeVo.getName();
				}
			}else if(value instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
				List<String> textList = new ArrayList<>();
				for(String valueStr : valueList) {
					ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(valueStr);
					if(channelTypeVo != null) {
						textList.add(channelTypeVo.getName());					
					}else {
						textList.add(valueStr);
					}
				}
				return textList;
			}			
		}
		return value;
	}
}
