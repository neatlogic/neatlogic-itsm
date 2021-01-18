package codedriver.module.process.workcenter.core.table;

import java.util.HashMap;
import java.util.List;
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
        return "processtask_id";
    }

    @Override
    public Map<String, String> getDependTableColumnMap(List<String> allTaleNameList) {
        String processTaskTableName = new ProcessTaskSqlTable().getName();
        Map<String,String> dependMap = null;
        if(allTaleNameList.contains(processTaskTableName)){
            dependMap = new HashMap<String,String>(){
                {
                    put(processTaskTableName,"id");
                }
            };
        }
        return dependMap;
    }

}
