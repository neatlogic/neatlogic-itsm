package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;

@Component
public class ProcessTaskPriorityColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	PriorityMapper priorityMapper;
	@Override
	public String getName() {
		return "priority";
	}

	@Override
	public String getDisplayName() {
		return "优先级";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String priorityUuid = json.getString(this.getName());
		JSONObject priorityJson = new JSONObject();
		if(StringUtils.isNotBlank(priorityUuid)) {
			priorityJson.put("value", priorityUuid);
			PriorityVo priority = priorityMapper.getPriorityByUuid(priorityUuid);
			if(priority != null) {
				priorityJson.put("text", priority.getName());
				priorityJson.put("color", priority.getColor());
			}
		}
		return priorityJson;
	}
	
	@Override
	public JSONObject getMyValueText(JSONObject json) {
		return (JSONObject) getMyValue(json);
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public Object getSimpleValue(Object json) {
		String priority = null;
		if(json != null){
			priority = JSONObject.parseObject(json.toString()).getString("text");
		}
		return priority;
	}
}
