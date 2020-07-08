package codedriver.module.process.condition.handler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandler;
@Component
public class ProcessTaskFormAttributeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

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
	public ParamType getParamType() {
		return null;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		if(condition !=null&&StringUtils.isNotBlank(condition.getName())) {
			if(condition.getHandler().equals(ProcessFormHandler.FORMDATE.getHandler())) {
				return getDateEsWhere(condition,conditionList);
			}else {
				String where = "(";
				String formKey = condition.getName();
				String formValueKey = "form.value_"+ProcessFormHandler.getDataType(condition.getHandler()).toLowerCase();
				Object value = StringUtils.EMPTY;
				if(condition.getValueList() instanceof String) {
					value = condition.getValueList();
				}else if(condition.getValueList() instanceof List) {
					List<String> values = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
					value = String.join("','", values);
				}
				if(StringUtils.isNotBlank(value.toString())) {
					value = String.format("'%s'",  value);
				}
				where += String.format(" [ form.key = '%s' and "+Expression.getExpressionEs(condition.getExpression())+" ] ", formKey,formValueKey,value);
				return where+")";
			}
		}
		return null;
	}
	
}
