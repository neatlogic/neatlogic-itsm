/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
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
