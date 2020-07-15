package codedriver.module.process.api.process;

import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.condition.core.IConditionHandler;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.dto.ExpressionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
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
		for(IConditionHandler condition : ConditionHandlerFactory.getConditionHandlerList()) {
			if(condition instanceof IProcessTaskCondition && ProcessField.getValue(condition.getName()) != null) {
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
		}
		//表单条件
		String formUuid = jsonObj.getString("formUuid");
		if(StringUtils.isNotBlank(formUuid)) {
			List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
			for(FormAttributeVo formAttributeVo : formAttrList) {
				if( formAttributeVo.getHandler().equals(ProcessFormHandler.FORMDIVIDER.getHandler())
						|| formAttributeVo.getHandler().equals(ProcessFormHandler.FORMDYNAMICLIST.getHandler())
						|| formAttributeVo.getHandler().equals(ProcessFormHandler.FORMSTATICLIST.getHandler())
						|| formAttributeVo.getHandler().equals(ProcessFormHandler.FORMLINK.getHandler())){
					continue;
				}
				formAttributeVo.setType("form");
				formAttributeVo.setConditionModel(conditionModel);
				ConditionParamVo conditionParamVo = new ConditionParamVo();
				conditionParamVo.setName(formAttributeVo.getUuid());
				conditionParamVo.setLabel(formAttributeVo.getLabel());
				conditionParamVo.setController(formAttributeVo.getHandlerType());
				conditionParamVo.setIsMultiple(formAttributeVo.getIsMultiple());
				conditionParamVo.setType(formAttributeVo.getType());
				conditionParamVo.setHandler(formAttributeVo.getHandler());
				conditionParamVo.setConfig(formAttributeVo.getConfig());
				if(ProcessFormHandler.FORMDATE.getHandler().equals(formAttributeVo.getHandler())) {
					JSONObject config = conditionParamVo.getConfig();
					if(MapUtils.isNotEmpty(config)) {
						config.put("type", "datetimerange");
						conditionParamVo.setConfig(config.toJSONString());
					}
				}else if(ProcessFormHandler.FORMTIME.getHandler().equals(formAttributeVo.getHandler())) {
					JSONObject config = conditionParamVo.getConfig();
					if(MapUtils.isNotEmpty(config)) {
						config.put("type", "timerange");
						conditionParamVo.setConfig(config.toJSONString());
					}
				}
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
