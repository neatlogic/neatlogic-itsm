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
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessService;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESSTASK_MODIFY.class)
@Transactional
public class UpdateProcessTaskConfigApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessService processService;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getName() {
        return "nmpapm.updateprocesstaskconfigapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "common.config", minSize = 1, isRequired = true)
    })
    @Output({})
    @Description(desc = "nmpapm.updateprocesstaskconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        List<ProcessTaskSlaVo> oldProcessTaskSlaList = processTaskSlaMapper.getProcessTaskSlaListByProcessTaskId(processTaskId);
        JSONObject newConfig = paramObj.getJSONObject("config");
        try {
            ProcessMessageManager.setOperationType(OperationTypeEnum.UPDATE);
            ProcessConfigUtil.regulateProcessConfig(newConfig);
        } finally {
            ProcessMessageManager.release();
        }
        String configHash = processTaskVo.getConfigHash();
        String oldConfigStr = selectContentByHashMapper.getProcessTaskConfigStringByHash(configHash);
        JSONObject oldConfig = JSONObject.parseObject(oldConfigStr);
        String processUuid = UuidUtil.randomUuid();
        String processName = "为了修改工单" + processTaskId + "流程图快照临时创建的流程图";
        setProcessUuidAndName(oldConfig, processUuid, processName);
        setProcessUuidAndName(newConfig, processUuid, processName);
        if (Objects.equals(JSON.toJSONString(oldConfig, SerializerFeature.MapSortField), JSON.toJSONString(newConfig, SerializerFeature.MapSortField))) {
            resultObj.put("message", "没有修改工单流程图快照");
            return resultObj;
        }
        ProcessVo oldProcessVo = processMapper.getProcessByUuid(processTaskVo.getProcessUuid());
        processService.saveOrDeleteProcessDependency(oldProcessVo, "delete");

        JSONObject config = mergeConfig(oldConfig, newConfig);
        ProcessVo processVo = new ProcessVo();
        processVo.setUuid(processUuid);
        processVo.setName(processName);
        processVo.setConfig(config);
        processService.saveProcess(processVo);
        processTaskService.saveProcessTask(processTaskVo, processUuid);
        processTaskMapper.insertProcessTaskHistoryConfigHash(processTaskId, configHash, UserContext.get().getUserUuid());
        processService.saveOrDeleteProcessDependency(processVo, "delete");
        processMapper.deleteProcessByUuid(processUuid);

        processService.saveOrDeleteProcessDependency(oldProcessVo, "save");
        resultObj.put("message", "已修改工单流程图快照");

        List<ProcessTaskSlaVo> newProcessTaskSlaList = processTaskSlaMapper.getProcessTaskSlaListByProcessTaskId(processTaskId);
        if (oldProcessTaskSlaList.size() != newProcessTaskSlaList.size()
                || Objects.equals(JSON.toJSONString(oldProcessTaskSlaList, SerializerFeature.SortField, SerializerFeature.MapSortField), JSON.toJSONString(oldProcessTaskSlaList, SerializerFeature.SortField, SerializerFeature.MapSortField))
        ) {
            // 重新计算时效
            processStepHandlerUtil.calculateSla(new ProcessTaskVo(processTaskId), false);
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "processtask/config/update";
    }

    private void setProcessUuidAndName(JSONObject config, String uuid, String name) {
        JSONObject process = config.getJSONObject("process");
        if (MapUtils.isNotEmpty(process)) {
            JSONObject processConfig = process.getJSONObject("processConfig");
            if (MapUtils.isNotEmpty(processConfig)) {
                processConfig.put("uuid", uuid);
                processConfig.put("name", name);
            }
        }
    }

    private JSONObject mergeConfig(JSONObject oldConfig, JSONObject newConfig) {
        JSONObject config = new JSONObject();
        JSONObject oldProcess = oldConfig.getJSONObject("process");
        JSONObject newProcess = newConfig.getJSONObject("process");
        if (MapUtils.isNotEmpty(oldProcess) && MapUtils.isNotEmpty(newProcess)
                && !Objects.equals(JSON.toJSONString(oldProcess, SerializerFeature.MapSortField), JSON.toJSONString(newProcess, SerializerFeature.MapSortField))) {
            JSONObject process = mergeProcess(oldProcess, newProcess);
            config.put("process", process);
        } else {
            config.put("process", oldProcess);
        }
        JSONObject oldTopo = oldConfig.getJSONObject("topo");
        JSONObject newTopo = newConfig.getJSONObject("topo");
        if (MapUtils.isNotEmpty(oldTopo) && MapUtils.isNotEmpty(newTopo)
                && !Objects.equals(JSON.toJSONString(oldTopo, SerializerFeature.MapSortField), JSON.toJSONString(newTopo, SerializerFeature.MapSortField))) {
            JSONObject topo = mergeTopo(oldTopo, newTopo);
            config.put("topo", topo);
        } else {
            config.put("topo", oldTopo);
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
                && !Objects.equals(JSON.toJSONString(oldStepList, SerializerFeature.MapSortField), JSON.toJSONString(newStepList, SerializerFeature.MapSortField))) {
            JSONArray stepList = mergeStepList(oldStepList, newStepList);
            process.put("stepList", stepList);
        } else {
            process.put("stepList", oldStepList);
        }
        JSONArray oldConnectionList = oldProcess.getJSONArray("connectionList");
//        JSONArray newConnectionList = newProcess.getJSONArray("connectionList");
        process.put("connectionList", oldConnectionList);
        return process;
    }

    private JSONArray mergeStepList(JSONArray oldStepList, JSONArray newStepList) {
        JSONArray stepList = new JSONArray();
        Map<String, JSONObject> newStepMap = new HashMap<>();
        for (int i = 0; i < newStepList.size(); i++) {
            JSONObject newStep = newStepList.getJSONObject(i);
            if (MapUtils.isNotEmpty(newStep)) {
                String uuid = newStep.getString("uuid");
                if (StringUtils.isNotBlank(uuid)) {
                    newStepMap.put(uuid, newStep);
                }
            }
        }
        for (int i = 0; i < oldStepList.size(); i++) {
            JSONObject oldStep = oldStepList.getJSONObject(i);
            if (MapUtils.isNotEmpty(oldStep)) {
                String uuid = oldStep.getString("uuid");
                if (StringUtils.isNotBlank(uuid)) {
                    JSONObject newStep = newStepMap.get(uuid);
                    if (MapUtils.isNotEmpty(newStep)) {
                        JSONObject step = new JSONObject();
                        step.putAll(oldStep);
                        step.put("name", newStep.get("name"));
                        step.put("stepConfig", newStep.get("stepConfig"));
                        stepList.add(step);
                    } else {
                        stepList.add(oldStep);
                    }
                }
            }
        }
        return stepList;
    }

    private JSONObject mergeTopo(JSONObject oldTopo, JSONObject newTopo) {
        JSONObject topo = new JSONObject();
        JSONArray oldCanvas = oldTopo.getJSONArray("canvas");
//        JSONArray newCanvas = newTopo.getJSONArray("canvas");
        topo.put("canvas", oldCanvas);
        JSONArray oldNodes = oldTopo.getJSONArray("nodes");
        JSONArray newNodes = newTopo.getJSONArray("nodes");
        if (CollectionUtils.isNotEmpty(oldNodes) && CollectionUtils.isNotEmpty(newNodes)
                && !Objects.equals(JSON.toJSONString(oldNodes, SerializerFeature.MapSortField), JSON.toJSONString(newNodes, SerializerFeature.MapSortField))) {
            JSONArray nodes = mergeNodes(oldNodes, newNodes);
            topo.put("nodes", nodes);
        } else {
            topo.put("nodes", oldNodes);
        }
        JSONArray oldLinks = oldTopo.getJSONArray("links");
//        JSONArray newLinks = newTopo.getJSONArray("links");
        topo.put("links", oldLinks);
        return topo;
    }

    private JSONArray mergeNodes(JSONArray oldNodes, JSONArray newNodes) {
        JSONArray nodes = new JSONArray();
        Map<String, JSONObject> newNodeMap = new HashMap<>();
        for (int i = 0; i < newNodes.size(); i++) {
            JSONObject newNode = newNodes.getJSONObject(i);
            if (MapUtils.isNotEmpty(newNode)) {
                String uuid = newNode.getString("uuid");
                if (StringUtils.isNotBlank(uuid)) {
                    newNodeMap.put(uuid, newNode);
                }
            }
        }
        for (int i = 0; i < oldNodes.size(); i++) {
            JSONObject oldNode = oldNodes.getJSONObject(i);
            if (MapUtils.isNotEmpty(oldNode)) {
                String uuid = oldNode.getString("uuid");
                if (StringUtils.isNotBlank(uuid)) {
                    JSONObject newNode = newNodeMap.get(uuid);
                    if (MapUtils.isNotEmpty(newNode)) {
                        JSONObject node = new JSONObject();
                        node.putAll(oldNode);
                        JSONObject oldStep = oldNode.getJSONObject("config");
                        JSONObject newStep = newNode.getJSONObject("config");
                        if (MapUtils.isNotEmpty(newStep)) {
                            JSONObject step = new JSONObject();
                            step.putAll(oldStep);
                            step.put("name", newStep.get("name"));
                            step.put("stepConfig", newStep.get("stepConfig"));
                            node.put("config", step);
                        } else {
                            node.put("config", oldStep);
                        }
                        nodes.add(node);
                    } else {
                        nodes.add(oldNode);
                    }
                }
            }
        }
        return nodes;
    }

}
