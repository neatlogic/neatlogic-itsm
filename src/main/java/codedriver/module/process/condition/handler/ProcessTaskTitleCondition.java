package codedriver.module.process.condition.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskTitleCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getName() {
		return "title";
	}

	@Override
	public String getDisplayName() {
		return "标题";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.INPUT.toString();
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
		return 2;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.STRING;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {	
		if(Expression.LIKE.getExpression().equals(conditionVo.getExpression())) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
			List<String> valueList = conditionVo.getValueList();
			return processTaskVo.getTitle().contains(valueList.get(0));
		}else {
			return false;
		}
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		String where = "(";
		if(condition.getValueList().size() == 1) {
			Object value = condition.getValueList().get(0);
			where += String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.TITLE.getValue()),String.format("'%s'",  value));
		}else {
			List<String> keywordList = condition.getValueList();
			for(int i=0;i<keywordList.size();i++) {
				if(i!=0) {
					where += " or ";
				}
				where += String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.TITLE.getValue()),String.format("'%s'",  keywordList.get(i)));
			}
		}
		return where+")";
	}
}
