/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.dashboard.statistics;

import neatlogic.framework.applicationlistener.core.ModuleInitializedListenerBase;
import neatlogic.framework.bootstrap.CodedriverWebApplicationContext;
import neatlogic.framework.common.RootComponent;

import java.util.HashMap;
import java.util.Map;

@RootComponent
public class DashboardStatisticsFactory extends ModuleInitializedListenerBase {
    private static final Map<String, StatisticsBase> statisticsMap = new HashMap<>();

    public static StatisticsBase getStatistics(String statistics) {
        return statisticsMap.get(statistics);
    }

    @Override
    protected void onInitialized(CodedriverWebApplicationContext context) {
        Map<String, StatisticsBase> myMap = context.getBeansOfType(StatisticsBase.class);
        for (Map.Entry<String, StatisticsBase> entry : myMap.entrySet()) {
            StatisticsBase statistics = entry.getValue();
                statisticsMap.put(statistics.getName(), statistics);
        }
    }

    @Override
    protected void myInit() {

    }
}
