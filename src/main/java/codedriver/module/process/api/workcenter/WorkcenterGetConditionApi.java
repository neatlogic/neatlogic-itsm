package codedriver.module.process.api.workcenter;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.framework.process.workcenter.condition.core.WorkcenterConditionFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessExpression;

@Service
@Transactional
public class WorkcenterGetConditionApi extends ApiComponentBase {
	@Override
	public String getToken() {
		return "workcenter/condition/get";
	}

	@Override
	public String getName() {
		return "工单中心获取条件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "组件uuid"),
		@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
		@Param(name = "handlerName", type = ApiParamType.STRING, desc = "处理器名"),
		@Param(name = "handlerType", type = ApiParamType.STRING, desc = "控件类型 select|input|radio|userselect|date|area|time"),
		@Param(name = "expressionList[0].expression", type = ApiParamType.STRING, desc = "表达式"),
		@Param(name = "expressionList[0].expressionName", type = ApiParamType.STRING, desc = "表达式名")
	})
	@Description(desc = "工单中心获取条件接口，目前用于工单中心过滤条件初始化使用")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray resultArray = new JSONArray();
		//固定字段条件
		Map<String, IWorkcenterCondition> workcenterConditionMap = WorkcenterConditionFactory.getConditionComponentMap();
		for (Map.Entry<String, IWorkcenterCondition> entry : workcenterConditionMap.entrySet()) {
			IWorkcenterCondition condition = entry.getValue();
			JSONObject commonObj = new JSONObject();
			commonObj.put("handler", condition.getName());
			commonObj.put("handlerName", condition.getDisplayName());
			String handlerType = condition.getHandler();
			if(handlerType.equals("select")) {
				commonObj.put("isMultiple",condition.getConfig().getBoolean("isMultiple"));
			}
			commonObj.put("handlerType", handlerType);
			commonObj.put("type", condition.getType());
			commonObj.put("config", condition.getConfig());
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
		return resultArray;
	}

}
