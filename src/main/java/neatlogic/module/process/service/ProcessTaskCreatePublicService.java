package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONObject;

public interface ProcessTaskCreatePublicService {
    /**
     * 创建工单
     *
     * @param paramObj 创建工单所需参数
     * @return
     * @throws Exception
     */
    JSONObject createProcessTask(JSONObject paramObj) throws Exception;
}
