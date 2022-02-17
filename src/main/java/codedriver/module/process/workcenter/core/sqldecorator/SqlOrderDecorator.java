package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: SqlOrderDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: 拼接order sql
 * @Author: 89770
 * @Date: 2021/1/15 11:39
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlOrderDecorator extends SqlDecoratorBase {
    private final Map<String, SqlOrderDecorator.BuildOrder<WorkcenterVo, StringBuilder>> buildOrderMap = new HashMap<>();

    @FunctionalInterface
    public interface BuildOrder<T, StringBuilder> {
        void build(T t, StringBuilder sqlSb);
    }

    /**
     * @Description: 初始化构建sql where
     * @Author: 89770
     * @Date: 2021/1/19 14:35
     * @Params: []
     * @Returns: void
     **/
    @PostConstruct
    public void fieldDispatcherInit() {
        buildOrderMap.put(FieldTypeEnum.FIELD.getValue(), (workcenterVo, sqlSb) -> {
            getFieldSql(sqlSb, workcenterVo);
        });

        buildOrderMap.put(FieldTypeEnum.LIMIT_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            //无需排序
        });

        buildOrderMap.put(FieldTypeEnum.DISTINCT_ID.getValue(), (workcenterVo, sqlSb) -> {
            getDistinctSql(sqlSb, workcenterVo);
        });

        buildOrderMap.put(FieldTypeEnum.TOTAL_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            //无需排序
        });

        buildOrderMap.put(FieldTypeEnum.FULL_TEXT.getValue(), (workcenterVo, sqlSb) -> {
            //关键字搜索条件获取processtaskIdList 不需要排序
            if (!CollectionUtils.isNotEmpty(workcenterVo.getKeywordConditionList())) {
                sqlSb.append(" ORDER BY  score DESC ");
            }
        });

        buildOrderMap.put(FieldTypeEnum.GROUP_COUNT.getValue(), (workcenterVo, sqlSb) -> {
            if (StringUtils.isNotBlank(workcenterVo.getDashboardConfigVo().getSubGroup()) && MapUtils.isNotEmpty(workcenterVo.getDashboardConfigVo().getGroupDataCountMap())) {
                List<String> groupDataList = new ArrayList<>();
                if (MapUtils.isNotEmpty(workcenterVo.getDashboardConfigVo().getGroupDataCountMap())) {
                    for (Map.Entry<String, Object> entry : workcenterVo.getDashboardConfigVo().getGroupDataCountMap().entrySet()) {
                        groupDataList.add(entry.getKey());
                    }
                }
                IProcessTaskColumn columnHandler = ProcessTaskColumnFactory.getHandler(workcenterVo.getDashboardConfigVo().getGroup());
                if (columnHandler != null && MapUtils.isNotEmpty(workcenterVo.getDashboardConfigVo().getGroupDataCountMap())) {
                    List<TableSelectColumnVo> columnVoList = columnHandler.getTableSelectColumn();
                    OUT:
                    for (TableSelectColumnVo columnVo : columnVoList) {
                        for (SelectColumnVo column : columnVo.getColumnList()) {
                            if (column.getIsPrimary()) {
                                sqlSb.append(" ORDER BY ( CASE ");
                                for (int i = 1; i <= groupDataList.size(); i++) {
                                    sqlSb.append(String.format(" WHEN %s = '%s'  THEN %d ", column.getPropertyName(), groupDataList.get(i - 1), i));
                                }
                                break OUT;
                            }
                        }
                    }
                    sqlSb.append(" END ),COUNT(1) DESC ");
                }
            } else {
                sqlSb.append(" order by COUNT(1) DESC");
            }
        });
    }

    private void getFieldSql(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" order by case ");
        for (int i = 0; i < workcenterVo.getProcessTaskIdList().size(); i++) {
            sqlSb.append(String.format(" WHEN  pt.id = %s THEN %s ", workcenterVo.getProcessTaskIdList().get(i).toString(), String.valueOf(i)));
        }
        sqlSb.append(" ELSE 1000 END ASC");
    }

    /**
     * @Description: 获取FIELD、DISTINCT_ID 的 order sql
     * @Author: 89770
     * @Date: 2021/2/8 16:44
     * @Params: [sqlSb, workcenterVo]
     * @Returns: void
     **/
    private void getDistinctSql(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        sqlSb.append(" order by ");
        JSONArray sortJsonArray = workcenterVo.getSortList();
        if (CollectionUtils.isNotEmpty(sortJsonArray)) {
            for (Object sortObj : sortJsonArray) {
                JSONObject sortJson = JSONObject.parseObject(sortObj.toString());
                for (Map.Entry<String, Object> entry : sortJson.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().toString();
                    IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(key);
                    if (column != null && column.getIsSort()) {
                        sqlSb.append(String.format(" %s %s ", column.getSortSqlColumn(false), value));
                    }
                }
            }
        } else {
            IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(ProcessWorkcenterField.STARTTIME.getValue());
            sqlSb.append(String.format(" %s %s ", column.getSortSqlColumn(false), " DESC "));
        }
    }

    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        buildOrderMap.get(workcenterVo.getSqlFieldType()).build(workcenterVo, sqlSb);
    }

    @Override
    public int getSort() {
        return 6;
    }
}
