/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.service;

import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.automatic.AutomaticConfigVo;
import com.alibaba.fastjson.JSONObject;

/**
 * @author lvzk
 * @since 2021/8/16 15:47
 **/
public interface ProcessTaskAutomaticService {
//    Boolean runRequest(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo);
//
//    JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo, AutomaticConfigVo automaticConfigVo, JSONObject data, String type);
//
//    void initJob(AutomaticConfigVo automaticConfigVo, ProcessTaskStepVo currentProcessTaskStepVo, JSONObject data);

    void firstRequest(ProcessTaskStepVo currentProcessTaskStepVo);

    boolean callbackRequest(ProcessTaskStepVo currentProcessTaskStepVo);

    AutomaticConfigVo getAutomaticConfigVoByProcessTaskStepId(Long processTaskStepId);

}
