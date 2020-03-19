package codedriver.module.process.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.module.process.constvalue.ProcessExpression;
import codedriver.module.process.constvalue.ProcessFormHandler;
import codedriver.module.process.constvalue.ProcessWorkcenterConditionModel;

public class FormAttributeVo implements Serializable {
	private static final long serialVersionUID = 8282018124626035430L;
	@EntityField(name = "属性uuid", type = ApiParamType.STRING)
	private String uuid;
	@EntityField(name = "表单uuid", type = ApiParamType.STRING)
	private String formUuid;
	@EntityField(name = "表单版本uuid", type = ApiParamType.STRING)
	private String formVersionUuid;
	@EntityField(name = "属性标签名", type = ApiParamType.STRING)
	private String label;
	@EntityField(name = "类型", type = ApiParamType.STRING)
	private String type;
	@EntityField(name = "处理器", type = ApiParamType.STRING)
	private String handler;
	@EntityField(name = "属性配置", type = ApiParamType.STRING)
	private String config;
	@EntityField(name = "属性数据", type = ApiParamType.STRING)
	private String data;
	@EntityField(name = "是否必填", type = ApiParamType.BOOLEAN)
	private boolean isRequired;
	@EntityField(name = "表达式列表", type = ApiParamType.JSONARRAY)
	List<ProcessExpressionVo> expressionList;
	@EntityField(name = "默认表达式", type = ApiParamType.JSONOBJECT)
	ProcessExpressionVo defaultExpression;
	
	@EntityField(name = "条件模型")
	private String conditionModel;
	
	public FormAttributeVo() {

	}

//	public FormAttributeVo(String _formUuid, String _formVersionUuid, String _uuid, String _config) {
//		this.formUuid = _formUuid;
//		this.formVersionUuid = _formVersionUuid;
//		this.uuid = uuid;
//		this.config = _config;
//	}
	
	public FormAttributeVo(String formUuid) {
		this.formUuid = formUuid;
	}
	
	public FormAttributeVo(String formUuid, String formVersionUuid) {
		this.formUuid = formUuid;
		this.formVersionUuid = formVersionUuid;
	}

	public FormAttributeVo(String formUuid, String formVersionUuid, String uuid, String label, String type, String handler, boolean isRequired, String config, String data) {
		this.uuid = uuid;
		this.formUuid = formUuid;
		this.formVersionUuid = formVersionUuid;
		this.label = label;
		this.type = type;
		this.handler = handler;
		this.isRequired = isRequired;
		this.config = config;
		this.data = data;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFormUuid() {
		return formUuid;
	}

	public void setFormUuid(String formUuid) {
		this.formUuid = formUuid;
	}

	public String getFormVersionUuid() {
		return formVersionUuid;
	}

	public void setFormVersionUuid(String formVersionUuid) {
		this.formVersionUuid = formVersionUuid;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	
	public ProcessExpressionVo getDefaultExpression() {
		if(expressionList != null) {
			return defaultExpression;
		}
		if(handler == null) {
			return null;
		}
		ProcessExpression processExpression = ProcessFormHandler.getExpression(handler);
		return new ProcessExpressionVo(processExpression);
	}

	public void setDefaultExpression(ProcessExpressionVo defaultExpression) {
		this.defaultExpression = defaultExpression;
	}

	public List<ProcessExpressionVo> getExpressionList() {
		if(expressionList != null) {
			return expressionList;
		}
		if(handler == null) {
			return null;
		}
		List<ProcessExpression> processExpressionList = ProcessFormHandler.getExpressionList(handler);
		if(CollectionUtils.isEmpty(processExpressionList)) {
			return null;
		}
		expressionList = new ArrayList<>();
		for(ProcessExpression processExpression : processExpressionList) {
			expressionList.add(new ProcessExpressionVo(processExpression));
		}
		return expressionList;
	}

	public void setExpressionList(List<ProcessExpressionVo> expressionList) {
		this.expressionList = expressionList;
	}

	public String getConditionModel() {
		return conditionModel;
	}

	public void setConditionModel(String conditionModel) {
		this.conditionModel = conditionModel;
	}

	public String getHandlerName() {
		if(handler == null) {
			return null;
		}
		return ProcessFormHandler.getHandlerName(handler);
	}
	
	public String getHandlerType() {
		if(handler == null) {
			return null;
		}
		if(conditionModel == null) {
			return null;
		}
		return ProcessFormHandler.getType(handler, conditionModel).toString();
	}
	
	public Boolean getIsMultiple() {
		if(handler == null) {
			return null;
		}
		if(ProcessFormHandler.FORMSELECT.getHandler().equals(handler)) {
			JSONObject configObj = JSON.parseObject(config);
			return configObj.getBoolean("isMultiple");
		} 

		if(conditionModel.equals(ProcessWorkcenterConditionModel.CUSTOM.getValue())) {
			if(ProcessFormHandler.FORMCHECKBOX.getHandler().equals(handler)){
				return true;
			}else {
				return false;
			}
		}
		return null;
	}
}
