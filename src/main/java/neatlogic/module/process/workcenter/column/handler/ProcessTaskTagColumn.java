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
import neatlogic.framework.process.dto.ProcessTagVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTagSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskTagSqlTable;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessTaskTagColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String getDisplayName() {
        return "标签";
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
        return 20;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo taskVo) {
        return getValue(taskVo).toString();
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        if (CollectionUtils.isNotEmpty(processTaskVo.getTagVoList())) {
            return processTaskVo.getTagVoList().stream().map(ProcessTagVo::getName).collect(Collectors.joining("、"));
        }
        return "-";
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return List.of(
                new TableSelectColumnVo(new ProcessTagSqlTable(), Arrays.asList(
                        new SelectColumnVo(ProcessTagSqlTable.FieldEnum.ID.getValue(), ProcessTagSqlTable.FieldEnum.ID.getProValue(), true),
                        new SelectColumnVo(ProcessTagSqlTable.FieldEnum.NAME.getValue(), ProcessTagSqlTable.FieldEnum.NAME.getProValue())
                ))
        );
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return List.of(
                new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskTagSqlTable(), List.of(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskTagSqlTable.FieldEnum.PROCESSTASK_ID.getValue()))),
                new JoinTableColumnVo(new ProcessTaskTagSqlTable(), new ProcessTagSqlTable(), List.of(new JoinOnVo(ProcessTaskTagSqlTable.FieldEnum.TAG_ID.getValue(), ProcessTagSqlTable.FieldEnum.ID.getValue())))
        );
    }

    @Override
    public Boolean getMyIsShow() {
        return false;
    }
}
