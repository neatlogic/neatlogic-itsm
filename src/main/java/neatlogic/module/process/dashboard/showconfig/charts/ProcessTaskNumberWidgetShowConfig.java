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

package neatlogic.module.process.dashboard.showconfig.charts;

import neatlogic.framework.common.constvalue.dashboard.ChartType;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dashboard.constvalue.IDashboardGroupField;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.module.process.dashboard.showconfig.ProcessTaskDashboardWidgetShowConfigBase;
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
