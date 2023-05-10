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

public interface ISqlDecorator {

    /**
     * @Description: 构建sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:52
     * @Params: []
     * @Returns: java.lang.String
     **/
    <T extends SqlDecoratorVo> void build(StringBuilder sqlSb, T decorator);


    ISqlDecorator getNextSqlDecorator();

    void setNextSqlDecorator(ISqlDecorator nextSqlDecorator);

    int getSort();

}
