/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.process.sql.decorator;

import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.module.process.sql.IProcessSqlStructure;
import neatlogic.module.process.sql.ProcessSqlStructureFactory;
import org.springframework.stereotype.Component;

@Component
public class SqlColumnDecorator extends SqlDecoratorBase {

    /**
     * @Description: 构建回显字段sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:53
     * @Params: []
     * @Returns: java.lang.String
     **/
    public <T extends SqlDecoratorVo> void myBuild(StringBuilder sqlSb, T sqlDecoratorVo) {
        IProcessSqlStructure processSqlStructure = ProcessSqlStructureFactory.getProcessSqlStructure("column", sqlDecoratorVo.getSqlFieldType());
        if(processSqlStructure != null) {
            processSqlStructure.doService(sqlSb, sqlDecoratorVo);
        }
    }



    @Override
    public int getSort() {
        return 2;
    }

}
