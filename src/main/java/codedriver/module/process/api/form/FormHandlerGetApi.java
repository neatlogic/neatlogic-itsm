package codedriver.module.process.api.form;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class FormHandlerGetApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/form/handler/get";
	}

	@Override
	public String getName() {
		return "表单组件获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
		
	})
	@Output({ 
		@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
		@Param(name = "handlerName", type = ApiParamType.STRING, desc = "处理器名"),
		@Param(name = "handlerType", type = ApiParamType.STRING, desc = "控件类型 select|input|radio|userselect|date|area|time"),
		@Param(name = "expressionList[0].expression", type = ApiParamType.STRING, desc = "表达式"),
		@Param(name = "expressionList[0].expressionName", type = ApiParamType.STRING, desc = "表达式名")
	})
	@Description(desc = "表单组件获取接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray resultArray = new JSONArray();
		for (ProcessFormHandler s : ProcessFormHandler.values()) {
			JSONObject formHandlerObj = new JSONObject();
			formHandlerObj.put("handler", s.getHandler());
			formHandlerObj.put("handlerName", s.getHandlerName());
			formHandlerObj.put("handlerType", s.getType(ProcessConditionModel.SIMPLE.getValue()).toString());
			List<ProcessExpression> expressionList = s.getExpressionList();
			JSONArray expressiobArray = new JSONArray();
			for(ProcessExpression expression:expressionList) {
				JSONObject expressionObj = new JSONObject();
				expressionObj.put("expression", expression.getExpression());
				expressionObj.put("expressionName", expression.getExpressionName());
				expressiobArray.add(expressionObj);
				formHandlerObj.put("expressionList", expressiobArray);
			}
			resultArray.add(formHandlerObj);
		}
		return resultArray;
	}

}
