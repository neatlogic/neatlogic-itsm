/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.core.processtask.fromjoin;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.module.process.sql.core.processtask.ProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskFromJoinSubGroupCountSqlStructure extends ProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.SUB_GROUP_COUNT.getValue();
    }

    @Override
    public String getDataSourceHandlerName() {
        return "processtask";
    }

    @Override
    public String getSqlStructureName() {
        return "fromJoin";
    }

    @Override
    public void doMyService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        getJoinTableOfSubGroupColumn(sqlSb, workcenterVo);
    }
}
