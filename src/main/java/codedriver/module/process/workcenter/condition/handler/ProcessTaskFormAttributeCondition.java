package codedriver.module.process.workcenter.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.condition.ConditionVo;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
@Component
public class ProcessTaskFormAttributeCondition implements IWorkcenterCondition {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return ProcessWorkcenterColumnType.FORM.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumnType.FORM.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return null;
	}

	@Override
	public String getType() {
		return ProcessWorkcenterColumnType.FORM.getValue();
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
	public List<ProcessExpression> getExpressionList() {
		return null;
	}

	@Override
	public ProcessExpression getDefaultExpression() {
		return null;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		List<String> valueList = conditionVo.getValueList();
		if(CollectionUtils.isEmpty(valueList)) {
			return false;
		}
		ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo = new ProcessTaskFormAttributeDataVo();
		processTaskFormAttributeDataVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFormAttributeDataVo.setAttributeUuid(conditionVo.getName());
		ProcessTaskFormAttributeDataVo processTaskFormAttributeData = processTaskMapper.getProcessTaskFormAttributeDataByProcessTaskIdAndAttributeUuid(processTaskFormAttributeDataVo);
		if(processTaskFormAttributeData == null) {
			return false;
		}
		String data = processTaskFormAttributeData.getData();
		if(StringUtils.isBlank(data)) {
			return false;
		}
		List<String> dataList = null;
		if(data.startsWith("[") && data.endsWith("]")) {
			dataList = JSON.parseArray(data, String.class);
		}else {
			dataList = new ArrayList<>();
			dataList.add(data);
		}
		ProcessExpression processExpression = ProcessExpression.getProcessExpression(conditionVo.getExpression());
		if(processExpression == null) {
			return false;
		}
		switch(processExpression) {
			case LIKE: 		
				return dataList.get(0).contains(valueList.get(0));
			case EQUAL: 
				return dataList.get(0).equals(valueList.get(0));
			case UNEQUAL: 
				return !dataList.get(0).equals(valueList.get(0));
			case INCLUDE: 
				return valueList.removeAll(dataList);
			case EXCLUDE: 
				return !valueList.removeAll(dataList);
			case BETWEEN: 
				long dataLong = Long.parseLong(dataList.get(0));
				boolean result = false;
				long left = Long.parseLong(valueList.get(0));
				if(dataLong >= left) {
					result = true;
				}
				if(result && valueList.size() == 2) {
					long right = Long.parseLong(valueList.get(1));
					if(dataLong <= right) {
						result = true;
					}else {
						result = false;
					}
				}
				return result;
			case GREATERTHAN: 
				return Long.parseLong(dataList.get(0)) > Long.parseLong(valueList.get(0));
			case LESSTHAN: 
				return Long.parseLong(dataList.get(0)) < Long.parseLong(valueList.get(0));
			default : 
				return false;
		}
	}

}
