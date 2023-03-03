/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
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
