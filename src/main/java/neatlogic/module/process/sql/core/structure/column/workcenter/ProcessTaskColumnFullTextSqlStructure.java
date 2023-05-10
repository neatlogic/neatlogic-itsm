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

package neatlogic.module.process.sql.core.structure.column.workcenter;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskColumnFullTextSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.FULL_TEXT.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "column";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        JSONArray keywordConditionList = workcenterVo.getKeywordConditionList();
        if (CollectionUtils.isNotEmpty(keywordConditionList)) {
            sqlSb.append(" pt.id  ");
        } else {
            //获取关键字
            IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(workcenterVo.getKeywordHandler());
            if (columnHandler != null) {
                List<String> matchColumnList = new ArrayList<>();
                List<String> columnList = new ArrayList<>();
                List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
                for (TableSelectColumnVo columnVo : columnVoList) {
                    for (SelectColumnVo column : columnVo.getColumnList()) {
                        matchColumnList.add(String.format("%s.%s", columnVo.getTableShortName(), column.getColumnName()));
                        columnList.add(String.format("%s.%s as %s", columnVo.getTableShortName(), column.getColumnName(), column.getPropertyName()));
                    }
                }
                sqlSb.append(String.format(" pt.id , %s ,MATCH (%s)  AGAINST ('\"%s\"' IN BOOLEAN MODE) AS score  ", String.join(",", columnList), String.join(",", matchColumnList), workcenterVo.getKeyword()));
            }
        }
    }
}
