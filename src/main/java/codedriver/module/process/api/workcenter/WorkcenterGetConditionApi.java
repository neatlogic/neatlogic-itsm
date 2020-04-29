package codedriver.module.process.api.workcenter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class WorkcenterGetConditionApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "workcenter/condition/get";
	}

	@Override
	public String getName() {
		return "流程编辑获取条件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "conditionModel", type = ApiParamType.STRING, desc = "条件模型 simple|custom,  simple:目前用于用于工单中心条件过滤简单模式, custom:目前用于用于工单中心条件过自定义模式;默认custom"),
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "组件uuid"),
		@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
		@Param(name = "handlerName", type = ApiParamType.STRING, desc = "处理器名"),
		@Param(name = "handlerType", type = ApiParamType.STRING, desc = "控件类型 select|input|radio|userselect|date|area|time"),
		@Param(name = "type", type = ApiParamType.STRING, desc = "类型  form|common"),
		@Param(name = "expressionList[0].expression", type = ApiParamType.STRING, desc = "表达式"),
		@Param(name = "expressionList[0].expressionName", type = ApiParamType.STRING, desc = "表达式名")
	})
	@Description(desc = "流程编辑获取条件接口，目前用于流程编辑，初始化条件使用")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray resultArray = new JSONArray();
		String conditionModel = jsonObj.getString("conditionModel") == null?ProcessConditionModel.CUSTOM.getValue():jsonObj.getString("conditionModel");
		//固定字段条件
		Map<String, IProcessTaskCondition> workcenterConditionMap = ProcessTaskConditionFactory.getConditionComponentMap();
		for (Map.Entry<String, IProcessTaskCondition> entry : workcenterConditionMap.entrySet()) {
			IProcessTaskCondition condition = entry.getValue();
			//不支持endTime过滤，如果是简单模式 title、id、content 不返回
			if(conditionModel.equals(ProcessConditionModel.SIMPLE.getValue())&&(condition.getName().equals(ProcessWorkcenterField.TITLE.getValue())
					||condition.getName().equals(ProcessWorkcenterField.ID.getValue())||condition.getName().equals(ProcessWorkcenterField.CONTENT.getValue()))
					||condition.getName().equals(ProcessWorkcenterField.ENDTIME.getValue())
					||ProcessWorkcenterField.getValue(condition.getName())== null
					) {
				continue;
			}
			JSONObject commonObj = new JSONObject();
			commonObj.put("handler", condition.getName());
			commonObj.put("handlerName", condition.getDisplayName());
			commonObj.put("handlerType", condition.getHandler(conditionModel));
			if(condition.getConfig() != null) {
				commonObj.put("isMultiple",condition.getConfig().getBoolean("isMultiple"));
			}
			commonObj.put("conditionModel", condition.getHandler(conditionModel));
			commonObj.put("type", condition.getType());
			commonObj.put("config", condition.getConfig() == null?"": condition.getConfig().toJSONString());
			commonObj.put("defaultExpression", condition.getDefaultExpression().getExpression());
			commonObj.put("sort", condition.getSort());
			JSONArray expressiobArray = new JSONArray();
			for(ProcessExpression expression:condition.getExpressionList()) {
				JSONObject expressionObj = new JSONObject();
				expressionObj.put("expression", expression.getExpression());
				expressionObj.put("expressionName", expression.getExpressionName());
				expressiobArray.add(expressionObj);
				commonObj.put("expressionList", expressiobArray);
			}
			resultArray.add(commonObj);
		}
		Collections.sort(resultArray, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				try {
					JSONObject obj1 = (JSONObject) o1;
					JSONObject obj2 = (JSONObject) o2;
					return obj1.getIntValue("sort") - obj2.getIntValue("sort");
				} catch (Exception ex) {

				}
				return 0;
			}
		});
		return resultArray;
	}

}
