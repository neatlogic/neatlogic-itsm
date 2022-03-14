/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.statistics;

import codedriver.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.dto.DashboardWidgetParamVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class StatisticsBase {
    /**
     * 统计类型名
     * @return 统计类型名
     */
    public abstract String getName();

    /**
     * 统计逻辑
     * @param sqlDecoratorVo  用于过滤条件
     * @param widgetDataVo
     * @param widgetVo
     */
    public abstract List<Map<String, Object>> doService(DashboardWidgetParamVo sqlDecoratorVo, DashboardWidgetAllGroupDefineVo widgetDataVo, DashboardWidgetVo widgetVo);


    /**
     * 获取table 需要 select 出来的 column
     * @return columnList
     */
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<>() ;
    }

    /**
     * 获取需要关联的表和字段
     * @return 表和字段关系
     */
    public List<JoinTableColumnVo> getJoinTableColumnList() {
        return new ArrayList<>();
    }

    /**
     * 获取单位
     * @return 单位
     */
    public String getUnit(){
        return StringUtils.EMPTY;
    }
}
