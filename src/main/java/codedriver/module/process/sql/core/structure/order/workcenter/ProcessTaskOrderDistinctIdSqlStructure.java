/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.structure.order.workcenter;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import com.alibaba.fastjson.JSONObject;
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
