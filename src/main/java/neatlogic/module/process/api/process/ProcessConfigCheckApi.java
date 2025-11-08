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

package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessStepType;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.dto.score.ProcessScoreTemplateVo;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import neatlogic.framework.process.sla.core.ISlaCalculateHandler;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerFactory;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerTypeFactory;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.score.ScoreTemplateMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessConfigCheckApi extends PrivateApiComponentBase {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private ScoreTemplateMapper scoreTemplateMapper;
    @Override
    public String getName() {
        return "检查流程图config与表(process_为前缀)数据是否一致";
    }

    @Input({
            @Param(name = "processUuidList", type = ApiParamType.JSONARRAY, desc = "term.itsm.processuuid"),
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, desc = "term.itsm.processtaskid"),
            @Param(name = "allProcess", type = ApiParamType.BOOLEAN, desc = "所有流程")
    })
    @Output({
            @Param(explode = ProcessVo.class)
    })
    @Description(desc = "检查流程图config与表(process_为前缀)数据是否一致")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        JSONArray processUuidList = paramObj.getJSONArray("processUuidList");
        JSONArray processTaskIdList = paramObj.getJSONArray("processTaskIdList");
        Boolean allProcess = paramObj.getBoolean("allProcess");
        if (CollectionUtils.isNotEmpty(processUuidList)) {
            for (int i = 0; i < processUuidList.size(); i++) {
                String processUuid = processUuidList.getString(i);
                ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
                if (processVo == null) {
                    throw new ProcessNotFoundException(processUuid);
                }
                try {
                    JSONObject jsonObj = checkProcessConfig(processUuid, processVo.getConfig());
                    if (MapUtils.isNotEmpty(jsonObj)) {
                        resultObj.put(processVo.getName() + "_" + processUuid, jsonObj);
                    }
                } catch (Exception e) {
                    resultObj.put(processVo.getName() + "_" + processUuid, e.getMessage());
                }
            }
        } else if (CollectionUtils.isNotEmpty(processTaskIdList)) {
            for (int i = 0; i < processTaskIdList.size(); i++) {
                Long processTaskId = processTaskIdList.getLong(i);
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(processTaskId);
                if (processTaskVo == null) {
                    throw new ProcessTaskNotFoundException(processTaskId);
                }
                try {
                    String configStr = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                    JSONObject config = JSON.parseObject(configStr);
                    JSONObject jsonObj = checkProcessTaskConfig(processTaskId, config);
                    if (MapUtils.isNotEmpty(jsonObj)) {
                        resultObj.put(processTaskVo.getTitle() + "_" + processTaskId.toString(), jsonObj);
                    }
                } catch (Exception e) {
                    resultObj.put(processTaskVo.getTitle() + "_" + processTaskId.toString(), e.getMessage());
                }
            }
        } else if (Objects.equals(allProcess, true)) {
            List<String> uuidList = processMapper.getAllProcessUuidList();
            for (String uuid : uuidList) {
                ProcessVo processVo = processMapper.getProcessByUuid(uuid);
                try {
                    JSONObject jsonObj = checkProcessConfig(uuid, processVo.getConfig());
                    if (MapUtils.isNotEmpty(jsonObj)) {
                        resultObj.put(processVo.getName() + "_" + uuid, jsonObj);
                    }
                } catch (Exception e) {
                    resultObj.put(processVo.getName() + "_" + uuid, e.getMessage());
                }
            }
        }
        return resultObj;
    }

    private JSONObject checkProcessConfig(String processUuid, JSONObject config) {
        JSONObject resultObj = new JSONObject();
        JSONArray list = new JSONArray();
        JSONObject processObj = config.getJSONObject("process");
        if (MapUtils.isEmpty(processObj)) {
            return resultObj;
        }
        JSONObject formConfig = processObj.getJSONObject("formConfig");
        if (MapUtils.isNotEmpty(formConfig)) {
            String formUuid = formConfig.getString("uuid");
            if (StringUtils.isNotBlank(formUuid)) {
                if (formMapper.checkFormIsExists(formUuid) == 0) {
                    throw new FormNotFoundException(formUuid);
                }
                ProcessFormVo newProcessFormVo = new ProcessFormVo(processUuid, formUuid);
                ProcessFormVo oldProcessFormVo = processMapper.getProcessFormByProcessUuid(processUuid);
                if (oldProcessFormVo == null) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("message", "`process_form`表缺少数据");
                    jsonObj.put("newProcessFormVo", newProcessFormVo);
                    list.add(jsonObj);
                } else if (!Objects.equals(oldProcessFormVo.getFormUuid(), formUuid)) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("message", "`process_form`表`form_uuid`字段值不相等");
                    jsonObj.put("newProcessFormVo", newProcessFormVo);
                    jsonObj.put("oldProcessFormVo", oldProcessFormVo);
                    list.add(jsonObj);
                }
            }
        }
        JSONArray slaList = processObj.getJSONArray("slaList");
        if (CollectionUtils.isNotEmpty(slaList)) {
            List<ProcessSlaVo> processSlaList = processMapper.getProcessSlaByProcessUuid(processUuid);
            Map<String, ProcessSlaVo> processSlaMap = processSlaList.stream().collect(Collectors.toMap(ProcessSlaVo::getUuid, e -> e));
            for (int i = 0; i < slaList.size(); i++) {
                JSONObject slaObj = slaList.getJSONObject(i);
                /* 关联了步骤的sla策略才保存 **/
                JSONArray processStepUuidList = slaObj.getJSONArray("processStepUuidList");
                if (CollectionUtils.isNotEmpty(processStepUuidList)) {
                    String calculateHandler = slaObj.getString("calculateHandler");
                    ISlaCalculateHandler slaCalculateHandler = SlaCalculateHandlerFactory.getHandler(calculateHandler);
                    if (slaCalculateHandler == null) {
                        throw new SlaCalculateHandlerNotFoundException(calculateHandler);
                    }
                    ProcessSlaVo processSlaVo = new ProcessSlaVo();
                    processSlaVo.setProcessUuid(processUuid);
                    processSlaVo.setName(slaObj.getString("name"));
                    processSlaVo.setCalculateHandler(calculateHandler);
                    processSlaVo.setConfig(slaObj.toJSONString());

                    if (Objects.equals(slaCalculateHandler.isSum(), 1)) {
                        //关联的多个步骤共用一个时效
                        processSlaVo.setUuid(slaObj.getString("uuid"));
                        ProcessSlaVo oldProcessSla = processSlaMap.get(processSlaVo.getUuid());
                        if (oldProcessSla == null) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("isSum", 1);
                            jsonObj.put("message", "`process_sla`表缺少数据");
                            jsonObj.put("newProcessSlaVo", processSlaVo);
                            list.add(jsonObj);
                        } else {
                            String oldProcessSlaStr = processSlaVoToString(oldProcessSla);
                            String processSlaStr = processSlaVoToString(processSlaVo);
                            if (!Objects.equals(oldProcessSlaStr, processSlaStr)) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("isSum", 1);
                                jsonObj.put("message", "`process_sla`表数据不对");
                                jsonObj.put("newProcessSlaVo", processSlaVo);
                                jsonObj.put("oldProcessSlaVo", oldProcessSla);
                                list.add(jsonObj);
                            }
                        }
                        List<String> slaStepUuidList = processMapper.getProcessStepUuidBySlaUuid(processSlaVo.getUuid());
                        if (!CollectionUtils.isEqualCollection(slaStepUuidList, processStepUuidList)) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("isSum", 1);
                            jsonObj.put("message", "`process_step_sla`表`sla_uuid`值为" + processSlaVo.getUuid() + "时数据不对");
                            jsonObj.put("newStepUuidList", processStepUuidList);
                            jsonObj.put("oldStepUuidList", slaStepUuidList);
                            list.add(jsonObj);
                        }
                    } else {
                        //关联的多个步骤各用一个时效
                        for (int p = 0; p < processStepUuidList.size(); p++) {
                            String stepUuid = processStepUuidList.getString(p);
                            processSlaVo.setUuid(UuidUtil.getCustomUUID(processSlaVo.getName() + "&" + stepUuid));
                            ProcessSlaVo oldProcessSla = processSlaMap.get(processSlaVo.getUuid());
                            if (oldProcessSla == null) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("isSum", 0);
                                jsonObj.put("message", "`process_sla`表缺少数据");
                                jsonObj.put("newProcessSlaVo", processSlaVo);
                                list.add(jsonObj);
                            } else {
                                String oldProcessSlaStr = processSlaVoToString(oldProcessSla);
                                String processSlaStr = processSlaVoToString(processSlaVo);
                                if (!Objects.equals(oldProcessSlaStr, processSlaStr)) {
                                    JSONObject jsonObj = new JSONObject();
                                    jsonObj.put("isSum", 0);
                                    jsonObj.put("message", "`process_sla`表数据不对");
                                    jsonObj.put("newProcessSlaVo", processSlaVo);
                                    jsonObj.put("oldProcessSlaVo", oldProcessSla);
                                    list.add(jsonObj);
                                }
                            }

                            List<String> slaStepUuidList = processMapper.getProcessStepUuidBySlaUuid(processSlaVo.getUuid());
                            if (slaStepUuidList.size() != 1 && slaStepUuidList.contains(stepUuid)) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("isSum", 0);
                                jsonObj.put("message", "`process_step_sla`表`sla_uuid`值为" + processSlaVo.getUuid() + "时数据不对");
                                jsonObj.put("newStepUuidList", Collections.singletonList(stepUuid));
                                jsonObj.put("oldStepUuidList", slaStepUuidList);
                                list.add(jsonObj);
                            }
                        }
                    }
                }
            }
        }
        String virtualStartStepUuid = "";// 虚拟开始节点uuid
        Map<String, ProcessStepVo> stepMap = new HashMap<>();
        JSONArray stepList = processObj.getJSONArray("stepList");
        if (CollectionUtils.isNotEmpty(stepList)) {
            for (int i = 0; i < stepList.size(); i++) {
                JSONObject stepObj = stepList.getJSONObject(i);
                String handler = stepObj.getString("handler");
                if (ProcessStepHandlerType.START.getHandler().equals(handler)) {// 找到虚拟开始节点uuid,虚拟开始节点不写入process_step表
                    virtualStartStepUuid = stepObj.getString("uuid");
                    continue;
                }
                ProcessStepVo processStepVo = new ProcessStepVo();
                processStepVo.setProcessUuid(processUuid);
                processStepVo.setConfig(stepObj.getString("stepConfig"));

                String uuid = stepObj.getString("uuid");
                if (StringUtils.isNotBlank(uuid)) {
                    processStepVo.setUuid(uuid);
                }
                String name = stepObj.getString("name");
                if (StringUtils.isNotBlank(name)) {
                    processStepVo.setName(name);
                }

                if (StringUtils.isNotBlank(handler)) {
                    processStepVo.setHandler(handler);
                    String type = ProcessStepHandlerTypeFactory.getType(handler);
                    processStepVo.setType(type);
                    IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(handler);
                    if (processStepUtilHandler != null) {
                    } else {
                        throw new ProcessStepUtilHandlerNotFoundException(handler);
                    }
                }
                stepMap.put(processStepVo.getUuid(), processStepVo);
            }
            JSONArray relList = processObj.getJSONArray("connectionList");
            if (CollectionUtils.isNotEmpty(relList)) {
                for (int i = 0; i < relList.size(); i++) {
                    ProcessStepRelVo processStepRelVo = relList.getObject(i, ProcessStepRelVo.class);
                    String fromStepUuid = processStepRelVo.getFromStepUuid();
                    String toStepUuid = processStepRelVo.getToStepUuid();
                    if (virtualStartStepUuid.equals(fromStepUuid)) {// 通过虚拟开始节点连线找到真正的开始步骤
                        ProcessStepVo startStep = stepMap.get(toStepUuid);
                        if (startStep != null) {
                            startStep.setType(ProcessStepType.START.getValue());
                        }
                        break;
                    }
                }
            }
            List<ProcessStepVo> processStepList = new ArrayList<>();
            for (Map.Entry<String, ProcessStepVo> entry : stepMap.entrySet()) {
                ProcessStepVo processStepVo = entry.getValue();
                processStepList.add(processStepVo);
            }
            processStepList.sort(Comparator.comparing(ProcessStepVo::getUuid));
            ProcessStepVo searchVo = new ProcessStepVo();
            searchVo.setProcessUuid(processUuid);
            List<ProcessStepVo> oldProcessStepList = processMapper.searchProcessStep(searchVo);
            oldProcessStepList.sort(Comparator.comparing(ProcessStepVo::getUuid));
            if (!Objects.equals(processStepList.size(), oldProcessStepList.size())) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "流程步骤数量不一致");
                jsonObj.put("newStepSize", processStepList.size());
                jsonObj.put("oldStepSize", oldProcessStepList.size());
                list.add(jsonObj);
            }
            for (int i = processStepList.size() - 1; i >= 0; i--) {
                ProcessStepVo processStepVo = processStepList.get(i);
                for (int j = oldProcessStepList.size() - 1; j >= 0; j--) {
                    ProcessStepVo oldProcessStepVo = oldProcessStepList.get(j);
                    if (Objects.equals(oldProcessStepVo.getUuid(), processStepVo.getUuid())) {
                        String oldProcessStepStr = processStepVoToString(oldProcessStepVo);
                        String processStepStr = processStepVoToString(processStepVo);
                        if (!Objects.equals(oldProcessStepStr, processStepStr)) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("message", "步骤数据不一致");
                            jsonObj.put("newProcessStepVo", processStepVo);
                            jsonObj.put("oldProcessStepVo", oldProcessStepVo);
                            list.add(jsonObj);
                        }
                        oldProcessStepList.remove(j);
                        processStepList.remove(i);
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(processStepList) || CollectionUtils.isNotEmpty(oldProcessStepList)) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "对比后剩下步骤数据");
                jsonObj.put("newProcessStepList", processStepList);
                jsonObj.put("oldProcessStepList", oldProcessStepList);
                list.add(jsonObj);
            }
        }

        JSONArray relList = processObj.getJSONArray("connectionList");
        if (CollectionUtils.isNotEmpty(relList)) {
            List<ProcessStepRelVo> processStepRelList = new ArrayList<>();
            for (int i = 0; i < relList.size(); i++) {
                JSONObject relObj = relList.getJSONObject(i);
                ProcessStepRelVo processStepRelVo = relObj.toJavaObject(ProcessStepRelVo.class);
                if (Objects.equals(processStepRelVo.getFromStepUuid(), virtualStartStepUuid)) {
                    continue;
                }
                processStepRelVo.setProcessUuid(processUuid);
                String type = processStepRelVo.getType();
                if (!ProcessFlowDirection.BACKWARD.getValue().equals(type)) {
                    type = ProcessFlowDirection.FORWARD.getValue();
                }
                processStepRelVo.setType(type);
                processStepRelList.add(processStepRelVo);
            }
            processStepRelList.sort(Comparator.comparing(ProcessStepRelVo::getUuid));
            List<ProcessStepRelVo> oldProcessStepRelList = processMapper.getProcessStepRelByProcessUuid(processUuid);
            for (int j = oldProcessStepRelList.size() - 1; j >= 0; j--) {
                ProcessStepRelVo oldProcessStepRelVo = oldProcessStepRelList.get(j);
                if (Objects.equals(oldProcessStepRelVo.getFromStepUuid(), virtualStartStepUuid)) {
                    oldProcessStepRelList.remove(j);
                }
            }
            oldProcessStepRelList.sort(Comparator.comparing(ProcessStepRelVo::getUuid));
            if (!Objects.equals(processStepRelList.size(), oldProcessStepRelList.size())) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "流程步骤连线数量不一致");
                jsonObj.put("newStepRelSize", processStepRelList.size());
                jsonObj.put("oldStepRelSize", oldProcessStepRelList.size());
                list.add(jsonObj);
            }
            for (int i = processStepRelList.size() - 1; i >= 0; i--) {
                ProcessStepRelVo processStepRelVo = processStepRelList.get(i);
                for (int j = oldProcessStepRelList.size() - 1; j >= 0; j--) {
                    ProcessStepRelVo oldProcessStepRelVo = oldProcessStepRelList.get(j);
                    if (Objects.equals(oldProcessStepRelVo.getUuid(), processStepRelVo.getUuid())) {
                        String oldProcessStepRelStr = processStepRelVoToString(oldProcessStepRelVo);
                        String processStepRelStr = processStepRelVoToString(processStepRelVo);
                        if (!Objects.equals(oldProcessStepRelStr, processStepRelStr)) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("message", "步骤连线数据不一致");
                            jsonObj.put("newProcessStepRelVo", processStepRelVo);
                            jsonObj.put("oldProcessStepRelVo", oldProcessStepRelVo);
                            list.add(jsonObj);
                        }
                        oldProcessStepRelList.remove(j);
                        processStepRelList.remove(i);
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(processStepRelList) || CollectionUtils.isNotEmpty(oldProcessStepRelList)) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "对比后剩下步骤连线数据");
                jsonObj.put("newProcessStepRelList", processStepRelList);
                jsonObj.put("oldProcessStepRelList", oldProcessStepRelList);
                list.add(jsonObj);
            }
        }

        /* 组装评分设置 */
        JSONObject scoreConfig = processObj.getJSONObject("scoreConfig");
        if (MapUtils.isNotEmpty(scoreConfig)) {
            Integer isActive = scoreConfig.getInteger("isActive");
            if (Objects.equals(isActive, 1)) {
                ProcessScoreTemplateVo processScoreTemplateVo = JSON.toJavaObject(scoreConfig, ProcessScoreTemplateVo.class);
                processScoreTemplateVo.setProcessUuid(processUuid);
                ProcessScoreTemplateVo oldProcessScoreTemplateVo = scoreTemplateMapper.getProcessScoreTemplateByProcessUuid(processUuid);
                if (oldProcessScoreTemplateVo == null) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("message", "`process_score_template`表缺少数据");
                    jsonObj.put("newProcessScoreTemplateVo", processScoreTemplateVo);
                    list.add(jsonObj);
                } else {
                    String oldProcessScoreTemplateStr = processScoreTemplateVoToString(oldProcessScoreTemplateVo);
                    String processScoreTemplateStr = processScoreTemplateVoToString(processScoreTemplateVo);
                    if (!Objects.equals(oldProcessScoreTemplateStr, processScoreTemplateStr)) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("message", "评分设置数据不一致");
                        jsonObj.put("newProcessScoreTemplateVo", processScoreTemplateVo);
                        jsonObj.put("oldProcessScoreTemplateVo", oldProcessScoreTemplateVo);
                        list.add(jsonObj);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(list)) {
            resultObj.put("list", list);
        }
        return resultObj;
    }

    private JSONObject checkProcessTaskConfig(Long processTaskId, JSONObject config) {
        JSONObject resultObj = new JSONObject();
        JSONArray list = new JSONArray();
        JSONObject processObj = config.getJSONObject("process");
        if (MapUtils.isEmpty(processObj)) {
            return resultObj;
        }
        JSONObject formConfig = processObj.getJSONObject("formConfig");
        if (MapUtils.isNotEmpty(formConfig)) {
            String formUuid = formConfig.getString("uuid");
            if (StringUtils.isNotBlank(formUuid)) {
                if (formMapper.checkFormIsExists(formUuid) == 0) {
                    throw new FormNotFoundException(formUuid);
                }
                ProcessTaskFormVo oldProcessTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
                if (oldProcessTaskFormVo == null) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("message", "`processtask_form`表缺少数据");
                    jsonObj.put("formUuid", formUuid);
                    list.add(jsonObj);
                } else if (!Objects.equals(oldProcessTaskFormVo.getFormUuid(), formUuid)) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("message", "`processtask_form`表`form_uuid`字段值不相等");
                    jsonObj.put("formUuid", formUuid);
                    jsonObj.put("oldProcessTaskFormVo", oldProcessTaskFormVo);
                    list.add(jsonObj);
                }
            }
        }

        String virtualStartStepUuid = "";// 虚拟开始节点uuid
        Map<String, ProcessStepVo> stepMap = new HashMap<>();
        JSONArray stepList = processObj.getJSONArray("stepList");
        if (CollectionUtils.isNotEmpty(stepList)) {
            for (int i = 0; i < stepList.size(); i++) {
                JSONObject stepObj = stepList.getJSONObject(i);
                String handler = stepObj.getString("handler");
                if (ProcessStepHandlerType.START.getHandler().equals(handler)) {// 找到虚拟开始节点uuid,虚拟开始节点不写入process_step表
                    virtualStartStepUuid = stepObj.getString("uuid");
                    continue;
                }
                ProcessStepVo processStepVo = new ProcessStepVo();
                processStepVo.setConfig(stepObj.getString("stepConfig"));

                String uuid = stepObj.getString("uuid");
                if (StringUtils.isNotBlank(uuid)) {
                    processStepVo.setUuid(uuid);
                }
                String name = stepObj.getString("name");
                if (StringUtils.isNotBlank(name)) {
                    processStepVo.setName(name);
                }

                if (StringUtils.isNotBlank(handler)) {
                    processStepVo.setHandler(handler);
                    String type = ProcessStepHandlerTypeFactory.getType(handler);
                    processStepVo.setType(type);
                    IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(handler);
                    if (processStepUtilHandler != null) {
                    } else {
                        throw new ProcessStepUtilHandlerNotFoundException(handler);
                    }
                }
                stepMap.put(processStepVo.getUuid(), processStepVo);
            }
            JSONArray relList = processObj.getJSONArray("connectionList");
            if (CollectionUtils.isNotEmpty(relList)) {
                for (int i = 0; i < relList.size(); i++) {
                    ProcessStepRelVo processStepRelVo = relList.getObject(i, ProcessStepRelVo.class);
                    String fromStepUuid = processStepRelVo.getFromStepUuid();
                    String toStepUuid = processStepRelVo.getToStepUuid();
                    if (virtualStartStepUuid.equals(fromStepUuid)) {// 通过虚拟开始节点连线找到真正的开始步骤
                        ProcessStepVo startStep = stepMap.get(toStepUuid);
                        if (startStep != null) {
                            startStep.setType(ProcessStepType.START.getValue());
                        }
                        break;
                    }
                }
            }
            List<ProcessStepVo> processStepList = new ArrayList<>();
            for (Map.Entry<String, ProcessStepVo> entry : stepMap.entrySet()) {
                ProcessStepVo processStepVo = entry.getValue();
                processStepList.add(processStepVo);
            }
            processStepList.sort(Comparator.comparing(ProcessStepVo::getUuid));
            List<ProcessStepVo> oldProcessStepList = getProcessStepByProcessTaskId(processTaskId);
            oldProcessStepList.sort(Comparator.comparing(ProcessStepVo::getUuid));
            if (!Objects.equals(processStepList.size(), oldProcessStepList.size())) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "流程步骤数量不一致");
                jsonObj.put("newStepSize", processStepList.size());
                jsonObj.put("oldStepSize", oldProcessStepList.size());
                list.add(jsonObj);
            }
            for (int i = processStepList.size() - 1; i >= 0; i--) {
                ProcessStepVo processStepVo = processStepList.get(i);
                for (int j = oldProcessStepList.size() - 1; j >= 0; j--) {
                    ProcessStepVo oldProcessStepVo = oldProcessStepList.get(j);
                    if (Objects.equals(oldProcessStepVo.getUuid(), processStepVo.getUuid())) {
                        String oldProcessStepStr = processStepVoToString(oldProcessStepVo);
                        String processStepStr = processStepVoToString(processStepVo);
                        if (!Objects.equals(oldProcessStepStr, processStepStr)) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("message", "步骤数据不一致");
                            jsonObj.put("newProcessStepVo", processStepVo);
                            jsonObj.put("oldProcessStepVo", oldProcessStepVo);
                            list.add(jsonObj);
                        }
                        oldProcessStepList.remove(j);
                        processStepList.remove(i);
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(processStepList) || CollectionUtils.isNotEmpty(oldProcessStepList)) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "对比后剩下步骤数据");
                jsonObj.put("newProcessStepList", processStepList);
                jsonObj.put("oldProcessStepList", oldProcessStepList);
                list.add(jsonObj);
            }
        }

        JSONArray relList = processObj.getJSONArray("connectionList");
        if (CollectionUtils.isNotEmpty(relList)) {
            List<ProcessStepRelVo> processStepRelList = new ArrayList<>();
            for (int i = 0; i < relList.size(); i++) {
                JSONObject relObj = relList.getJSONObject(i);
                ProcessStepRelVo processStepRelVo = relObj.toJavaObject(ProcessStepRelVo.class);
                if (Objects.equals(processStepRelVo.getFromStepUuid(), virtualStartStepUuid)) {
                    continue;
                }
                String type = processStepRelVo.getType();
                if (!ProcessFlowDirection.BACKWARD.getValue().equals(type)) {
                    type = ProcessFlowDirection.FORWARD.getValue();
                }
                processStepRelVo.setType(type);
                processStepRelList.add(processStepRelVo);
            }
            processStepRelList.sort(Comparator.comparing(ProcessStepRelVo::getUuid));
            List<ProcessStepRelVo> oldProcessStepRelList = getProcessStepRelListByProcessTaskId(processTaskId);
            for (int j = oldProcessStepRelList.size() - 1; j >= 0; j--) {
                ProcessStepRelVo oldProcessStepRelVo = oldProcessStepRelList.get(j);
                if (Objects.equals(oldProcessStepRelVo.getFromStepUuid(), virtualStartStepUuid)) {
                    oldProcessStepRelList.remove(j);
                }
            }
            oldProcessStepRelList.sort(Comparator.comparing(ProcessStepRelVo::getUuid));
            if (!Objects.equals(processStepRelList.size(), oldProcessStepRelList.size())) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "流程步骤连线数量不一致");
                jsonObj.put("newStepRelSize", processStepRelList.size());
                jsonObj.put("oldStepRelSize", oldProcessStepRelList.size());
                list.add(jsonObj);
            }
            for (int i = processStepRelList.size() - 1; i >= 0; i--) {
                ProcessStepRelVo processStepRelVo = processStepRelList.get(i);
                for (int j = oldProcessStepRelList.size() - 1; j >= 0; j--) {
                    ProcessStepRelVo oldProcessStepRelVo = oldProcessStepRelList.get(j);
                    if (Objects.equals(oldProcessStepRelVo.getUuid(), processStepRelVo.getUuid())) {
                        String oldProcessStepRelStr = processStepRelVoToString(oldProcessStepRelVo);
                        String processStepRelStr = processStepRelVoToString(processStepRelVo);
                        if (!Objects.equals(oldProcessStepRelStr, processStepRelStr)) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("message", "步骤连线数据不一致");
                            jsonObj.put("newProcessStepRelVo", processStepRelVo);
                            jsonObj.put("oldProcessStepRelVo", oldProcessStepRelVo);
                            list.add(jsonObj);
                        }
                        oldProcessStepRelList.remove(j);
                        processStepRelList.remove(i);
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(processStepRelList) || CollectionUtils.isNotEmpty(oldProcessStepRelList)) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("message", "对比后剩下步骤连线数据");
                jsonObj.put("newProcessStepRelList", processStepRelList);
                jsonObj.put("oldProcessStepRelList", oldProcessStepRelList);
                list.add(jsonObj);
            }
        }

        /* 组装评分设置 */
        JSONObject scoreConfig = processObj.getJSONObject("scoreConfig");
        if (MapUtils.isNotEmpty(scoreConfig)) {
            Integer isActive = scoreConfig.getInteger("isActive");
            if (Objects.equals(isActive, 1)) {
                ProcessScoreTemplateVo processScoreTemplateVo = JSON.toJavaObject(scoreConfig, ProcessScoreTemplateVo.class);
                ProcessScoreTemplateVo oldProcessScoreTemplateVo = getProcessScoreTemplateVoByProcessTaskId(processTaskId);
                if (oldProcessScoreTemplateVo == null) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("message", "`processtask_score_template`表缺少数据");
                    jsonObj.put("newProcessScoreTemplateVo", processScoreTemplateVo);
                    list.add(jsonObj);
                } else {
                    String oldProcessScoreTemplateStr = processScoreTemplateVoToString(oldProcessScoreTemplateVo);
                    String processScoreTemplateStr = processScoreTemplateVoToString(processScoreTemplateVo);
                    if (!Objects.equals(oldProcessScoreTemplateStr, processScoreTemplateStr)) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("message", "评分设置数据不一致");
                        jsonObj.put("newProcessScoreTemplateVo", processScoreTemplateVo);
                        jsonObj.put("oldProcessScoreTemplateVo", oldProcessScoreTemplateVo);
                        list.add(jsonObj);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(list)) {
            resultObj.put("list", list);
        }
        return resultObj;
    }

    private ProcessScoreTemplateVo getProcessScoreTemplateVoByProcessTaskId(Long processTaskId) {
        ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskId);
        if (processTaskScoreTemplateVo != null) {
            ProcessScoreTemplateVo processScoreTemplateVo = new ProcessScoreTemplateVo();
            processScoreTemplateVo.setScoreTemplateId(processTaskScoreTemplateVo.getScoreTemplateId());
            processScoreTemplateVo.setIsAuto(processTaskScoreTemplateVo.getIsAuto());
            String config = selectContentByHashMapper.getProcessTaskScoreTempleteConfigStringIsByHash(processTaskScoreTemplateVo.getConfigHash());
            if (StringUtils.isNotBlank(config)) {
                JSONObject configObj = JSONObject.parseObject(config);
                JSONObject newConfigObj = new JSONObject();
                newConfigObj.put("autoTimeType", configObj.getString("autoTimeType"));
                newConfigObj.put("autoTime", configObj.getInteger("autoTime"));
                processScoreTemplateVo.setConfig(newConfigObj.toJSONString());
            }
            return processScoreTemplateVo;
        }
        return null;
    }

    private List<ProcessStepRelVo> getProcessStepRelListByProcessTaskId(Long processTaskId) {
        List<ProcessStepRelVo> processStepRelList = new ArrayList<>();
        List<ProcessTaskStepRelVo> processTaskStepRelList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
        for (ProcessTaskStepRelVo processTaskStepRelVo : processTaskStepRelList) {
            ProcessStepRelVo processStepRelVo = new ProcessStepRelVo();
            processStepRelVo.setUuid(processTaskStepRelVo.getProcessStepRelUuid());
            processStepRelVo.setFromStepUuid(processTaskStepRelVo.getFromProcessStepUuid());
            processStepRelVo.setToStepUuid(processTaskStepRelVo.getToProcessStepUuid());
            processStepRelVo.setName(processTaskStepRelVo.getName());
            processStepRelVo.setType(processTaskStepRelVo.getType());
            processStepRelVo.setCondition(processTaskStepRelVo.getCondition());
            processStepRelList.add(processStepRelVo);
        }
        return processStepRelList;
    }

    private List<ProcessStepVo> getProcessStepByProcessTaskId(Long processTaskId) {
        List<ProcessStepVo> processStepList = new ArrayList<>();
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            ProcessStepVo processStepVo = new ProcessStepVo();
            processStepVo.setUuid(processTaskStepVo.getProcessStepUuid());
            processStepVo.setName(processTaskStepVo.getName());
            processStepVo.setType(processTaskStepVo.getType());
            processStepVo.setHandler(processTaskStepVo.getHandler());
            String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
            processStepVo.setConfig(config);
            processStepList.add(processStepVo);
        }
        return processStepList;
    }

    private String processScoreTemplateVoToString(ProcessScoreTemplateVo processScoreTemplateVo) {
        JSONObject jsonObj = new JSONObject();
        Long scoreTemplateId = processScoreTemplateVo.getScoreTemplateId();
        String config = processScoreTemplateVo.getConfig();
        Integer isAuto = processScoreTemplateVo.getIsAuto();
        jsonObj.put("scoreTemplateId", scoreTemplateId);
        jsonObj.put("config", JSON.parseObject(config));
        jsonObj.put("isAuto", isAuto);
        return JSON.toJSONString(jsonObj, SerializerFeature.MapSortField);
    }

    private String processSlaVoToString(ProcessSlaVo processSlaVo) {
        JSONObject jsonObj = new JSONObject();
        String uuid = processSlaVo.getUuid();
        String name = processSlaVo.getName();
        String config = processSlaVo.getConfig();
        jsonObj.put("uuid", uuid);
        jsonObj.put("name", name);
        jsonObj.put("config", JSON.parseObject(config));
        return JSON.toJSONString(jsonObj, SerializerFeature.MapSortField);
    }

    private String processStepVoToString(ProcessStepVo processStepVo) {
        JSONObject jsonObj = new JSONObject();
        String uuid = processStepVo.getUuid();
        String name = processStepVo.getName();
        String type = processStepVo.getType();
        String handler = processStepVo.getHandler();
        String config = processStepVo.getConfig();
        jsonObj.put("uuid", uuid);
        jsonObj.put("name", name);
        jsonObj.put("type", type);
        jsonObj.put("handler", handler);
        jsonObj.put("config", JSON.parseObject(config));
        return JSON.toJSONString(jsonObj, SerializerFeature.MapSortField);
    }

    private String processStepRelVoToString(ProcessStepRelVo processStepRelVo) {
        JSONObject jsonObj = new JSONObject();
        String uuid = processStepRelVo.getUuid();
        String fromStepUuid = processStepRelVo.getFromStepUuid();
        String toStepUuid = processStepRelVo.getToStepUuid();
        String name = processStepRelVo.getName();
        String type = processStepRelVo.getType();
        String condition = processStepRelVo.getCondition();
        jsonObj.put("uuid", uuid);
        jsonObj.put("fromStepUuid", fromStepUuid);
        jsonObj.put("toStepUuid", toStepUuid);
        jsonObj.put("name", name);
        jsonObj.put("type", type);
        jsonObj.put("condition", condition);
        return JSON.toJSONString(jsonObj, SerializerFeature.MapSortField);
    }
    @Override
    public String getToken() {
        return "process/config/check";
    }
}
