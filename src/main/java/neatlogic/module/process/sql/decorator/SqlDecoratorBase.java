/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
