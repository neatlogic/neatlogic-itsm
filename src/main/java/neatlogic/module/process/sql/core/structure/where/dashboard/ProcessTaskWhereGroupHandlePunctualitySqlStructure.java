/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

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

package neatlogic.module.process.sql.core.structure.where.dashboard;

import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSlaTimeSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.DashboardProcessSqlBase;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskWhereGroupHandlePunctualitySqlStructure extends DashboardProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.GROUP_HANDLE_PUNCTUALITY.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "where";
    }

    @Override
    public void doService(StringBuilder sqlSb, DashboardWidgetParamVo dashboardWidgetParamVo) {
        groupWhereService(dashboardWidgetParamVo, sqlSb);
        sqlSb.append(String.format(" AND %s.`%s` = 'handle' and %s.%s = '%s'", new ProcessTaskStepSlaTimeSqlTable().getShortName(),ProcessTaskStepSlaTimeSqlTable.FieldEnum.TYPE.getValue(),
                new ProcessTaskStepSqlTable().getShortName(),ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskStatus.SUCCEED.getValue()));
    }
}