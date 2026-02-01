/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.constvalue.ProcessStepType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.dto.score.ProcessScoreTemplateVo;
import neatlogic.framework.process.dto.score.ScoreTemplateDimensionVo;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import neatlogic.module.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.module.process.service.ProcessService;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
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
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessService processService;

    @Resource
    private FormMapper formMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public String getName() {
        return "nmpapm.updateprocesstaskconfigapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "流程配置内容", minSize = 1, isRequired = true)
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid")
    })
    @Description(desc = "nmpapm.updateprocesstaskconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        JSONObject newConfig = paramObj.getJSONObject("config");
        try {
            ProcessMessageManager.setOperationType(OperationTypeEnum.UPDATE);
            ProcessConfigUtil.regulateProcessConfig(newConfig);
        } finally {
            ProcessMessageManager.release();
        }
        String oldConfigStr = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
        JSONObject oldConfig = JSONObject.parseObject(oldConfigStr);
        if (Objects.equals(JSON.toJSONString(oldConfig, SerializerFeature.MapSortField), JSON.toJSONString(newConfig, SerializerFeature.MapSortField))) {
            resultObj.put("message", "没有修改工单流程图快照");
            return resultObj;
        }
        JSONObject config = mergeConfig(oldConfig, newConfig);
        String processUuid = UuidUtil.randomUuid();
        ProcessVo processVo = new ProcessVo();
        processVo.setUuid(processUuid);
        processVo.setName("为了修改工单" + processTaskId + "流程图快照临时创建的流程图");
        processVo.setConfig(config);
        processService.saveProcess(processVo);
        saveProcessTask(processTaskVo, processUuid);
        processService.saveOrDeleteProcessDependency(processVo, "delete");
        processMapper.deleteProcessByUuid(processUuid);
        resultObj.put("message", "已修改工单流程图快照");
        return resultObj;
    }

    @Override
    public String getToken() {
        return "processtask/config/update";
    }

    private JSONObject mergeConfig(JSONObject oldConfig, JSONObject newConfig) {
        JSONObject config = new JSONObject();
        JSONObject oldProcess = oldConfig.getJSONObject("process");
        JSONObject newProcess = newConfig.getJSONObject("process");
        if (MapUtils.isNotEmpty(oldProcess) && MapUtils.isNotEmpty(newProcess)
                && !Objects.equals(JSON.toJSONString(oldProcess, SerializerFeature.MapSortField), JSON.toJSONString(newProcess, SerializerFeature.MapSortField))) {
            JSONObject process = mergeProcess(oldProcess, newProcess);
            config.put("process", process);
        }
        JSONObject oldTopo = oldConfig.getJSONObject("topo");
        JSONObject newTopo = newConfig.getJSONObject("topo");
        if (MapUtils.isNotEmpty(oldTopo) && MapUtils.isNotEmpty(newTopo)
                && !Objects.equals(JSON.toJSONString(oldTopo, SerializerFeature.MapSortField), JSON.toJSONString(newTopo, SerializerFeature.MapSortField))) {
            JSONObject topo = mergeTopo(oldTopo, newTopo);
            config.put("topo", topo);
        }
        return config;
    }

    private JSONObject mergeProcess(JSONObject oldProcess, JSONObject newProcess) {
        JSONObject process = new JSONObject();
//        JSONArray oldSlaList = oldProcess.getJSONArray("slaList");
        JSONArray newSlaList = newProcess.getJSONArray("slaList");
        process.put("slaList", newSlaList);
//        JSONObject oldFormConfig = oldProcess.getJSONObject("formConfig");
        JSONObject newFormConfig = newProcess.getJSONObject("formConfig");
        process.put("formConfig", newFormConfig);
//        JSONObject oldProcessConfig = oldProcess.getJSONObject("processConfig");
        JSONObject newProcessConfig = newProcess.getJSONObject("processConfig");
        process.put("processConfig", newProcessConfig);
//        JSONObject oldScoreConfig = oldProcess.getJSONObject("scoreConfig");
        JSONObject newScoreConfig = newProcess.getJSONObject("scoreConfig");
        process.put("scoreConfig", newScoreConfig);
        JSONArray oldStepList = oldProcess.getJSONArray("stepList");
        JSONArray newStepList = newProcess.getJSONArray("stepList");
        if (CollectionUtils.isNotEmpty(oldStepList) && CollectionUtils.isNotEmpty(newStepList)
                && !Objects.equals(JSON.toJSONString(oldStepList, SerializerFeature.MapSortField), JSON.toJSONString(oldStepList, SerializerFeature.MapSortField))) {

            process.put("stepList", oldStepList);
        } else {
            process.put("stepList", oldStepList);
        }
        JSONArray oldConnectionList = oldProcess.getJSONArray("connectionList");
//        JSONArray newConnectionList = newProcess.getJSONArray("connectionList");
        process.put("connectionList", oldConnectionList);
        return process;
    }

    private JSONArray mergeStepList() {
        return null;
    }

    private JSONObject mergeTopo(JSONObject oldTopo, JSONObject newTopo) {
        JSONArray oldCanvas = oldTopo.getJSONArray("canvas");
        JSONArray oldNodes = oldTopo.getJSONArray("nodes");
        JSONArray oldLinks = oldTopo.getJSONArray("links");
        JSONArray newCanvas = newTopo.getJSONArray("canvas");
        JSONArray newNodes = newTopo.getJSONArray("nodes");
        JSONArray newLinks = newTopo.getJSONArray("links");
        return null;
    }

    private JSONArray mergeNodes() {
        return null;
    }

    private Long saveProcessTask(ProcessTaskVo processTaskVo, String processUuid) {
        Long processTaskId = processTaskVo.getId();
        ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
        String formUuid = processVo.getFormUuid();
        Long startProcessTaskStepId = null;
//        JSONObject process = config.getJSONObject("process");
//        JSONObject formConfig = process.getJSONObject("formConfig");
//        JSONArray stepList = process.getJSONArray("stepList");
        {
            String oldFormUuid = null;
            ProcessTaskFormVo oldProcessTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
            if (oldProcessTaskFormVo != null) {
                oldFormUuid = oldProcessTaskFormVo.getFormUuid();
            }
            if (!Objects.equals(oldFormUuid, formUuid)) {
                processTaskMapper.deleteProcessTaskFormByProcessTaskId(processTaskId);
                /* 写入表单信息 **/
                if (StringUtils.isNotBlank(formUuid)) {
                    FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(formUuid);
                    if (formVersionVo != null && MapUtils.isNotEmpty(formVersionVo.getFormConfig())) {
                        ProcessTaskFormVo processTaskFormVo = new ProcessTaskFormVo();
                        processTaskFormVo.setFormContent(JSON.toJSONString(formVersionVo.getFormConfig(), SerializerFeature.MapSortField));
                        processTaskFormVo.setProcessTaskId(processTaskId);
                        processTaskFormVo.setFormUuid(formVersionVo.getFormUuid());
                        processTaskFormVo.setFormName(formVersionVo.getFormName());
                        processTaskMapper.insertProcessTaskForm(processTaskFormVo);
                        processTaskMapper.insertIgnoreProcessTaskFormContent(processTaskFormVo);
                    }
                }
            }
        }
        {
            ProcessTaskScoreTemplateVo oldProcessTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskId);
            ProcessScoreTemplateVo processScoreTemplateVo = processMapper.getProcessScoreTemplateByProcessUuid(processUuid);
            if (processScoreTemplateVo != null) {
                ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = new ProcessTaskScoreTemplateVo(processScoreTemplateVo);
                JSONObject processTaskScoreTemplateConfig = processTaskScoreTemplateVo.getConfig();
                if (processTaskScoreTemplateConfig != null) {
//                IScoreTemplateCrossoverMapper scoreTemplateCrossoverMapper = CrossoverServiceFactory.getApi(IScoreTemplateCrossoverMapper.class);
                    List<ScoreTemplateDimensionVo> scoreTemplateDimensionList = scoreTemplateMapper.getScoreTemplateDimensionListByScoreTemplateId(processTaskScoreTemplateVo.getScoreTemplateId());
                    processTaskScoreTemplateConfig.put("scoreTemplateDimensionList", scoreTemplateDimensionList);
                    ProcessTaskScoreTemplateConfigVo processTaskScoreTemplateConfigVo = new ProcessTaskScoreTemplateConfigVo(processTaskScoreTemplateConfig.toJSONString());
                    processTaskScoreTemplateVo.setConfigHash(processTaskScoreTemplateConfigVo.getHash());
                    if (oldProcessTaskScoreTemplateVo == null
                            || !Objects.equals(oldProcessTaskScoreTemplateVo.getConfigHash(), processTaskScoreTemplateVo.getConfigHash())) {
                        processTaskMapper.insertProcessTaskScoreTemplateConfig(processTaskScoreTemplateConfigVo);
                    }
                }
                if (oldProcessTaskScoreTemplateVo == null
                        || !Objects.equals(oldProcessTaskScoreTemplateVo.getIsAuto(), processTaskScoreTemplateVo.getIsAuto())
                        || !Objects.equals(oldProcessTaskScoreTemplateVo.getScoreTemplateId(), processTaskScoreTemplateVo.getScoreTemplateId())
                        || !Objects.equals(oldProcessTaskScoreTemplateVo.getConfigHash(), processTaskScoreTemplateVo.getConfigHash())
                ) {
                    processTaskScoreTemplateVo.setProcessTaskId(processTaskId);
                    processTaskMapper.insertProcessTaskScoreTemplate(processTaskScoreTemplateVo);
                }
            } else {
                if (oldProcessTaskScoreTemplateVo != null) {
                    processTaskMapper.deleteProcessTaskScoreTemplateByProcessTaskId(processTaskId);
                }
            }
        }

        List<ProcessTaskStepVo> oldProcessTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
        Map<String, ProcessTaskStepVo> oldProcessTaskStepMap = oldProcessTaskStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getProcessStepUuid, e -> e));
        List<ProcessTaskStepTagVo> oldProcessTaskStepTagList = processTaskMapper.getProcessTaskStepTagListByProcessTaskId(processTaskId);
        Map<String, List<ProcessTaskStepWorkerPolicyVo>> oldProcessTaskStepWorkerPolicyListMap = new HashMap<>();
        ProcessTaskStepWorkerPolicyVo searchWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        searchWorkerPolicyVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepWorkerPolicyVo> oldProcessTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(searchWorkerPolicyVo);
        for (ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo : oldProcessTaskStepWorkerPolicyList) {
            oldProcessTaskStepWorkerPolicyListMap.computeIfAbsent(processTaskStepWorkerPolicyVo.getProcessStepUuid(), key -> new ArrayList<>()).add(processTaskStepWorkerPolicyVo);
        }
        Map<String, Long> stepIdMap = new HashMap<>();
        List<ProcessTaskStepVo> processTaskStepList = new ArrayList<>();
        List<ProcessTaskStepConfigVo> processTaskStepConfigList = new ArrayList<>();
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = new ArrayList<>();
        List<ProcessTaskStepTagVo> processTaskStepTagList = new ArrayList<>();
        /* 写入所有步骤信息 **/
        List<ProcessStepTagVo> processStepTagList = processMapper.getProcessStepTagListByProcessUuid(processUuid);
        List<ProcessStepVo> processStepList = processMapper.getProcessStepDetailByProcessUuid(processUuid);
        for (ProcessStepVo stepVo : processStepList) {
            ProcessTaskStepVo ptStepVo = new ProcessTaskStepVo(stepVo);
            ptStepVo.setStatus(ProcessTaskStepStatus.PENDING.getValue());
            ptStepVo.setProcessTaskId(processTaskId);
            String stepConfig = stepVo.getConfig();
            if (StringUtils.isNotBlank(stepConfig)) {
                /* 对步骤配置进行散列处理 **/
                String hash = DigestUtils.md5DigestAsHex(stepConfig.getBytes());
                ptStepVo.setConfigHash(hash);
                processTaskStepConfigList.add(new ProcessTaskStepConfigVo(hash, stepConfig));
            }
            ProcessTaskStepVo oldPtStepVo = oldProcessTaskStepMap.get(ptStepVo.getProcessStepUuid());
            if (oldPtStepVo != null) {
                ptStepVo.setId(oldPtStepVo.getId());
                if (!Objects.equals(oldPtStepVo.getName(), ptStepVo.getName())
                        || !Objects.equals(oldPtStepVo.getConfigHash(), ptStepVo.getConfigHash())) {
                    processTaskStepList.add(ptStepVo);
                }
            } else {
                ptStepVo.setId(SnowflakeUtil.uniqueLong());
                processTaskStepList.add(ptStepVo);
            }

            stepIdMap.put(ptStepVo.getProcessStepUuid(), ptStepVo.getId());
            /* 找到开始节点 **/
            if (ptStepVo.getType().equals(ProcessStepType.START.getValue())) {
                startProcessTaskStepId = ptStepVo.getId();
            }

            {
                Map<String, ProcessTaskStepWorkerPolicyVo> oldWorkerPolicyMap = new HashMap<>();
                List<ProcessTaskStepWorkerPolicyVo> oldWorkerPolicyList = oldProcessTaskStepWorkerPolicyListMap.get(ptStepVo.getProcessStepUuid());
                if (CollectionUtils.isNotEmpty(oldWorkerPolicyList)) {
                    oldWorkerPolicyMap = oldWorkerPolicyList.stream().collect(Collectors.toMap(ProcessTaskStepWorkerPolicyVo::getPolicy, e -> e));
                }
                /* 写入用户分配策略信息 **/
                if (CollectionUtils.isNotEmpty(ptStepVo.getWorkerPolicyList())) {
                    for (ProcessTaskStepWorkerPolicyVo policyVo : ptStepVo.getWorkerPolicyList()) {
                        ProcessTaskStepWorkerPolicyVo oldWorkerPolicyVo = oldWorkerPolicyMap.remove(policyVo.getPolicy());
                        if (oldWorkerPolicyVo == null
                                || !Objects.equals(oldWorkerPolicyVo.getSort(), policyVo.getSort())
                                || !Objects.equals(oldWorkerPolicyVo.getConfig(), policyVo.getConfig())) {
                            policyVo.setProcessTaskId(processTaskId);
                            policyVo.setProcessTaskStepId(ptStepVo.getId());
                            processTaskStepWorkerPolicyList.add(policyVo);
                        }
                    }
                }
                if (MapUtils.isNotEmpty(oldWorkerPolicyMap)) {
                    for (Map.Entry<String, ProcessTaskStepWorkerPolicyVo> entry : oldWorkerPolicyMap.entrySet()) {
                        processTaskMapper.deleteProcessTaskStepWorkerPolicy(entry.getValue());
                    }
                }
            }

            {
                List<Long> oldTagIdList = new ArrayList<>();
                for (ProcessTaskStepTagVo processTaskStepTagVo : oldProcessTaskStepTagList) {
                    if (Objects.equals(processTaskStepTagVo.getProcessTaskStepId(), ptStepVo.getId())) {
                        oldTagIdList.add(processTaskStepTagVo.getTagId());
                    }
                }
                List<Long> tagIdList = new ArrayList<>();
                for (ProcessStepTagVo processStepTagVo : processStepTagList) {
                    if (Objects.equals(processStepTagVo.getProcessStepUuid(), ptStepVo.getProcessStepUuid())) {
                        tagIdList.add(processStepTagVo.getTagId());
                    }
                }
                oldTagIdList.sort(Long::compareTo);
                tagIdList.sort(Long::compareTo);
                if (!ListUtils.isEqualList(oldTagIdList, tagIdList)) {
                    for (Long oldTagId : oldTagIdList) {
                        if (!tagIdList.contains(oldTagId)) {
                            ProcessTaskStepTagVo tagVo = new ProcessTaskStepTagVo();
                            tagVo.setProcessTaskId(processTaskId);
                            tagVo.setProcessTaskStepId(ptStepVo.getId());
                            tagVo.setTagId(oldTagId);
                            processTaskMapper.deleteProcessTaskStepTag(tagVo);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(tagIdList)) {
                        for (Long tagId : tagIdList) {
                            if (!oldTagIdList.contains(tagId)) {
                                ProcessTaskStepTagVo processTaskStepTagVo = new ProcessTaskStepTagVo();
                                processTaskStepTagVo.setProcessTaskId(processTaskId);
                                processTaskStepTagVo.setProcessTaskStepId(ptStepVo.getId());
                                processTaskStepTagVo.setTagId(tagId);
                                processTaskStepTagList.add(processTaskStepTagVo);
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(processTaskStepList)) {
            processTaskMapper.insertProcessTaskStepList(processTaskStepList);
        }
        if (CollectionUtils.isNotEmpty(processTaskStepConfigList)) {
            processTaskMapper.insertIgnoreProcessTaskStepConfigList(processTaskStepConfigList);
        }
        if (CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
            processTaskMapper.insertProcessTaskStepWorkerPolicyList(processTaskStepWorkerPolicyList);
        }
        if (CollectionUtils.isNotEmpty(processTaskStepTagList)) {
            processTaskMapper.insertProcessTaskStepTagList(processTaskStepTagList);
        }
        {
            List<ProcessTaskStepRelVo> oldProcessTaskStepRelList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
            if (CollectionUtils.isNotEmpty(oldProcessTaskStepRelList)) {
                /* 写入关系信息 **/
                List<ProcessTaskStepRelVo> processTaskStepRelList = new ArrayList<>();
                List<ProcessStepRelVo> processStepRelList = processMapper.getProcessStepRelByProcessUuid(processUuid);
                for (ProcessStepRelVo relVo : processStepRelList) {
                    ProcessTaskStepRelVo processTaskStepRelVo = new ProcessTaskStepRelVo(relVo);
                    processTaskStepRelVo.setProcessTaskId(processTaskId);
                    processTaskStepRelVo.setFromProcessTaskStepId(stepIdMap.get(processTaskStepRelVo.getFromProcessStepUuid()));
                    processTaskStepRelVo.setToProcessTaskStepId(stepIdMap.get(processTaskStepRelVo.getToProcessStepUuid()));
                    /* 同时找到from step id 和to step id 时才写入，其他数据舍弃 **/
                    if (processTaskStepRelVo.getFromProcessTaskStepId() != null && processTaskStepRelVo.getToProcessTaskStepId() != null) {
                        processTaskStepRelList.add(processTaskStepRelVo);
                    }
                }
                if (CollectionUtils.isNotEmpty(processTaskStepRelList)) {
                    processTaskMapper.insertProcessTaskStepRelList(processTaskStepRelList);
                }
            }
        }

        {
            //        IProcessTaskSlaCrossoverMapper processTaskSlaCrossoverMapper = CrossoverServiceFactory.getApi(IProcessTaskSlaCrossoverMapper.class);
            Map<String, ProcessTaskSlaVo> oldProcessTaskSlaMap = new HashMap<>();
            List<ProcessTaskSlaVo> oldProcessTaskSlaList = processTaskSlaMapper.getProcessTaskSlaListByProcessTaskId(processTaskId);
            for (ProcessTaskSlaVo oldProcessTaskSlaVo : oldProcessTaskSlaList) {
                String uuid = null;
                JSONObject configObj = oldProcessTaskSlaVo.getConfigObj();
                if (MapUtils.isNotEmpty(configObj)) {
                    uuid = configObj.getString("uuid");
                }
                if (StringUtils.isNotBlank(uuid)) {
                    oldProcessTaskSlaMap.put(uuid, oldProcessTaskSlaVo);
                }
            }
            /* 写入sla信息 **/
            List<ProcessSlaVo> processSlaList = processMapper.getProcessSlaByProcessUuid(processUuid);
            for (ProcessSlaVo slaVo : processSlaList) {
                ProcessTaskSlaVo oldProcessTaskSlaVo = oldProcessTaskSlaMap.get(slaVo.getUuid());
                List<String> slaStepUuidList = processMapper.getProcessStepUuidBySlaUuid(slaVo.getUuid());
                if (CollectionUtils.isNotEmpty(slaStepUuidList)) {
                    List<Long> oldProcessTaskStepIdList = new ArrayList<>();
                    ProcessTaskSlaVo processTaskSlaVo = new ProcessTaskSlaVo(slaVo);
                    processTaskSlaVo.setProcessTaskId(processTaskId);
                    processTaskSlaVo.setIsActive(1);
                    if (oldProcessTaskSlaVo != null) {
                        processTaskSlaVo.setId(oldProcessTaskSlaVo.getId());
                        oldProcessTaskStepIdList = processTaskSlaMapper.getProcessTaskStepIdListBySlaId(oldProcessTaskSlaVo.getId());
                    } else {
                        processTaskSlaVo.setId(SnowflakeUtil.uniqueLong());
                    }
                    if (oldProcessTaskSlaVo == null || !Objects.equals(oldProcessTaskSlaVo.getConfig(), processTaskSlaVo.getConfig())) {
                        processTaskSlaMapper.insertProcessTaskSla(processTaskSlaVo);
                    }
                    List<Long> stepIdList = new ArrayList<>();
                    for (String suuid : slaStepUuidList) {
                        Long stepId = stepIdMap.get(suuid);
                        if (stepId != null) {
                            stepIdList.add(stepId);
                        }
                    }
                    oldProcessTaskStepIdList.sort(Long::compareTo);
                    stepIdList.sort(Long::compareTo);
                    if (!ListUtils.isEqualList(oldProcessTaskStepIdList, stepIdList)) {
                        for (Long oldStepId : oldProcessTaskStepIdList) {
                            if (!stepIdList.contains(oldStepId)) {
                                processTaskSlaMapper.deleteProcessTaskStepSla(oldStepId, processTaskSlaVo.getId());
                            }
                        }
                        for (Long stepId : stepIdList) {
                            if (!oldProcessTaskStepIdList.contains(stepId)) {
                                processTaskSlaMapper.insertProcessTaskStepSla(stepId, processTaskSlaVo.getId());
                            }
                        }
                    }
                } else {
                    if (oldProcessTaskSlaVo != null) {
                        processTaskSlaMapper.deleteProcessTaskSlaById(oldProcessTaskSlaVo.getId());
                        processTaskSlaMapper.deleteProcessTaskStepSlaBySlaId(oldProcessTaskSlaVo.getId());
                    }
                }
            }
        }
        return startProcessTaskStepId;
    }
}
