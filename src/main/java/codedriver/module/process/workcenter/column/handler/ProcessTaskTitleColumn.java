package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskTitleColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

	@Override
	public String getName() {
		return "title";
	}

	@Override
	public String getDisplayName() {
		return "标题";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String title = json.getString(this.getName());
		return title;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

}
