package codedriver.module.process.workcenter.core.table;

import codedriver.framework.process.workcenter.table.ISqlTable;
import codedriver.module.process.workcenter.core.SqlBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: ProcessTaskTable
 * @Package: codedriver.module.process.workcenter.core.table
 * @Description: 工单表
 * @Author: 89770
 * @Date: 2021/1/15 16:02
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Component
public class UserTable implements ISqlTable {

    @Override
    public String getName() {
        return "user";
    }

    @Override
    public String getShortName() {
        return "u";
    }


    @Override
    public String getJoinKey() {
        return FieldEnum.UUID.getValue();
    }

    @Override
    public Map<ISqlTable, Map<String, String>> getDependTableColumnMap() {
        return new HashMap<>();
    }

    public enum FieldEnum {
        UUID("uuid", "用户UUID"),
        USER_ID("user_id", "用户ID"),
        USER_NAME("user_name", "用户名"),
        USER_INFO("user_info", "用户信息"),
        VIP_LEVEL("vip_level", "vip等级"),
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
