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

public abstract class SqlDecoratorBase implements ISqlDecorator{

    protected ISqlDecorator nextSqlDecorator;

    @Override
    public <T extends SqlDecoratorVo> void build(StringBuilder sqlSb, T sqlDecoratorVo){
        myBuild(sqlSb,sqlDecoratorVo);
        //最后一步 limit 没有 next
        if(nextSqlDecorator != null) {
            //构造下一部分 sql
            nextSqlDecorator.build(sqlSb, sqlDecoratorVo);
        }
    }

    public abstract <T extends SqlDecoratorVo> void myBuild(StringBuilder sqlSb, T sqlDecoratorVo);

    @Override
    public ISqlDecorator getNextSqlDecorator() {
        return nextSqlDecorator;
    }

    @Override
    public void setNextSqlDecorator(ISqlDecorator nextSqlDecorator) {
        this.nextSqlDecorator = nextSqlDecorator;
    }
}
