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

package neatlogic.module.process.api.processtask.manualintervention;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESSTASK_MODIFY.class)
public class UpdateProcessTaskConfigApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "nmpapm.updateprocesstaskconfigapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "processUuid", type = ApiParamType.LONG, desc = "term.itsm.processuuid"),
    })
    @Output({})
    @Description(desc = "nmpapm.updateprocesstaskconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        String processUuid = paramObj.getString("processUuid");
        if (StringUtils.isBlank(processUuid)) {
            processUuid = processTaskVo.getProcessUuid();
        }
        ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
        if (processVo == null) {
            throw new ProcessNotFoundException(processUuid);
        }
        String configStr = processVo.getConfigStr();
        String configHash = DigestUtils.md5DigestAsHex(configStr.getBytes());
        ProcessTaskConfigVo processTaskConfigVo = new ProcessTaskConfigVo();
        processTaskConfigVo.setConfig(configStr);
        processTaskConfigVo.setHash(configHash);
        processTaskMapper.insertIgnoreProcessTaskConfig(processTaskConfigVo);
        processTaskMapper.updateProcessTaskConfigHashById(processTaskId, configHash);
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
        Map<String, ProcessTaskStepVo> processTaskStepMap = processTaskStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getProcessStepUuid, e -> e));
        JSONObject config = JSONObject.parseObject(configStr);
        JSONObject process = config.getJSONObject("process");
        JSONArray stepList = process.getJSONArray("stepList");
        if (CollectionUtils.isNotEmpty(stepList)) {
            for (int i = 0; i < stepList.size(); i++) {
                JSONObject stepObj = stepList.getJSONObject(i);
                if (MapUtils.isNotEmpty(stepObj)) {
                    String uuid = stepObj.getString("uuid");
                    ProcessTaskStepVo processTaskStepVo = processTaskStepMap.get(uuid);
                    if (processTaskStepVo == null) {
                        continue;
                    }
                    Long processTaskStepId = processTaskStepVo.getId();
                    String name = stepObj.getString("name");
                    JSONObject stepConfigObj = stepObj.getJSONObject("stepConfig");
                    String stepConfig = stepConfigObj.toJSONString();
                    String stepConfigHash = DigestUtils.md5DigestAsHex(stepConfig.getBytes());
                    if (Objects.equals(name, processTaskStepVo.getName()) && Objects.equals(stepConfigHash, processTaskStepVo.getConfigHash())) {
                        continue;
                    }
                    if (!Objects.equals(stepConfigHash, processTaskStepVo.getConfigHash())) {
                        processTaskMapper.insertIgnoreProcessTaskStepConfig(new ProcessTaskStepConfigVo(stepConfigHash, stepConfig));
                        processTaskMapper.deleteProcessTaskStepWorkerPolicyByProcessTaskStepId(processTaskStepId);
                        JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
                        if (MapUtils.isNotEmpty(workerPolicyConfig)) {
                            JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
                            if (CollectionUtils.isNotEmpty(policyList)) {
                                List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = new ArrayList<>();
                                for (int k = 0; k < policyList.size(); k++) {
                                    JSONObject policyObj = policyList.getJSONObject(k);
                                    if (!"1".equals(policyObj.getString("isChecked"))) {
                                        continue;
                                    }
                                    ProcessTaskStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
                                    processStepWorkerPolicyVo.setProcessTaskId(processTaskId);
                                    processStepWorkerPolicyVo.setProcessTaskStepId(processTaskStepId);
                                    processStepWorkerPolicyVo.setProcessStepUuid(uuid);
                                    processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
                                    processStepWorkerPolicyVo.setSort(k + 1);
                                    processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
                                    workerPolicyList.add(processStepWorkerPolicyVo);
                                }
                                if (CollectionUtils.isNotEmpty(workerPolicyList)) {
                                    processTaskMapper.insertProcessTaskStepWorkerPolicyList(workerPolicyList);
                                }
                            }
                        }
                    }
                    processTaskMapper.updateProcessTaskStepNameAndConfigHashByProcessTaskIdAndProcessStepUuid(processTaskId, uuid, name, stepConfigHash);
                }
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "manualintervention/processtask/config/update";
    }
}
