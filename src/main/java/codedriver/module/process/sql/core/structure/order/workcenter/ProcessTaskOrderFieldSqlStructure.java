/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.structure.order.workcenter;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskOrderFieldSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.FIELD.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "order";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" order by case ");
        for (int i = 0; i < workcenterVo.getProcessTaskIdList().size(); i++) {
            sqlSb.append(String.format(" WHEN  pt.id = %s THEN %s ", workcenterVo.getProcessTaskIdList().get(i).toString(), String.valueOf(i)));
        }
        sqlSb.append(" ELSE 1000 END ASC");
    }
}
