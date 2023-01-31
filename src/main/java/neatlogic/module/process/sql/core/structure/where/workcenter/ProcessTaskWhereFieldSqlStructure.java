/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.sql.core.structure.where.workcenter;

import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProcessTaskWhereFieldSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.FIELD.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "where";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" where ");
        //根据column获取需要的表
        sqlSb.append(String.format(" %s.%s in ( %s ) ", new ProcessTaskSqlTable().getShortName(),
                ProcessTaskSqlTable.FieldEnum.ID.getValue(), workcenterVo.getProcessTaskIdList().stream().map(Object::toString).collect(Collectors.joining(","))));
    }
}
