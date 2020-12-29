package codedriver.module.process.workcenter.operate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;

public class WorkcenterBuildOperateBuilder {
    JSONObject buildOperateJson = new JSONObject();
    
    public JSONObject build() {
        return buildOperateJson;
    }
   
    public WorkcenterBuildOperateBuilder setOperate(ProcessTaskOperationType action) {
        buildOperateJson.put("name", action.getValue());
        buildOperateJson.put("text", action.getText());
        return this;
    }
    
    public WorkcenterBuildOperateBuilder setSort(int sort) {
        buildOperateJson.put("sort", sort);
        return this;
    }
    
    public WorkcenterBuildOperateBuilder setConfig(JSONObject configJson) {
        buildOperateJson.put("config", configJson);
        return this;
    }
    
    public WorkcenterBuildOperateBuilder setIsEnable(int isEnable) {
        buildOperateJson.put("isEnable", isEnable);
        return this;
    }
    
    public WorkcenterBuildOperateBuilder setHandleArray(JSONArray handleArray) {
        buildOperateJson.put("handleArray", handleArray);
        return this;
    }
}
