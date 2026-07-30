/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.process.portal.widget.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerBase;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessTaskConditionType;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskSearchConditionPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Override
    public String getHandler() {
        return "process.processTaskSearchCondition";
    }

    @Override
    protected JSONObject getMyData(JSONObject jsonObj) {
        JSONArray resultArray = new JSONArray();
        String conditionModel = jsonObj.getString("conditionModel");
        FormConditionModel formConditionModel = FormConditionModel.getFormConditionModel(conditionModel);
        formConditionModel = formConditionModel == null ? FormConditionModel.CUSTOM : formConditionModel;
        //固定字段条件
        for(ProcessWorkcenterField f : ProcessWorkcenterField.values() ){
            IProcessTaskCondition condition = ProcessTaskConditionFactory.getHandler(f.getValue());
            if (condition == null || !condition.isShow(jsonObj, ProcessTaskConditionType.WORKCENTER.getValue())) {
                continue;
            }
            JSONObject config = condition.getConfig(ConditionConfigType.WORKCENTER);
            JSONObject commonObj = new JSONObject();
            commonObj.put("handler", condition.getName());
            commonObj.put("handlerName", condition.getDisplayName());
            commonObj.put("handlerType", condition.getHandler(formConditionModel));
            if (config != null) {
                commonObj.put("isMultiple", config.getBoolean("isMultiple"));
            }
            commonObj.put("conditionModel", condition.getHandler(formConditionModel));
            commonObj.put("type", condition.getType());
            commonObj.put("config", config);
            commonObj.put("sort", condition.getSort());
            commonObj.put("desc", condition.getDesc());
            ParamType paramType = condition.getParamType();
            if (paramType != null) {
                commonObj.put("defaultExpression", paramType.getDefaultExpression().getExpression());
                JSONArray expressionArray = new JSONArray();
                for (Expression expression : paramType.getExpressionList()) {
                    JSONObject expressionObj = new JSONObject();
                    expressionObj.put("expression", expression.getExpression());
                    expressionObj.put("expressionName", expression.getExpressionName());
                    expressionArray.add(expressionObj);
                    commonObj.put("expressionList", expressionArray);
                }
            }

            resultArray.add(commonObj);
        }
        resultArray.sort((o1, o2) -> {
            try {
                JSONObject obj1 = (JSONObject) o1;
                JSONObject obj2 = (JSONObject) o2;
                return obj1.getIntValue("sort") - obj2.getIntValue("sort");
            } catch (Exception ignored) {

            }
            return 0;
        });
        return new JSONObject().fluentPut("tbodyList", resultArray);
    }
}
