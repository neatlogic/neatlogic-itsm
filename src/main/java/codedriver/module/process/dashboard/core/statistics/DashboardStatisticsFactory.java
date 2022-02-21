package codedriver.module.process.dashboard.core.statistics;

import codedriver.framework.applicationlistener.core.ModuleInitializedListenerBase;
import codedriver.framework.bootstrap.CodedriverWebApplicationContext;
import codedriver.framework.common.RootComponent;

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
