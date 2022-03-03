package codedriver.module.process.service;

import com.alibaba.fastjson.JSONObject;

public interface ProcessTaskCompleteService {
    /**
     * 工单步骤完成
     *
     * @param paramObj 参数结构见processtask/complete接口
     * @return
     * @throws Exception
     */
    void complete(JSONObject paramObj) throws Exception;
}
