package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ChannelSqlTable;
import neatlogic.framework.process.workcenter.table.ChannelWorkTimeSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.WorkTimeSqlTable;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ProcessTaskWorkTimeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	WorktimeMapper worktimeMapper;
	@Override
	public String getName() {
		return "worktime";
	}

	@Override
	public String getDisplayName() {
		return "时间窗口";
	}

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String worktimeUuid = json.getString(this.getName());
		String worktimeName = StringUtils.EMPTY;
		if(StringUtils.isBlank(worktimeName)) {
			WorktimeVo worktimeVo = worktimeMapper.getWorktimeByUuid(worktimeUuid);
			if(worktimeVo != null) {
				worktimeName = worktimeVo.getName();
			}
		}
		return worktimeName;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 12;
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
		return processTaskVo.getWorktimeName();
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new WorkTimeSqlTable(), Arrays.asList(
						new SelectColumnVo(WorkTimeSqlTable.FieldEnum.UUID.getValue(),"worktimeUuid"),
						new SelectColumnVo(WorkTimeSqlTable.FieldEnum.NAME.getValue(),"worktimeName")
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ChannelSqlTable(), new ArrayList<JoinOnVo>() {{
					add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.CHANNEL_UUID.getValue(), ChannelSqlTable.FieldEnum.UUID.getValue()));
				}}));
				add(new JoinTableColumnVo(new ChannelSqlTable(), new ChannelWorkTimeSqlTable(), new ArrayList<JoinOnVo>() {{
					add(new JoinOnVo(ChannelSqlTable.FieldEnum.UUID.getValue(), ChannelWorkTimeSqlTable.FieldEnum.CHANNEL_UUID.getValue()));
				}}));
				add(new JoinTableColumnVo(new ChannelWorkTimeSqlTable(), new WorkTimeSqlTable(), new ArrayList<JoinOnVo>() {{
					add(new JoinOnVo(ChannelWorkTimeSqlTable.FieldEnum.WORKTIME_UUID.getValue(), WorkTimeSqlTable.FieldEnum.UUID.getValue()));
				}}));
			}
		};
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		return processTaskVo.getWorktimeName();
	}
}
