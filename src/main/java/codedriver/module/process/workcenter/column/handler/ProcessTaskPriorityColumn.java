package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskPriorityColumn extends WorkcenterColumnBase implements IWorkcenterColumn{
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
		if(StringUtils.isBlank(priorityUuid)) {
			priorityJson.put("id", priorityUuid);
			PriorityVo priority = priorityMapper.getPriorityByUuid(priorityUuid);
			if(priority != null) {
				priorityJson.put("name", priority.getName());
				priorityJson.put("color", priority.getColor());
			}
		}
		return priorityJson;
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
}
