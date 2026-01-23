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
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

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
        String oldConfigStr = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
        JSONObject oldConfig = JSONObject.parseObject(oldConfigStr);
        JSONObject oldProcess = oldConfig.getJSONObject("process");
        JSONArray oldStepList = oldProcess.getJSONArray("stepList");

        if (CollectionUtils.isNotEmpty(oldStepList)) {
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
            Map<String, ProcessTaskStepVo> processTaskStepMap = processTaskStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getProcessStepUuid, e -> e));
            String configStr = processVo.getConfigStr();
            JSONObject config = JSONObject.parseObject(configStr);
            JSONObject process = config.getJSONObject("process");
            JSONArray stepList = process.getJSONArray("stepList");
            Map<String, JSONObject> stepMap = new HashMap<>();
            for (int i = 0; i < stepList.size(); i++) {
                JSONObject stepObj = stepList.getJSONObject(i);
                String uuid = stepObj.getString("uuid");
                stepMap.put(uuid, stepObj);
            }
            for (int i = 0; i < oldStepList.size(); i++) {
                JSONObject oldStepObj = oldStepList.getJSONObject(i);
                if (MapUtils.isNotEmpty(oldStepObj)) {
                    String uuid = oldStepObj.getString("uuid");
                    ProcessTaskStepVo oldProcessTaskStepVo = processTaskStepMap.get(uuid);
                    if (oldProcessTaskStepVo == null) {
                        continue;
                    }
                    JSONObject stepObj = stepMap.get(uuid);
                    if (stepObj == null) {
                        continue;
                    }
                    String name = stepObj.getString("name");
                    JSONObject stepConfigObj = stepObj.getJSONObject("stepConfig");
                    String stepConfig = stepConfigObj.toJSONString();
                    String stepConfigHash = DigestUtils.md5DigestAsHex(stepConfig.getBytes());
                    Long processTaskStepId = oldProcessTaskStepVo.getId();
                    String oldName = oldStepObj.getString("name");
                    JSONObject oldStepConfigObj = oldStepObj.getJSONObject("stepConfig");
                    String oldStepConfig = oldStepConfigObj.toJSONString();
                    if (Objects.equals(oldName, name) && Objects.equals(oldStepConfig, stepConfig)) {
                        continue;
                    }
                    if (!Objects.equals(oldStepConfig, stepConfig)) {
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
                    oldStepObj.put("name", name);
                    oldStepObj.put("stepConfig", stepConfigObj);
                }
            }
        }
        String newConfigStr = oldConfig.toJSONString();
        if (!Objects.equals(newConfigStr, oldConfigStr)) {
            String configHash = DigestUtils.md5DigestAsHex(newConfigStr.getBytes());
            ProcessTaskConfigVo processTaskConfigVo = new ProcessTaskConfigVo();
            processTaskConfigVo.setConfig(newConfigStr);
            processTaskConfigVo.setHash(configHash);
            processTaskMapper.insertIgnoreProcessTaskConfig(processTaskConfigVo);
            processTaskMapper.updateProcessTaskConfigHashById(processTaskId, configHash);
        }
//        JSONArray connectionList = process.getJSONArray("connectionList");
//        if (CollectionUtils.isNotEmpty(connectionList)) {
//            List<ProcessTaskStepRelVo> processTaskStepRelList = new ArrayList<>();
//            List<ProcessTaskStepRelVo> oldProcessTaskStepRelList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
//            for (int i = 0; i < connectionList.size(); i++) {
//                JSONObject connectionObj = connectionList.getJSONObject(i);
//                if (MapUtils.isNotEmpty(connectionObj)) {
//                    String fromStepUuid = connectionObj.getString("fromStepUuid");
//                    String toStepUuid = connectionObj.getString("toStepUuid");
//                    String name = connectionObj.getString("name");
//                    String type = connectionObj.getString("type");
//                    String uuid = connectionObj.getString("uuid");
//                    String conditionConfig = connectionObj.getString("conditionConfig");
//                    ProcessTaskStepVo fromProcessTaskStepVo = processTaskStepMap.get(fromStepUuid);
//                    if (fromProcessTaskStepVo == null) {
//                        continue;
//                    }
//                    ProcessTaskStepVo toProcessTaskStepVo = processTaskStepMap.get(toStepUuid);
//                    if (toProcessTaskStepVo == null) {
//                        continue;
//                    }
//                    ProcessTaskStepRelVo processTaskStepRelVo = new ProcessTaskStepRelVo();
//                    processTaskStepRelVo.setProcessTaskId(processTaskId);
//                    processTaskStepRelVo.setFromProcessStepUuid(fromStepUuid);
//                    processTaskStepRelVo.setToProcessStepUuid(toStepUuid);
//                    processTaskStepRelVo.setFromProcessTaskStepId(fromProcessTaskStepVo.getId());
//                    processTaskStepRelVo.setToProcessTaskStepId(toProcessTaskStepVo.getId());
//                    processTaskStepRelVo.setCondition(conditionConfig);
//                    processTaskStepRelVo.setProcessStepRelUuid(uuid);
//                    processTaskStepRelVo.setName(name);
//                    processTaskStepRelVo.setType(type);
//                    processTaskStepRelVo.setIsHit(0);
//                    for (ProcessTaskStepRelVo oldProcessTaskStepRelVo : oldProcessTaskStepRelList) {
//                        if (Objects.equals(oldProcessTaskStepRelVo.getFromProcessStepUuid(), processTaskStepRelVo.getFromProcessStepUuid())
//                                && Objects.equals(oldProcessTaskStepRelVo.getToProcessStepUuid(), processTaskStepRelVo.getToProcessStepUuid())
//                                && Objects.equals(oldProcessTaskStepRelVo.getFromProcessTaskStepId(), processTaskStepRelVo.getFromProcessTaskStepId())
//                                && Objects.equals(oldProcessTaskStepRelVo.getToProcessTaskStepId(), processTaskStepRelVo.getToProcessTaskStepId())
//                        ) {
//                            processTaskStepRelVo.setIsHit(oldProcessTaskStepRelVo.getIsHit());
//                        }
//                    }
//                    processTaskStepRelList.add(processTaskStepRelVo);
//                }
//            }
//            if (CollectionUtils.isNotEmpty(processTaskStepRelList)) {
//                processTaskMapper.deleteProcessTaskStepRelByProcessTaskId(processTaskId);
//                processTaskMapper.insertProcessTaskStepRelList(processTaskStepRelList);
//            }
//        }
        return null;
    }

    @Override
    public String getToken() {
        return "manualintervention/processtask/config/update";
    }
}
