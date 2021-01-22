package codedriver.module.process.workcenter.core.table;

import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.module.process.workcenter.core.SqlBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: ProcessTaskStepTable
 * @Package: codedriver.module.process.workcenter.core.table
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/15 16:37
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@Component
public class ProcessTaskStepSqlTable implements ISqlTable {
    @Override
    public String getName() {
        return "processtask_step";
    }

    @Override
    public String getShortName() {
        return "pts";
    }

    @Override
    public String getJoinKey() {
        return FieldEnum.PROCESSTASK_ID.getValue();
    }

    @Override
    public Map<ISqlTable,Map<String,String>> getDependTableColumnMap() {
        return new HashMap<ISqlTable,Map<String,String>>(){
            {
                put(new ProcessTaskSqlTable(),new HashMap<String,String>(){{ put("id","processtask_id");}});
            }
        };
    }

    public enum FieldEnum {
        ID("id", "步骤ID"),
        PROCESSTASK_ID("processtask_id","工单id"),
        STATUS("status", "步骤状态"),
        ;
        private final String name;
        private final String text;

        private FieldEnum(String _value, String _text) {
            this.name = _value;
            this.text = _text;
        }

        public String getValue() {
            return name;
        }

        public String getText() {
            return text;
        }

        public static String getText(String value) {
            for (SqlBuilder.FieldTypeEnum f : SqlBuilder.FieldTypeEnum.values()) {
                if (f.getValue().equals(value)) {
                    return f.getText();
                }
            }
            return "";
        }
    }
}
