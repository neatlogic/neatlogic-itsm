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

package neatlogic.module.process.dashboard.showconfig;

import neatlogic.framework.dashboard.constvalue.IDashboardGroupField;
import neatlogic.framework.dashboard.config.DashboardWidgetShowConfigBase;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import neatlogic.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Arrays;
import java.util.List;

public abstract class ProcessTaskDashboardWidgetShowConfigBase extends DashboardWidgetShowConfigBase {
    @Override
    public String getName() {
        return "processtask";
    }

    @Override
    public List<IDashboardGroupField> getGroupFieldOptionList() {
        return getMyGroupFields();
    }

    public List<IDashboardGroupField> getMyGroupFields() {
        return Arrays.asList(
                ProcessWorkcenterField.PRIORITY,
                ProcessWorkcenterField.STATUS,
                ProcessWorkcenterField.CHANNELTYPE,
                ProcessWorkcenterField.CHANNEL,
                ProcessWorkcenterField.STEP_USER,
                ProcessWorkcenterField.OWNER
        );
    }

    @Override
    public JSONArray getGroupFieldOptionListConfig() {
        return getMyGroupFieldsConfig();
    }

    public JSONArray getMyGroupFieldsConfig() {
        return getFieldsConfig(getGroupFieldOptionList());
    }


    /**
     * 获取分组选项渲染配置
     *
     * @return 分组选项渲染配置
     */
    private JSONArray getFieldsConfig(List<IDashboardGroupField> getGroupFields) {
        JSONArray groupFieldJsonArray = new JSONArray();
        for (IDashboardGroupField groupField : getGroupFields) {
            groupFieldJsonArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", groupField.getValue(), groupField.getText(), new JSONObject())));
        }
        return groupFieldJsonArray;
    }

    @Override
    public List<IDashboardGroupField> getSubGroupFieldOptionList() {
        return getMySubGroupFields();
    }

    @Override
    public JSONArray getSubGroupFieldOptionListConfig() {
        return getMySubGroupFieldsConfig();
    }

    public JSONArray getMySubGroupFieldsConfig() {
        return getFieldsConfig(getSubGroupFieldOptionList());
    }

    public List<IDashboardGroupField> getMySubGroupFields() {
        return Arrays.asList(
                ProcessWorkcenterField.PRIORITY,
                ProcessWorkcenterField.STATUS,
                ProcessWorkcenterField.CHANNELTYPE,
                ProcessWorkcenterField.CHANNEL,
                ProcessWorkcenterField.STEP_USER,
                ProcessWorkcenterField.OWNER
        );
    }

    @Override
    public String getModule() {
        return "process";
    }

    @Override
    public JSONArray getStatisticsOptionList() {
        return JSONArray.parseArray(
                String.format("[{'value':'%s','text':'%s','isDefault':0},{'value':'%s','text':'%s','isDefault':0}]", ProcessTaskDashboardStatistics.AVG_HANDLE_COST_TIME.getValue(), ProcessTaskDashboardStatistics.AVG_HANDLE_COST_TIME.getText()
                        , ProcessTaskDashboardStatistics.AVG_RESPONSE_COST_TIME.getValue(), ProcessTaskDashboardStatistics.AVG_RESPONSE_COST_TIME.getText())
        );
    }

}
