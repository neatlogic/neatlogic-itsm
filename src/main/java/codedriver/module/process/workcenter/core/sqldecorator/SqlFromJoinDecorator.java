package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @Title: SqlColumnDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 11:38
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlFromJoinDecorator extends SqlDecoratorBase {
    /**
     * @Description: 构建回显字段sql语句
     * @Author: 89770
     * @Date: 2021/1/15 11:53
     * @Params: []
     * @Returns: java.lang.String
     **/
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        List<String> joinTableKeyList = new ArrayList<>();
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
        //如果是distinct id 则 只需要 根据条件获取需要的表。否则需要根据column获取需要的表
        if (FieldTypeEnum.DISTINCT_ID.getValue().equals(workcenterVo.getSqlFieldType())) {
            joinTableColumnList = getJoinTableOfCondition(workcenterVo, joinTableKeyList);
        } else {
            joinTableColumnList = getJoinTableOfColumn(workcenterVo, joinTableKeyList);
        }
        //补充排序需要的表
        joinTableColumnList.addAll(getOrderJoinTableOfCondition(workcenterVo, joinTableKeyList));
        sqlSb.append(" from  processtask pt ");
        for (JoinTableColumnVo joinTableColumn : joinTableColumnList) {
            sqlSb.append(joinTableColumn.toString());
        }
    }

    /**
     * @Description: 补充排序需要 join 的表
     * @Author: 89770
     * @Date: 2021/1/26 20:36
     * @Params: [workcenterVo, joinTableKeyList]
     * @Returns: java.util.List<codedriver.framework.process.workcenter.dto.JoinTableColumnVo>
     **/
    private List<JoinTableColumnVo> getOrderJoinTableOfCondition(WorkcenterVo workcenterVo, List<String> joinTableKeyList){
        JSONArray sortJsonArray = workcenterVo.getSortList();
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sortJsonArray)) {
            for (Object sortObj : sortJsonArray) {
                JSONObject sortJson = JSONObject.parseObject(sortObj.toString());
                for (Map.Entry<String, Object> entry : sortJson.entrySet()) {
                    String handler = entry.getKey();
                    IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(handler);
                    if (column != null && column.getIsSort()) {
                        List<JoinTableColumnVo> handlerJoinTableColumnList = column.getJoinTableColumnList();
                        for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                            String key = handlerJoinTableColumn.getHash();
                            if (!joinTableKeyList.contains(handlerJoinTableColumn.getHash())) {
                                joinTableColumnList.add(handlerJoinTableColumn);
                                joinTableKeyList.add(key);
                            }
                        }
                    }
                }
            }
        }
        return joinTableColumnList;
    }

    /**
     * @Description: 如果是distinct id 则 只需要 根据条件获取需要的表
     * @Author: 89770
     * @Date: 2021/1/20 16:36
     * @Params: []
     * @Returns: void
     **/
    private List<JoinTableColumnVo> getJoinTableOfCondition(WorkcenterVo workcenterVo, List<String> joinTableKeyList) {
        //根据接口入参的返回需要的conditionList,然后获取需要关联的tableList
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
        for (ConditionGroupVo groupVo : groupList) {
            List<ConditionVo> conditionVoList = groupVo.getConditionList();
            for (ConditionVo conditionVo : conditionVoList) {
                IProcessTaskCondition conditionHandler = (IProcessTaskCondition) ConditionHandlerFactory.getHandler(conditionVo.getName());
                if (conditionHandler != null) {
                    List<JoinTableColumnVo> handlerJoinTableColumnList = conditionHandler.getJoinTableColumnList();
                    for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                        String key = handlerJoinTableColumn.getHash();
                        if (!joinTableKeyList.contains(key)) {
                            joinTableColumnList.add(handlerJoinTableColumn);
                            joinTableKeyList.add(key);
                        }
                    }
                }
            }
        }
        return joinTableColumnList;
    }

    private List<JoinTableColumnVo> getJoinTableOfColumn(WorkcenterVo workcenterVo, List<String> joinTableKeyList) {
        List<JoinTableColumnVo> joinTableColumnList = new ArrayList<>();
        //根据接口入参的返回需要的columnList,然后获取需要关联的tableList
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //循环所有需要展示的字段
        for (WorkcenterTheadVo theadVo : workcenterVo.getTheadVoList()) {
            if (columnComponentMap.containsKey(theadVo.getName())) {
                IProcessTaskColumn column = columnComponentMap.get(theadVo.getName());
                List<JoinTableColumnVo> handlerJoinTableColumnList = column.getJoinTableColumnList();
                for (JoinTableColumnVo handlerJoinTableColumn : handlerJoinTableColumnList) {
                    String key = handlerJoinTableColumn.getHash();
                    if (!joinTableKeyList.contains(key)) {
                        joinTableColumnList.add(handlerJoinTableColumn);
                        joinTableKeyList.add(key);
                    }
                }
            }
        }
        return joinTableColumnList;
    }

    @Override
    public int getSort() {
        return 3;
    }

    /**
     * @Description: 通过列表中对象的某个字段进行去重
     * @Author: 89770
     * @Date: 2021/1/20 17:14
     * @Params: [keyExtractor]
     * @Returns: java.util.function.Predicate<T>
     **/
    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
