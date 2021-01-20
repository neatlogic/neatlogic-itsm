package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTableFactory;
import codedriver.module.process.workcenter.core.SqlBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        List<ISqlTable> tableList = null;
        //如果是distinct id 则 只需要 根据条件获取需要的表。否则需要根据column获取需要的表
        if(SqlBuilder.FieldTypeEnum.DISTINCT_ID.getValue().equals(workcenterVo.getSqlFieldType())){
            tableList = getJoinTableOfCondition(sqlSb,workcenterVo);
        }else{
            tableList = getJoinTableOfColumn(sqlSb);
        }

        //去掉processtask表，因为它是主表
        tableList = tableList.stream().filter(o ->!"processtask".contains(o.getName())).collect(Collectors.toList());
        sqlSb.append(" from  processtask pt ");

        //根据表拼凑 left join sql
        for (ISqlTable table : tableList){
            Map<ISqlTable, Map<String, String>> tableMapMap = table.getDependTableColumnMap();
            List<String> onList = new ArrayList<>();
            for (Map.Entry<ISqlTable,Map<String,String>> entry : tableMapMap.entrySet()){
                ISqlTable dependTable = entry.getKey();
                Map<String,String> dependMap = entry.getValue();
                for (Map.Entry<String,String> depend : dependMap.entrySet()) {
                    onList.add(String.format(" %s.%s = %s.%s ", dependTable.getShortName(), depend.getKey(),table.getShortName(),depend.getValue()));
                }
            }
            sqlSb.append(String.format(" LEFT JOIN %s %s ON %s",table.getName(),table.getShortName(),String.join(" and ",onList)));
        }
    }

    /**
     * @Description: 如果是distinct id 则 只需要 根据条件获取需要的表
     * @Author: 89770
     * @Date: 2021/1/20 16:36
     * @Params: []
     * @Returns: void
     **/
    private List<ISqlTable> getJoinTableOfCondition(StringBuilder sqlSb,WorkcenterVo workcenterVo){
        //根据接口入参的返回需要的conditionList,然后获取需要关联的tableList
        List<ISqlTable> tableList = new ArrayList<>();
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        for(ConditionGroupVo groupVo : groupList ){
            List<ConditionVo> conditionVoList =  groupVo.getConditionList();
            for(ConditionVo conditionVo :conditionVoList){
                IProcessTaskCondition conditionHandler = (IProcessTaskCondition)ConditionHandlerFactory.getHandler(conditionVo.getName());
                if(conditionHandler != null){
                    tableList.addAll(conditionHandler.getSqlTableList());
                }
            }
        }
        //table去重
        return  tableList.stream().filter(distinctByKey(ISqlTable::getName)).collect(Collectors.toList());

    }

    private List<ISqlTable> getJoinTableOfColumn(StringBuilder sqlSb){
        List<ISqlTable> tableList = new ArrayList<>();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        Map<String, ISqlTable> tableComponentMap =  ProcessTaskSqlTableFactory.tableComponentMap;
        Set<ISqlTable> tableSet = new HashSet<>();
        //循环所有需要展示的字段
        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
            IProcessTaskColumn column = entry.getValue();
            Map<ISqlTable, List<String>> map = column.getSqlTableColumnMap();
            for(Map.Entry<ISqlTable, List<String>> tcEntry : map.entrySet()){
                ISqlTable table = tcEntry.getKey();
                Map<ISqlTable,Map<String,String>> dependTableColumnMap = table.getDependTableColumnMap();
               // sqlSb.append(String.format(" LEFT JOIN %s %s ON %s.%s = %s"))
            }

        }
        return tableList;
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
