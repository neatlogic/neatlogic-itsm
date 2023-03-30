package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessTaskIdColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "id";
	}

	@Override
	public String getDisplayName() {
		return "handler.processtask.column.id";
	}

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String id = json.getString(this.getName());
		return id;
	}*/

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

	/*@Override
	public Object getSimpleValue(Object json) {
		if(json != null){
			return json.toString();
		}
		return null;
	}*/

	@Override
	public String getSimpleValue(ProcessTaskVo taskVo) {
		return taskVo.getId().toString();
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		return processTaskVo.getId();
	}

	@Override
    public Boolean getMyIsShow() {
        return false;
    }

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
						new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue())
				)));
			}
		};
	}
}
