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
