package codedriver.module.process.service;

import com.alibaba.fastjson.JSONObject;

public interface ProcessTaskStartService {
    /**
     * 工单步骤开始
     *
     * @param paramObj 参数结构见processtask/start接口
     * @return
     * @throws Exception
     */
    void start(JSONObject paramObj) throws Exception;
}
