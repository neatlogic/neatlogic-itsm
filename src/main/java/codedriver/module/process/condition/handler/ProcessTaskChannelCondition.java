package codedriver.module.process.condition.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskChannelCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

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
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE);
	}
	
	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.INCLUDE;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		boolean result = false;
		List<String> valueList = conditionVo.getValueList();
		if(!CollectionUtils.isEmpty(valueList)) {
			ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
			result = valueList.contains(processTask.getChannelUuid());
		}			
		if(ProcessExpression.INCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return result;
		}else if(ProcessExpression.EXCLUDE.getExpression().equals(conditionVo.getExpression())) {
			return !result;
		}else {
			return false;
		}
	}

}
