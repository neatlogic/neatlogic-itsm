/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.stephandler.makeup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.ProcessStepWorkerPolicyVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class WorkerPolicyConfigMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private ProcessMapper processMapper;

    @Override
    public String getName() {
        return "workerPolicyConfig";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        /* 组装分配策略 **/
        JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
        if (MapUtils.isNotEmpty(workerPolicyConfig)) {
            JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
            if (CollectionUtils.isNotEmpty(policyList)) {
                if (Objects.equals(action, "save")) {
                    for (int k = 0; k < policyList.size(); k++) {
                        JSONObject policyObj = policyList.getJSONObject(k);
                        if (!"1".equals(policyObj.getString("isChecked"))) {
                            continue;
                        }
                        ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
                        processStepWorkerPolicyVo.setProcessUuid(processStepVo.getProcessUuid());
                        processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
                        processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
                        processStepWorkerPolicyVo.setSort(k + 1);
                        processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
                        processMapper.insertProcessStepWorkerPolicy(processStepWorkerPolicyVo);
                    }
                }
//                else if (Objects.equals(action, "delete")) {
//                    processMapper.deleteProcessStepWorkerPolicyByProcessStepUuid(processStepVo.getUuid());
//                }
            }
        }
    }
}
