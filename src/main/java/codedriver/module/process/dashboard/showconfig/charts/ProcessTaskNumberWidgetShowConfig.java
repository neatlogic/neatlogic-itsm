/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.showconfig.charts;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.condition.core.IConditionHandler;
import codedriver.framework.dashboard.constvalue.IDashboardGroupField;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.module.process.dashboard.showconfig.ProcessTaskDashboardWidgetShowConfigBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class ProcessTaskNumberWidgetShowConfig extends ProcessTaskDashboardWidgetShowConfigBase {
    @Override
    public String[] getSupportChart() {
        return new String[]{ChartType.NUMBERCHART.getValue()};
    }

    @Override
    public JSONArray getMyGroupFieldsConfig() {
        JSONArray groupFieldJsonArray = new JSONArray();
        for (IDashboardGroupField groupField : getGroupFieldOptionList()) {
            IConditionHandler conditionHandler = ConditionHandlerFactory.getHandler(groupField.getValue());
            JSONObject groupFieldJson = conditionHandler.getConfig();
            groupFieldJson.remove("isMultiple");
            groupFieldJson.put("handler", conditionHandler.getHandler(FormConditionModel.CUSTOM));
            groupFieldJsonArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", groupField.getValue(), groupField.getText(), groupFieldJson)));
        }
        return groupFieldJsonArray;
    }

    @Override
    public JSONArray getMySubGroupFieldsConfig() {
        return null;
    }

}
