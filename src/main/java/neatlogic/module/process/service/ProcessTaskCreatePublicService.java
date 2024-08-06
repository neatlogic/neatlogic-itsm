package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessTaskCreateVo;

public interface ProcessTaskCreatePublicService {
    /**
     * 创建工单
     *
     * @param processTaskCreateVo 创建工单所需参数
     * @return
     * @throws Exception
     */
    JSONObject createProcessTask(ProcessTaskCreateVo processTaskCreateVo) throws Exception;
}
