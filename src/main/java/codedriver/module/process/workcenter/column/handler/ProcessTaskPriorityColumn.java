package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.framework.process.workcenter.table.PrioritySqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		JSONObject priorityJson = new JSONObject();
		if(processTaskVo.getPriority() == null){
			priorityJson.put("value", "服务已删除");
			priorityJson.put("text", "服务已删除");
			priorityJson.put("color", "rgb(255, 102, 102)");
		}else {
			priorityJson.put("value", processTaskVo.getPriority().getUuid());
			priorityJson.put("text", processTaskVo.getPriority().getName());
			priorityJson.put("color", processTaskVo.getPriority().getColor());
		}
		return priorityJson;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new PrioritySqlTable(), Arrays.asList(
						new SelectColumnVo(PrioritySqlTable.FieldEnum.UUID.getValue(),"priorityUuid"),
						new SelectColumnVo(PrioritySqlTable.FieldEnum.NAME.getValue(),"priorityName"),
						new SelectColumnVo(PrioritySqlTable.FieldEnum.COLOR.getValue(),"priorityColor")
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new PrioritySqlTable(), new HashMap<String, String>() {{
					put(ProcessTaskSqlTable.FieldEnum.PRIORITY_UUID.getValue(), PrioritySqlTable.FieldEnum.UUID.getValue());
				}}));
			}
		};
	}

	@Override
	public String getMySortSqlColumn(){
		return PrioritySqlTable.FieldEnum.SORT.getValue();
	}

	@Override
	public ISqlTable getMySortSqlTable(){
		return new PrioritySqlTable();
	}
}
