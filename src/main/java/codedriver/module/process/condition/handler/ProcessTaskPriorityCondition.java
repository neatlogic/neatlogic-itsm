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
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;

@Component
public class ProcessTaskPriorityCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getName() {
		return "priority";
	}

	@Override
	public String getDisplayName() {
		return "优先级";
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
		PriorityVo priorityVo = new PriorityVo();
		priorityVo.setIsActive(1);
		List<PriorityVo> priorityList = priorityMapper.searchPriorityList(priorityVo);
		JSONArray jsonList = new JSONArray();
		for (PriorityVo priority : priorityList) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("value", priority.getUuid());
			jsonObj.put("text", priority.getName());
			jsonList.add(jsonObj);
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("dataList", jsonList);
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 5;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}

	@Override
	public Object valueConversionText(Object value) {
		if(value != null) {
			if(value instanceof String) {
				PriorityVo priorityVo = priorityMapper.getPriorityByUuid(value.toString());
				if(priorityVo != null) {
					return priorityVo.getName();
				}
			}else if(value instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
				List<String> textList = new ArrayList<>();
				for(String valueStr : valueList) {
					PriorityVo priorityVo = priorityMapper.getPriorityByUuid(valueStr);
					if(priorityVo != null) {
						textList.add(priorityVo.getName());					
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
