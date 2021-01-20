package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.module.process.workcenter.core.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskTitleCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	
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
		JSONObject config = new JSONObject();
		config.put("type", "text");
		config.put("value", "");
		config.put("defaultValue", "");
		config.put("maxlength", 50);
//		config.put("name", "");
//		config.put("label", "");
		return config;
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
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		String where = "(";
		if(condition.getValueList() instanceof String) {
			Object value = condition.getValueList();
			where += String.format(Expression.getExpressionEs(condition.getExpression()),this.getEsName(),String.format("'%s'",  value));
		}else if(condition.getValueList() instanceof List) {
			List<String> keywordList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
			if(keywordList.size() == 1) {
				Object value = keywordList.get(0);
				where += String.format(Expression.getExpressionEs(condition.getExpression()),this.getEsName(),String.format("'%s'",  value));
			}else {
				for(int i=0;i<keywordList.size();i++) {
					if(i!=0) {
						where += " or ";
					}
					where += String.format(Expression.getExpressionEs(condition.getExpression()),this.getEsName(),String.format("'%s'",  keywordList.get(i)));
				}
			}
		}
		return where+")";
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
		return value;
	}

	@Override
	public  List<ISqlTable> getMySqlTableList(){
		return new ArrayList<ISqlTable>(){
			{
				add(new ProcessTaskSqlTable());
			}
		};
	}
}
