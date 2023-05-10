/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.dashboard.statistics;

import neatlogic.framework.dashboard.dto.DashboardWidgetAllGroupDefineVo;
import neatlogic.framework.dashboard.dto.DashboardWidgetVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.dto.DashboardWidgetParamVo;
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
