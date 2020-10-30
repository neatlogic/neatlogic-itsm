package codedriver.module.process.workcenter.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;

public class WorkcenterBuildActionBuilder {
    JSONObject buildActionJson = new JSONObject();
    
    public JSONObject build() {
        return buildActionJson;
    }
   
    public WorkcenterBuildActionBuilder setAction(ProcessTaskOperationType action) {
        buildActionJson.put("name", action.getValue());
        buildActionJson.put("text", action.getText());
        return this;
    }
    
    public WorkcenterBuildActionBuilder setSort(int sort) {
        buildActionJson.put("sort", sort);
        return this;
    }
    
    public WorkcenterBuildActionBuilder setConfig(JSONObject configJson) {
        buildActionJson.put("config", configJson);
        return this;
    }
    
    public WorkcenterBuildActionBuilder setIsEnable(int isEnable) {
        buildActionJson.put("isEnable", isEnable);
        return this;
    }
    
    public WorkcenterBuildActionBuilder setHandleArray(JSONArray handleArray) {
        buildActionJson.put("handleArray", handleArray);
        return this;
    }
}
