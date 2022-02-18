/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dashboard.core;

import codedriver.framework.common.constvalue.dashboard.DashboardShowConfig;
import codedriver.framework.dashboard.constvalue.IDashboardGroupField;
import codedriver.framework.dashboard.core.IDashboardChartCustom;
import codedriver.framework.dashboard.dto.DashboardShowConfigVo;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class DashboardChartProcessBase implements IDashboardChartCustom {
    @Override
    public List<IDashboardGroupField> getGroupFields(){
        return getMyGroupFields();
    }

    public List<IDashboardGroupField> getMyGroupFields(){
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
    public JSONArray getGroupFieldsConfig(){
        return getMyGroupFieldsConfig();
    }

    public JSONArray getMyGroupFieldsConfig(){
        return getFieldsConfig(getGroupFields());
    }


    /**
     * 获取分组选项渲染配置
     * @return 分组选项渲染配置
     */
    private JSONArray getFieldsConfig(List<IDashboardGroupField> getGroupFields){
        JSONArray groupFieldJsonArray = new JSONArray();
        for (IDashboardGroupField groupField : getGroupFields) {
            groupFieldJsonArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", groupField.getValue(), groupField.getText(), new JSONObject())));
        }
        return groupFieldJsonArray;
    }

    @Override
    public List<IDashboardGroupField> getSubGroupFields(){
        return getMySubGroupFields();
    }

    @Override
    public JSONArray getSubGroupFieldsConfig(){
        return getMySubGroupFieldsConfig();
    }

    public JSONArray getMySubGroupFieldsConfig(){
        return getFieldsConfig(getSubGroupFields());
    }

    public List<IDashboardGroupField> getMySubGroupFields(){
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
    public JSONArray getShowConfig(JSONObject showConfigJson){
        JSONArray processTaskShowChartConfigArray = new JSONArray();
        for (DashboardShowConfig gs : DashboardShowConfig.values()) {
            JSONObject showConfig = showConfigJson.getJSONObject(gs.getValue());
            if(showConfig == null){
                continue;
            }
            DashboardShowConfigVo showConfigVo = showConfig.toJavaObject(DashboardShowConfigVo.class);
            if (Objects.equals(gs.getValue(), DashboardShowConfig.GROUPFIELD.getValue())) {
                showConfigVo.getDataList().addAll(getGroupFieldsConfig());
            } else if (Objects.equals(gs.getValue(), DashboardShowConfig.SUBGROUPFIELD.getValue())) {
                showConfigVo.getDataList().addAll(getSubGroupFieldsConfig());
            }
            processTaskShowChartConfigArray.add(showConfigVo);
        }
        return processTaskShowChartConfigArray;
    }

    @Override
    public String getModule(){
      return "process";
    }

}
