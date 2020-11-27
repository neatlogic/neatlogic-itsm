package codedriver.module.process.workcenter.column.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessTaskIdColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "id";
	}

	@Override
	public String getDisplayName() {
		return "工单号";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String id = json.getString(this.getName());
		return id;
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
		return null;
	}

	@Override
	public Integer getSort() {
		return 2;
	}

	@Override
	public Object getSimpleValue(Object json) {
		if(json != null){
			return json.toString();
		}
		return null;
	}
}
