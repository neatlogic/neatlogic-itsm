/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql.decorator;

import codedriver.framework.process.dto.SqlDecoratorVo;
import codedriver.module.process.dashboard.handler.ProcessTaskDashboardHandler;
import codedriver.module.process.sql.IProcessSqlStructure;
import codedriver.module.process.sql.ProcessSqlStructureFactory;
import org.springframework.stereotype.Component;

@Component
public class SqlFromJoinDecorator extends SqlDecoratorBase {

    /**
     * @Description: 构建回显字段sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:53
     * @Params: []
     * @Returns: java.lang.String
     **/
    @Override
    public <T extends SqlDecoratorVo> void myBuild(StringBuilder sqlSb, T sqlDecoratorVo) {
        IProcessSqlStructure processSqlStructure = ProcessSqlStructureFactory.getProcessSqlStructure(ProcessTaskDashboardHandler.class.getName(),"fromJoin", sqlDecoratorVo.getSqlFieldType());
        if(processSqlStructure != null) {
            processSqlStructure.doService(sqlSb, sqlDecoratorVo);
        }
    }



    @Override
    public int getSort() {
        return 3;
    }

}
