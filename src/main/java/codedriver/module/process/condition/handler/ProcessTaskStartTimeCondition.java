package codedriver.module.process.condition.handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessTaskStartTimeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public String getName() {
		return "starttime";
	}

	@Override
	public String getDisplayName() {
		return "上报时间";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.DATE.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

	@Override
	public Integer getSort() {
		return 4;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.DATE;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		return getDateEsWhere(condition,conditionList);
	}

	@Override
	public Object valueConversionText(Object value) {
		if(value != null) {
			if(value instanceof String) {
				return simpleDateFormat.format(new Date(Integer.parseInt(value.toString())));
			}else if(value instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
				List<String> textList = new ArrayList<>();
				for(String valueStr : valueList) {
					textList.add(simpleDateFormat.format(new Date(Integer.parseInt(valueStr))));
				}
				return textList;
			}
		}		
		return value;
	}

}
