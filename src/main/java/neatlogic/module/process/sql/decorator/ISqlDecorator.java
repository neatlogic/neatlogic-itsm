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
