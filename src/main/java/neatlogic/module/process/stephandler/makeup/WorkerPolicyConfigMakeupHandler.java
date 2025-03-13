/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
