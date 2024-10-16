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
public class ProcessTaskSerialNumberColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "serialnumber";
	}

	@Override
	public String getDisplayName() {
		return "工单号";
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
		return 3;
	}

	@Override
	public Boolean getMyIsShow() {
		return false;
	}

	@Override
	public String getSimpleValue(ProcessTaskVo processTaskVo) {
		return processTaskVo.getSerialNumber();
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		return processTaskVo.getSerialNumber();
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
						new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getValue(),ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getProName())
				)));
			}
		};
	}
}
