package codedriver.module.process.workcenter.core.sqldecorator;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

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
    @Override
    public void myBuild(StringBuilder sqlSb, WorkcenterVo workcenterVo) {
        if(FieldTypeEnum.DISTINCT_ID.getValue().equals(workcenterVo.getSqlFieldType())) {
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
                            sqlSb.append(String.format(" %s.%s %s ", column.getSortSqlTable().getShortName(), column.getSortSqlColumn(), value));
                        }
                    }
                }
            } else {
                IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(ProcessWorkcenterField.STARTTIME.getValue());
                sqlSb.append(String.format(" %s.%s %s ", column.getSortSqlTable().getShortName(), column.getSortSqlColumn(), " DESC "));
            }
        }
    }

    @Override
    public int getSort() {
        return 5;
    }
}
