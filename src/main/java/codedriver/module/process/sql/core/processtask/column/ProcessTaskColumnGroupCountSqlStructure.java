/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.processtask.column;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.processtask.ProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskColumnGroupCountSqlStructure extends ProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_COUNT.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "column";
    }

    @Override
    public String getDataSourceHandlerName() {
        return "processtask";
    }

    @Override
    public void doMyService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        groupColumnService(workcenterVo, sqlSb);
    }
}
