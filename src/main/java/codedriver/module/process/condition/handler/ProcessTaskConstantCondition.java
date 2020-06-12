package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.condition.ConditionVo;
@Component
public class ProcessTaskConstantCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

	@Override
	public String getName() {
		return ProcessFieldType.CONSTANT.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessFieldType.CONSTANT.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return null;
	}

	@Override
	public String getType() {
		return ProcessFieldType.CONSTANT.getValue();
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

	@Override
	public Integer getSort() {
		return null;
	}

	@Override
	public ParamType getParamType() {
		return null;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		String data = conditionVo.getName();
		List<String> dataList = null;
		if(data.startsWith("[") && data.endsWith("]")) {
			dataList = JSON.parseArray(data, String.class);
		}else {
			dataList = new ArrayList<>();
			if(StringUtils.isNotBlank(data)) {
				dataList.add(data);
			}
		}
		Expression processExpression = Expression.getProcessExpression(conditionVo.getExpression());
		if(processExpression == null) {
			return false;
		}

		List<String> valueList = conditionVo.getValueList();
		switch(processExpression) {
			case LIKE: 
				if(CollectionUtils.isEmpty(valueList) || CollectionUtils.isEmpty(dataList)) {
					return false;
				}
				return dataList.get(0).contains(valueList.get(0));
			case EQUAL: 
				if(CollectionUtils.isEmpty(valueList) || CollectionUtils.isEmpty(dataList)) {
					return false;
				}
				return dataList.get(0).equals(valueList.get(0));
			case UNEQUAL: 
				if(CollectionUtils.isEmpty(valueList) || CollectionUtils.isEmpty(dataList)) {
					return false;
				}
				return !dataList.get(0).equals(valueList.get(0));
			case INCLUDE: 
				return valueList.removeAll(dataList);
			case EXCLUDE: 
				return !valueList.removeAll(dataList);
			case BETWEEN: 
				if(CollectionUtils.isEmpty(valueList) || CollectionUtils.isEmpty(dataList)) {
					return false;
				}
				String dataStr = dataList.get(0);
				boolean result = false;
				String left = valueList.get(0);
				if(dataStr.length() > left.length()) {
					result = true;
				}else if(dataStr.length() < left.length()) {
					result = false;
				}else {
					result = dataStr.compareTo(left) >= 0 ? true : false;
				}
				if(result && valueList.size() == 2) {
					String right = valueList.get(1);
					if(dataStr.length() > right.length()) {
						result = false;
					}else if(dataStr.length() < right.length()) {
						result = true;
					}else {
						result = dataStr.compareTo(right) <= 0 ? true : false;
					}
				}
				return result;
			case GREATERTHAN: 
				if(CollectionUtils.isEmpty(valueList) || CollectionUtils.isEmpty(dataList)) {
					return false;
				}
				if(dataList.get(0).length() > valueList.get(0).length()) {
					return true;
				}else if(dataList.get(0).length() < valueList.get(0).length()) {
					return false;
				}else {
					return dataList.get(0).compareTo(valueList.get(0)) > 0 ? true : false;
				}
			case LESSTHAN: 
				if(CollectionUtils.isEmpty(valueList) || CollectionUtils.isEmpty(dataList)) {
					return false;
				}
				if(dataList.get(0).length() > valueList.get(0).length()) {
					return false;
				}else if(dataList.get(0).length() < valueList.get(0).length()) {
					return true;
				}else {
					return dataList.get(0).compareTo(valueList.get(0)) < 0 ? true : false;
				}
			default : 
				return false;
		}
	}

}
