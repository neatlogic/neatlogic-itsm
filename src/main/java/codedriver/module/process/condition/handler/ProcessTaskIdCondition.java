package codedriver.module.process.condition.handler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;

@Component
public class ProcessTaskIdCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	@Override
	public String getName() {
		return "id";
	}

	@Override
	public String getDisplayName() {
		return "工单id";
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
		return 1;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.STRING;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		String where = StringUtils.EMPTY;
		if(condition.getValueList() instanceof String) {
			Object value = condition.getValueList();
			where += String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.ID.getValue()),String.format("'%s'",  value));
		}else if(condition.getValueList() instanceof List) {
			List<String> keywordList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
			if(keywordList.size() == 1) {
				Object value = keywordList.get(0);
				where += String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.ID.getValue()),String.format("'%s'",  value));
			}else {
				for(int i=0;i<keywordList.size();i++) {
					if(i!=0) {
						where += " or ";
					}
					where += String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.ID.getValue()),String.format("'%s'",  keywordList.get(i)));
				}
			}
		}
		return where;
	}

	@Override
	public Object valueConversionText(Object value) {
		return value;
	}
}
