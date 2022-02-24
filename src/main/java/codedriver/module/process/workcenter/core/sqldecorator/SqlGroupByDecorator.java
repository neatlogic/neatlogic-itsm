package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.dashboard.dto.DashboardWidgetChartConfigVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Title: SqlLimitDecorator
 * @Package: codedriver.module.process.workcenter.core
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/2/26 17:41
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class SqlGroupByDecorator extends SqlDecoratorBase {
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        if (Arrays.asList(FieldTypeEnum.GROUP_COUNT.getValue(), FieldTypeEnum.SUB_GROUP_COUNT.getValue()).contains(workcenterVo.getSqlFieldType())) {
            Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
            List<String> columnList = new ArrayList<>();
            List<String> groupList = new ArrayList<>();
            groupList.add(workcenterVo.getDashboardWidgetChartConfigVo().getGroup());
            if (Objects.equals(workcenterVo.getSqlFieldType(), FieldTypeEnum.SUB_GROUP_COUNT.getValue())) {
                groupList.add(workcenterVo.getDashboardWidgetChartConfigVo().getSubGroup());
            }

            for (String group : groupList) {
                if (columnComponentMap.containsKey(group)) {
                    IProcessTaskColumn column = columnComponentMap.get(group);
                    for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                        for (SelectColumnVo selectColumnVo : tableSelectColumnVo.getColumnList()) {
                            if (selectColumnVo.getIsPrimary()) {
                                String columnStr = String.format("%s", selectColumnVo.getPropertyName());
                                if (!columnList.contains(columnStr)) {
                                    columnList.add(columnStr);
                                }
                            }
                        }
                    }

                }
            }
            sqlSb.append(String.format(" group by %s ", String.join(",", columnList)));
        }
        if (FieldTypeEnum.DISTINCT_ID.getValue().equals(workcenterVo.getSqlFieldType())) {
            sqlSb.append(String.format(" group by %s.%s ", new ProcessTaskSqlTable().getShortName(), "id"));
        }

        if (FieldTypeEnum.GROUP_SUM.getValue().equals(workcenterVo.getSqlFieldType())) {
            DashboardWidgetChartConfigVo chartVo = workcenterVo.getDashboardWidgetChartConfigVo();
            String subGroup = chartVo.getSubGroup();
            String subGroupProperty = StringUtils.EMPTY;
            if (StringUtils.isNotBlank(subGroup)) {
                IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(subGroup);
                for (TableSelectColumnVo tableSelectColumnVo : column.getTableSelectColumn()) {
                    Optional<SelectColumnVo> optional = tableSelectColumnVo.getColumnList().stream().filter(SelectColumnVo::getIsPrimary).findFirst();
                    if (optional.isPresent()) {
                        subGroupProperty = optional.get().getPropertyName();
                        subGroupProperty = String.format(", a.%s ",subGroupProperty);
                    }
                }
            }
            sqlSb.append(String.format(" group by a.everyday %s", subGroupProperty));
        }
    }

    @Override
    public int getSort() {
        return 5;
    }
}
