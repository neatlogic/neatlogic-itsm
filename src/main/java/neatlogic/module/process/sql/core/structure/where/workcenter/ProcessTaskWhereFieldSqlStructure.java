/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.sql.core.structure.where.workcenter;

import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProcessTaskWhereFieldSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.FIELD.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "where";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" where ");
        //根据column获取需要的表
        sqlSb.append(String.format(" %s.%s in ( %s ) ", new ProcessTaskSqlTable().getShortName(),
                ProcessTaskSqlTable.FieldEnum.ID.getValue(), workcenterVo.getProcessTaskIdList().stream().map(Object::toString).collect(Collectors.joining(","))));
    }
}
