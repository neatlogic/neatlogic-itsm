/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.core.statistics;

import codedriver.framework.dashboard.dto.DashboardWidgetDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;

public abstract class StatisticsBase {
    /**
     * 统计类型名
     * @return 统计类型名
     */
    public abstract String getName();

    /**
     * 统计逻辑
     * @param workcenterVo 工单中心vo 用于过滤条件
     * @param widgetDataVo
     * @param widgetVo
     */
    public abstract void doService(WorkcenterVo workcenterVo, DashboardWidgetDataVo widgetDataVo, DashboardWidgetVo widgetVo);
}
