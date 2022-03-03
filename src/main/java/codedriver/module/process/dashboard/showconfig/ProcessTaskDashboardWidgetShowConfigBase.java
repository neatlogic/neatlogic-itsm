/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.showconfig;

import codedriver.framework.dashboard.constvalue.IDashboardGroupField;
import codedriver.framework.dashboard.config.DashboardWidgetShowConfigBase;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.module.process.dashboard.constvalue.ProcessTaskDashboardStatistics;
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
