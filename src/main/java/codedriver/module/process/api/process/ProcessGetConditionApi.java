package codedriver.module.process.api.process;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;
import codedriver.framework.process.workcenter.condition.core.WorkcenterConditionFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandler;
import codedriver.module.process.constvalue.ProcessFormHandlerType;
import codedriver.module.process.dto.FormAttributeVo;

@Service
@Transactional
public class ProcessGetConditionApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;

	@Override
	public String getToken() {
		return "process/condition/get";
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
		@Param(name = "formUuid", type = ApiParamType.STRING, isRequired = false, desc = "流程绑定表单的uuid")
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
			}else if(handlerType.equals("radio")) {
				commonObj.put("isMultiple",false);
			}else if(handlerType.equals("checkbox")) {
				commonObj.put("isMultiple",true);
			}
			if(handlerType.equals("select")||handlerType.equals("radio")||handlerType.equals("checkbox")) {
				handlerType = ProcessFormHandlerType.SELECT.toString();
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
		//表单条件
		if(jsonObj.containsKey("formUuid") && !StringUtils.isBlank(jsonObj.getString("formUuid"))) {
			String formUuid = jsonObj.getString("formUuid");
			List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
			for(FormAttributeVo formAttributeVo : formAttrList) {
				JSONObject formObj = new JSONObject();
				String handler = formAttributeVo.getHandler();
				formObj.put("uuid",formAttributeVo.getUuid());
				formObj.put("label",formAttributeVo.getLabel());
				JSONObject configObj =JSONObject.parseObject(formAttributeVo.getConfig());
				formObj.put("config",configObj);
				formObj.put("handler",handler);
				formObj.put("handlerName", ProcessFormHandler.getHandlerName(handler));
				String handlerType = ProcessFormHandler.getType(handler).toString();
				if(handlerType.equals("select")||handlerType.equals("radio")||handlerType.equals("checkbox")) {
					formObj.put("handlerType", ProcessFormHandlerType.SELECT.toString());
				}else {
					formObj.put("handlerType", handlerType);
				}
				if(handlerType.equals("select")) {
					formObj.put("isMultiple",configObj.getString("isMultiple"));
				}else if(handlerType.equals("radio")) {
					formObj.put("isMultiple",false);
				}else if(handlerType.equals("checkbox")) {
					formObj.put("isMultiple",true);
				}
				formObj.put("type", "form");
				JSONArray expressiobArray = new JSONArray();
				for(ProcessExpression expression:ProcessFormHandler.getExpressionList(handler)) {
					JSONObject expressionObj = new JSONObject();
					expressionObj.put("expression", expression.getExpression());
					expressionObj.put("expressionName", expression.getExpressionName());
					expressiobArray.add(expressionObj);
					formObj.put("expressionList", expressiobArray);
				}
				resultArray.add(formObj);
			}
		}
		return resultArray;
	}

}
