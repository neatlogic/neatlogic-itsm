package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ISqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		return json.getString(this.getName());
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
		return "fontBold";
	}

	@Override
	public Integer getSort() {
		return 4;
	}

	/*@Override
	public Object getSimpleValue(Object json) {
		if(json != null){
			return json.toString();
		}
		return null;
	}*/
	@Override
	public String getSimpleValue(ProcessTaskVo processTaskVo) {
		return processTaskVo.getTitle();
	}

	@Override
	public ISqlTable getSortSqlTable(){
		return new ProcessTaskSqlTable();
	}

	@Override
	public String getSortSqlColumn(Boolean isColumn){
		return ProcessTaskSqlTable.FieldEnum.TITLE.getText();
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		return processTaskVo.getTitle();
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
						new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.TITLE.getValue())
				)));
			}
		};
	}

}
