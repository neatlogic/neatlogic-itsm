package codedriver.module.process.condition.handler;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Component
public class ProcessTaskFormAttributeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

	@Override
	public String getName() {
		return ProcessFieldType.FORM.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessFieldType.FORM.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return null;
	}

	@Override
	public String getType() {
		return ProcessFieldType.FORM.getValue();
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

	@Override
	public Integer getSort() {
		return null;
	}

	@Override
	public ParamType getParamType() {
		return null;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		if(condition !=null&&StringUtils.isNotBlank(condition.getName())) {
			if(condition.getHandler().equals(ProcessFormHandler.FORMDATE.getHandler())) {
				return getDateEsWhere(condition,conditionList);
			}else {
				String where = "(";
				String formKey = condition.getName();
				String formValueKey = "form.value_"+ProcessFormHandler.getDataType(condition.getHandler()).toLowerCase();
				Object value = StringUtils.EMPTY;
				if(condition.getValueList() instanceof String) {
					value = condition.getValueList();
				}else if(condition.getValueList() instanceof List) {
					List<String> values = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
					value = String.join("','", values);
				}
				if(StringUtils.isNotBlank(value.toString())) {
					value = String.format("'%s'",  value);
				}
				where += String.format(" [ form.key = '%s' and "+Expression.getExpressionEs(condition.getExpression())+" ] ", formKey,formValueKey,value);
				return where+")";
			}
		}
		return null;
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
		if(value != null) {
			if(MapUtils.isNotEmpty(config)) {
				String attributeUuid = config.getString("attributeUuid");
				String formConfig = config.getString("formConfig");
				FormVersionVo formVersionVo = new FormVersionVo();
				formVersionVo.setFormConfig(formConfig);
				List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
				if(CollectionUtils.isNotEmpty(formAttributeList)) {
					for(FormAttributeVo formAttribute : formAttributeList) {
						if(Objects.equal(attributeUuid, formAttribute.getUuid())) {
							config.put("name", formAttribute.getLabel());
							IFormAttributeHandler formAttributeHandler = FormAttributeHandlerFactory.getHandler(formAttribute.getHandler());
							if(formAttributeHandler != null) {
								AttributeDataVo attributeDataVo = new AttributeDataVo();
								attributeDataVo.setAttributeUuid(attributeUuid);
								if(value instanceof String) {
									attributeDataVo.setData((String)value);
								}else if(value instanceof JSONArray){
									attributeDataVo.setData(JSON.toJSONString(value));
								}						
								return formAttributeHandler.getValue(attributeDataVo, JSON.parseObject(formAttribute.getConfig()));
							}
						}
					}
				}
			}
		}
		return value;
	}
	
}
