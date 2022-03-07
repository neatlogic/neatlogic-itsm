/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.decorator;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.dashboard.handler.ProcessTaskDashboardHandler;
import codedriver.module.process.sql.IProcessSqlStructure;
import codedriver.module.process.sql.ProcessSqlStructureFactory;
import org.springframework.stereotype.Component;

@Component
public class SqlWhereDecorator extends SqlDecoratorBase {

    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        IProcessSqlStructure processSqlStructure = ProcessSqlStructureFactory.getProcessSqlStructure(ProcessTaskDashboardHandler.class.getName(),"where", workcenterVo.getSqlFieldType());
        if(processSqlStructure != null) {
            processSqlStructure.doService(sqlSb, workcenterVo);
        }
    }

    @Override
    public int getSort() {
        return 4;
    }
}
