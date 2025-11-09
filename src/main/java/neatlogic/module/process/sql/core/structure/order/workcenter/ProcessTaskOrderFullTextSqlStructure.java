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

package neatlogic.module.process.sql.core.structure.order.workcenter;

import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskOrderFullTextSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.FULL_TEXT.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "order";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        //关键字搜索条件获取processtaskIdList 不需要排序
        if (!CollectionUtils.isNotEmpty(workcenterVo.getKeywordConditionList())) {
            sqlSb.append(" ORDER BY  score DESC ");
        }
    }
}
