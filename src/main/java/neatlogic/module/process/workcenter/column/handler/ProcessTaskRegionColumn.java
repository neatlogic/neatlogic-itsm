/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
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

import neatlogic.framework.util.$;
@Component
public class ProcessTaskRegionColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "region";
	}

	@Override
	public String getDisplayName() {
		return $.t("nmpwch.processtaskregioncolumn.getdisplayname");
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
