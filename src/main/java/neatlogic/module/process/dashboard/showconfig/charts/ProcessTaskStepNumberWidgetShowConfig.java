/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.dashboard.showconfig.charts;

import neatlogic.framework.common.constvalue.dashboard.ChartType;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dashboard.constvalue.IDashboardGroupField;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import neatlogic.module.process.dashboard.showconfig.ProcessTaskStepDashboardWidgetShowConfigBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProcessTaskStepNumberWidgetShowConfig extends ProcessTaskStepDashboardWidgetShowConfigBase {
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
    public List<IDashboardGroupField> getMyGroupFields() {
        return Arrays.asList(
                ProcessWorkcenterField.STEP_STATUS
        );
    }

    @Override
    public JSONArray getMySubGroupFieldsConfig() {
        return null;
    }


    @Override
    public JSONArray getStatisticsOptionList() {
        return new JSONArray();
    }

}
