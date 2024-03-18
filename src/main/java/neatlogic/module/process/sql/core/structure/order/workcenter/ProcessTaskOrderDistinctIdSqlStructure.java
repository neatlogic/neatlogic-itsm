/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.sql.core.structure.order.workcenter;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProcessTaskOrderDistinctIdSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.DISTINCT_ID.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "order";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" order by ");
        JSONObject sortConfig = workcenterVo.getSortConfig();
        if (MapUtils.isNotEmpty(sortConfig)) {
            for (Map.Entry<String, Object> entry : sortConfig.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(key);
                if (column != null && column.getIsSort()) {
                    sqlSb.append(String.format(" %s %s ", column.getSortSqlColumn(false), value));
                }
            }
        } else {
            IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(ProcessWorkcenterField.STARTTIME.getValue());
            sqlSb.append(String.format(" %s %s ", column.getSortSqlColumn(false), " DESC "));
        }
    }
}
