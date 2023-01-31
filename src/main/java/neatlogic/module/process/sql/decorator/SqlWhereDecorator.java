/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.sql.decorator;

import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.module.process.dashboard.handler.ProcessTaskDashboardHandler;
import neatlogic.module.process.sql.IProcessSqlStructure;
import neatlogic.module.process.sql.ProcessSqlStructureFactory;
import org.springframework.stereotype.Component;

@Component
public class SqlWhereDecorator extends SqlDecoratorBase {

    @Override
    public <T extends SqlDecoratorVo> void myBuild(StringBuilder sqlSb, T sqlDecoratorVo) {
        IProcessSqlStructure processSqlStructure = ProcessSqlStructureFactory.getProcessSqlStructure(ProcessTaskDashboardHandler.class.getName(),"where", sqlDecoratorVo.getSqlFieldType());
        if(processSqlStructure != null) {
            processSqlStructure.doService(sqlSb, sqlDecoratorVo);
        }
    }

    @Override
    public int getSort() {
        return 4;
    }
}
