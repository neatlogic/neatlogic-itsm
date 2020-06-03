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
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessField;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

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
		@Param(name = "formUuid", type = ApiParamType.STRING, desc = "流程绑定表单的uuid")
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
		String conditionModel = ProcessConditionModel.CUSTOM.getValue();
		//固定字段条件
		Map<String, IProcessTaskCondition> workcenterConditionMap = ProcessTaskConditionFactory.getConditionComponentMap();
		for (Map.Entry<String, IProcessTaskCondition> entry : workcenterConditionMap.entrySet()) {
			IProcessTaskCondition condition = entry.getValue();
			if(ProcessField.getValue(condition.getName())== null) {
				continue;
			}
			JSONObject commonObj = new JSONObject();
			commonObj.put("handler", condition.getName());
			commonObj.put("handlerName", condition.getDisplayName());
			commonObj.put("handlerType", condition.getHandler(conditionModel));
			if(condition.getConfig() != null) {
				commonObj.put("isMultiple",condition.getConfig().getBoolean("isMultiple"));
				commonObj.put("config", condition.getConfig().toJSONString());
			}
			commonObj.put("type", condition.getType());
			commonObj.put("basicType", condition.getBasicType().getName());
			commonObj.put("basicTypeName", condition.getBasicType().getText());
			commonObj.put("defaultExpression", condition.getBasicType().getDefaultExpression().getExpression());
			JSONArray expressiobArray = new JSONArray();
			for(Expression expression:condition.getBasicType().getExpressionList()) {
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
				if(formAttributeVo.getHandler().equals(ProcessFormHandler.FORMCASCADELIST.getHandler())
						||formAttributeVo.getHandler().equals(ProcessFormHandler.FORMDIVIDER.getHandler())
						||formAttributeVo.getHandler().equals(ProcessFormHandler.FORMDYNAMICLIST.getHandler())
						||formAttributeVo.getHandler().equals(ProcessFormHandler.FORMSTATICLIST.getHandler())){
					continue;
				}
				formAttributeVo.setType("form");
				formAttributeVo.setConditionModel(conditionModel);
				resultArray.add(JSONObject.toJSON(formAttributeVo));
			}
		}
		return resultArray;
	}

}
