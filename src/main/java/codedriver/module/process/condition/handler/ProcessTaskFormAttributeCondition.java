package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.condition.ConditionVo;
@Component
public class ProcessTaskFormAttributeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return ProcessFieldType.FORM.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessFieldType.FORM.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return null;
	}

	@Override
	public String getType() {
		return ProcessFieldType.FORM.getValue();
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
	public ParamType getBasicType() {
		return null;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo = new ProcessTaskFormAttributeDataVo();
		processTaskFormAttributeDataVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFormAttributeDataVo.setAttributeUuid(conditionVo.getName());
		ProcessTaskFormAttributeDataVo processTaskFormAttributeData = processTaskMapper.getProcessTaskFormAttributeDataByProcessTaskIdAndAttributeUuid(processTaskFormAttributeDataVo);
		if(processTaskFormAttributeData == null) {
			return false;
		}
		String data = processTaskFormAttributeData.getData();
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
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		if(condition !=null&&StringUtils.isNotBlank(condition.getName())) {
			String where = "(";
			String formKey = condition.getName();
			String formValueKey = "form.value_"+ProcessFormHandler.getDataType(condition.getHandler()).toLowerCase();
			Object value = StringUtils.EMPTY;
			if(CollectionUtils.isNotEmpty(condition.getValueList())) {
				value = condition.getValueList().get(0);
			}
			if(condition.getValueList().size()>1) {
				value = String.join("','",condition.getValueList());
			}
			if(StringUtils.isNotBlank(value.toString())) {
				value = String.format("'%s'",  value);
			}
			where += String.format(" [ form.key = '%s' and "+Expression.getExpressionEs(condition.getExpression())+" ] ", formKey,formValueKey,value);
			return where+")";
		}
		return null;
	}
	
}
