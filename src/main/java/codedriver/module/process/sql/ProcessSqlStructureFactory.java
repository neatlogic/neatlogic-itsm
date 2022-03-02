/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.sql;

import codedriver.framework.applicationlistener.core.ModuleInitializedListenerBase;
import codedriver.framework.bootstrap.CodedriverWebApplicationContext;
import codedriver.framework.common.RootComponent;

import java.util.HashMap;
import java.util.Map;

@RootComponent
public class ProcessSqlStructureFactory extends ModuleInitializedListenerBase {
    private static final Map<String, IProcessSqlStructure> processSqlStructureMap = new HashMap<>();

    public static IProcessSqlStructure getProcessSqlStructure(String dataSourceHandlerName, String sqlStructureName, String name) {
        return processSqlStructureMap.get(String.format("%s_%s_%s", dataSourceHandlerName, sqlStructureName, name));
    }

    @Override
    protected void onInitialized(CodedriverWebApplicationContext context) {
        Map<String, IProcessSqlStructure> myMap = context.getBeansOfType(IProcessSqlStructure.class);
        for (Map.Entry<String, IProcessSqlStructure> entry : myMap.entrySet()) {
            IProcessSqlStructure processSqlStructure = entry.getValue();
            processSqlStructureMap.put(String.format("%s_%s_%s", processSqlStructure.getDataSourceHandlerName(), processSqlStructure.getSqlStructureName(), processSqlStructure.getName()), processSqlStructure);
        }
    }

    @Override
    protected void myInit() {

    }
}
