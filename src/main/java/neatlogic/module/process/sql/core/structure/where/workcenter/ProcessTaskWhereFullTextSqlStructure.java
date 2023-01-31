/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.sql.core.structure.where.workcenter;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.module.process.sql.core.structure.WorkcenterProcessSqlBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskWhereFullTextSqlStructure extends WorkcenterProcessSqlBase {

    @Override
    public String getName() {
        return ProcessSqlTypeEnum.FULL_TEXT.getValue();
    }

    @Override
    public String getSqlStructureName() {
        return "where";
    }

    @Override
    public void doService(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" where ");
        buildCommonConditionWhereSql(sqlSb, workcenterVo);
        JSONArray keywordConditionList = workcenterVo.getKeywordConditionList();
        //获取过滤最终工单idList
        if (CollectionUtils.isNotEmpty(keywordConditionList)) {
            for (Object keywordCondition : keywordConditionList) {
                JSONObject keywordConditionJson = JSONObject.parseObject(JSONObject.toJSONString(keywordCondition));
                String handler = keywordConditionJson.getString("name");
                JSONArray valueArray = keywordConditionJson.getJSONArray("valueList");
                List<String> valueList = JSONObject.parseArray(valueArray.toJSONString(), String.class);
                getFullTextSql(sqlSb, String.join("\" \"", valueList), handler);
            }
        } else {//获取关键字搜索下拉选项
            getFullTextSql(sqlSb, workcenterVo.getKeyword(), workcenterVo.getKeywordHandler());
        }
    }

    private void getFullTextSql(StringBuilder sqlSb, String value, String handler) {
        IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(handler);
        if (columnHandler != null) {
            List<String> columnList = new ArrayList<>();
            List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
            for (TableSelectColumnVo columnVo : columnVoList) {
                for (SelectColumnVo column : columnVo.getColumnList()) {
                    columnList.add(String.format("%s.%s", columnVo.getTableShortName(), column.getColumnName()));
                }
            }
            sqlSb.append(String.format(" AND MATCH (%s)  AGAINST ('\"%s\"' IN BOOLEAN MODE) ", String.join(",", columnList), value));
        }
    }
}
