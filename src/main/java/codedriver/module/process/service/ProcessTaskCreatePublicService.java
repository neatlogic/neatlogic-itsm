package codedriver.module.process.service;

import com.alibaba.fastjson.JSONObject;

public interface ProcessTaskCreatePublicService {
    JSONObject createProcessTask(JSONObject paramObj) throws Exception;
}
