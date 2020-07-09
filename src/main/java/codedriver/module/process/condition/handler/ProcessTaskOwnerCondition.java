package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessTaskOwnerCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getName() {
		return "owner";
	}

	@Override
	public String getDisplayName() {
		return "上报人";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.USERSELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		JSONObject returnObj = new JSONObject();
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 4;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}

	@Override
	public Object valueConversionText(Object value) {
		if(value != null) {
			if(value instanceof String) {
				UserVo userVo = userMapper.getUserByUuid(value.toString().substring(5));
				if(userVo != null) {
					return userVo.getUserName();
				}
			}else if(value instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
				List<String> textList = new ArrayList<>();
				for(String valueStr : valueList) {
					UserVo userVo = userMapper.getUserByUuid(valueStr.substring(5));
					if(userVo != null) {
						textList.add(userVo.getUserName());					
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
