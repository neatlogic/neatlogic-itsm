package codedriver.module.process.api.form;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.constvalue.ProcessConditionModel;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormHandlerGetApi extends PrivateApiComponentBase {

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
		for (ProcessFormHandlerType s : ProcessFormHandlerType.values()) {
			JSONObject formHandlerObj = new JSONObject();
			formHandlerObj.put("handler", s.getHandler());
			formHandlerObj.put("handlerName", s.getHandlerName());
			formHandlerObj.put("handlerType", s.getHandlerType(ProcessConditionModel.SIMPLE.getValue()).toString());
			List<Expression> expressionList = s.getExpressionList();
			JSONArray expressiobArray = new JSONArray();
			for(Expression expression:expressionList) {
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
