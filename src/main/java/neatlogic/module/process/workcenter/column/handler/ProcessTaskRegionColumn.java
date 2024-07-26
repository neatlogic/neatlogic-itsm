/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.RegionSqlTable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ProcessTaskRegionColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "region";
	}

	@Override
	public String getDisplayName() {
		return "地域";
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
		return 9;
	}

	@Override
	public String getSimpleValue(ProcessTaskVo taskVo) {
		return getValue(taskVo).toString();
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		if (processTaskVo.getRegionVo() != null) {
			return processTaskVo.getRegionVo().getUpwardNamePath();
		}
		return "-";
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>() {
			{
				add(new TableSelectColumnVo(new RegionSqlTable(), Arrays.asList(
						new SelectColumnVo(RegionSqlTable.FieldEnum.ID.getValue(), RegionSqlTable.FieldEnum.ID.getProValue(),true),
						new SelectColumnVo(RegionSqlTable.FieldEnum.NAME.getValue(), RegionSqlTable.FieldEnum.NAME.getProValue()),
						new SelectColumnVo(RegionSqlTable.FieldEnum.UPWARD_NAME_PATH.getValue(), RegionSqlTable.FieldEnum.UPWARD_NAME_PATH.getProValue())
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new RegionSqlTable(), new ArrayList<JoinOnVo>() {{
					add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.REGION_ID.getValue(), RegionSqlTable.FieldEnum.ID.getValue()));
				}}));
			}
		};
	}
}
