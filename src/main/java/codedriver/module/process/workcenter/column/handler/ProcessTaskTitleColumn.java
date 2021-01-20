package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.module.process.workcenter.core.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskTitleColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

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
		return json.getString(this.getName());
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
		return "fontBold";
	}

	@Override
	public Integer getSort() {
		return 1;
	}

	@Override
	public Object getSimpleValue(Object json) {
		if(json != null){
			return json.toString();
		}
		return null;
	}

	@Override
	public Map<ISqlTable,List<String>> getMySqlTableColumnMap(){
		return new HashMap<ISqlTable,List<String>>(){
			{
				put(new ProcessTaskSqlTable(), Collections.singletonList("title"));
			}
		};
	}
}
