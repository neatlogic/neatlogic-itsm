package codedriver.module.process.workcenter.core.table;

import codedriver.framework.process.workcenter.table.ISqlTable;
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
public class ProcessTaskSqlTable implements ISqlTable {

    @Override
    public String getName() {
        return "processtask";
    }

    @Override
    public String getShortName() {
        return "pt";
    }


    @Override
    public String getJoinKey() {
        return "id";
    }

    @Override
    public Map<ISqlTable,Map<String,String>> getDependTableColumnMap() {
        return new HashMap<>();
    }
}
