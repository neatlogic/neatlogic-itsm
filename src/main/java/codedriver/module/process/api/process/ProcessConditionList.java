package codedriver.module.process.api.process;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.dto.ExpressionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessField;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessConditionList extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;

	@Override
	public String getToken() {
		return "process/condition/list";
	}

	@Override
	public String getName() {
		return "流程条件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "formUuid", type = ApiParamType.STRING, desc = "流程绑定表单的uuid")
	})
	@Output({
		@Param(explode=ConditionParamVo[].class, desc = "流程条件列表")
	})
	@Description(desc = "流程条件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray resultArray = new JSONArray();
		String conditionModel = ProcessConditionModel.CUSTOM.getValue();
		//固定字段条件
		Map<String, IProcessTaskCondition> workcenterConditionMap = ProcessTaskConditionFactory.getConditionComponentMap();
		for (Map.Entry<String, IProcessTaskCondition> entry : workcenterConditionMap.entrySet()) {
			IProcessTaskCondition condition = entry.getValue();
			if(ProcessField.getValue(condition.getName())== null) {
				continue;
			}
//			JSONObject commonObj = new JSONObject();
//			commonObj.put("handler", condition.getName());
//			commonObj.put("handlerName", condition.getDisplayName());
//			commonObj.put("handlerType", condition.getHandler(conditionModel));
//			if(condition.getConfig() != null) {
//				commonObj.put("isMultiple",condition.getConfig().getBoolean("isMultiple"));
//				commonObj.put("config", condition.getConfig().toJSONString());
//			}
//			commonObj.put("type", condition.getType());
//			commonObj.put("basicType", condition.getBasicType().getName());
//			commonObj.put("basicTypeName", condition.getBasicType().getText());
//			commonObj.put("defaultExpression", condition.getBasicType().getDefaultExpression().getExpression());
//			JSONArray expressiobArray = new JSONArray();
//			for(Expression expression:condition.getBasicType().getExpressionList()) {
//				JSONObject expressionObj = new JSONObject();
//				expressionObj.put("expression", expression.getExpression());
//				expressionObj.put("expressionName", expression.getExpressionName());
//				expressiobArray.add(expressionObj);
//				commonObj.put("expressionList", expressiobArray);
//			}
//			resultArray.add(commonObj);
			
			ConditionParamVo conditionParamVo = new ConditionParamVo();
			conditionParamVo.setName(condition.getName());
			conditionParamVo.setLabel(condition.getDisplayName());
			conditionParamVo.setController(condition.getHandler(conditionModel));
			if(condition.getConfig() != null) {
				conditionParamVo.setIsMultiple(condition.getConfig().getBoolean("isMultiple"));
				conditionParamVo.setConfig(condition.getConfig().toJSONString());
			}
			conditionParamVo.setType(condition.getType());
			ParamType paramType = condition.getParamType();
			if(paramType != null) {
				conditionParamVo.setParamType(paramType.getName());
				conditionParamVo.setParamTypeName(paramType.getText());
				conditionParamVo.setDefaultExpression(paramType.getDefaultExpression().getExpression());
				for(Expression expression:paramType.getExpressionList()) {
					conditionParamVo.getExpressionList().add(new ExpressionVo(expression));
				}
			}
			
			resultArray.add(conditionParamVo);
		}
		//表单条件
		String formUuid = jsonObj.getString("formUuid");
		if(StringUtils.isNotBlank(formUuid)) {
			List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
			for(FormAttributeVo formAttributeVo : formAttrList) {
				if(formAttributeVo.getHandler().equals(ProcessFormHandler.FORMCASCADELIST.getHandler())
						||formAttributeVo.getHandler().equals(ProcessFormHandler.FORMDIVIDER.getHandler())
						||formAttributeVo.getHandler().equals(ProcessFormHandler.FORMDYNAMICLIST.getHandler())
						||formAttributeVo.getHandler().equals(ProcessFormHandler.FORMSTATICLIST.getHandler())){
					continue;
				}
				formAttributeVo.setType("form");
				formAttributeVo.setConditionModel(conditionModel);
				ConditionParamVo conditionParamVo = new ConditionParamVo();
				conditionParamVo.setName(formAttributeVo.getUuid());
				conditionParamVo.setLabel(formAttributeVo.getLabel());
				conditionParamVo.setController(formAttributeVo.getHandlerType());
				conditionParamVo.setIsMultiple(formAttributeVo.getIsMultiple());
				conditionParamVo.setConfig(formAttributeVo.getConfig());
				conditionParamVo.setType(formAttributeVo.getType());

				ParamType paramType = ProcessFormHandler.getParamType(formAttributeVo.getHandler());
				if(paramType != null) {
					conditionParamVo.setParamType(paramType.getName());
					conditionParamVo.setParamTypeName(paramType.getText());
					conditionParamVo.setDefaultExpression(paramType.getDefaultExpression().getExpression());
					for(Expression expression:paramType.getExpressionList()) {
						conditionParamVo.getExpressionList().add(new ExpressionVo(expression));
					}
				}
				
				resultArray.add(conditionParamVo);
			}
		}
		return resultArray;
	}

}
