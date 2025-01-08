/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessStepType;
import neatlogic.framework.process.crossover.IProcessCrossoverService;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.dto.score.ProcessScoreTemplateVo;
import neatlogic.framework.process.exception.process.ProcessNameRepeatException;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import neatlogic.framework.process.sla.core.ISlaCalculateHandler;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerFactory;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerTypeFactory;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.module.process.dependency.handler.IntegrationProcessDependencyHandler;
import neatlogic.module.process.dependency.handler.NotifyPolicyProcessDependencyHandler;
import neatlogic.module.process.dependency.handler.NotifyPolicyProcessSlaDependencyHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ProcessServiceImpl implements ProcessService, IProcessCrossoverService {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Resource
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public int saveProcess(ProcessVo processVo) throws ProcessNameRepeatException {
        if (processMapper.checkProcessNameIsRepeat(processVo) > 0) {
            throw new ProcessNameRepeatException(processVo.getName());
        }
        String uuid = processVo.getUuid();
        JSONObject config = processVo.getConfig();
        if (MapUtils.isNotEmpty(config)) {
            JSONObject processObj = config.getJSONObject("process");
            if (MapUtils.isNotEmpty(processObj)) {
                JSONObject processConfig = processObj.getJSONObject("processConfig");
                if (MapUtils.isNotEmpty(processConfig)) {
                    processConfig.put("uuid", uuid);
                    processConfig.put("name", processVo.getName());
                }
            }
        }
        ProcessVo oldProcessVo = processMapper.getProcessByUuid(uuid);
        if (oldProcessVo != null) {
            saveOrDeleteProcessDependency(oldProcessVo, "delete");
        }
        saveOrDeleteProcessDependency(processVo, "save");
        if (oldProcessVo != null) {
            processMapper.updateProcess(processVo);
        } else {
            processVo.setFcu(UserContext.get().getUserUuid(true));
            processMapper.insertProcess(processVo);
        }
        /* 清空自己的草稿 **/
        ProcessDraftVo processDraftVo = new ProcessDraftVo();
        processDraftVo.setProcessUuid(uuid);
        processDraftVo.setFcu(UserContext.get().getUserUuid(true));
        processMapper.deleteProcessDraft(processDraftVo);
        return 1;
    }

    @Override
    public void saveOrDeleteProcessDependency(ProcessVo processVo, String action) {
        JSONObject config = processVo.getConfig();
        if (MapUtils.isEmpty(config)) {
            return;
        }
        JSONObject processObj = config.getJSONObject("process");
        if (MapUtils.isEmpty(processObj)) {
            return;
        }

        JSONObject formConfig = processObj.getJSONObject("formConfig");
        if (MapUtils.isNotEmpty(formConfig)) {
            String formUuid = formConfig.getString("uuid");
            if (StringUtils.isNotBlank(formUuid)) {
                if (Objects.equals(action, "save")) {
                    if (formMapper.checkFormIsExists(formUuid) == 0) {
                        throw new FormNotFoundException(formUuid);
                    }
                    processMapper.insertProcessForm(new ProcessFormVo(processVo.getUuid(), formUuid));
                } else if (Objects.equals(action, "delete")) {
                    processMapper.deleteProcessFormByProcessUuid(processVo.getUuid());
                }
            }
        }
        JSONArray slaList = processObj.getJSONArray("slaList");
        if (CollectionUtils.isNotEmpty(slaList)) {
            if (Objects.equals(action, "save")) {
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
                        List<String> slaUuidList = new ArrayList<>();
                        ProcessSlaVo processSlaVo = new ProcessSlaVo();
                        processSlaVo.setProcessUuid(processVo.getUuid());
                        processSlaVo.setName(slaObj.getString("name"));
                        processSlaVo.setCalculateHandler(calculateHandler);
                        processSlaVo.setConfig(slaObj.toJSONString());

                        if (Objects.equals(slaCalculateHandler.isSum(), 1)) {
                            //关联的多个步骤共用一个时效
                            processSlaVo.setUuid(slaObj.getString("uuid"));
                            processMapper.insertProcessSla(processSlaVo);
                            for (int p = 0; p < processStepUuidList.size(); p++) {
                                String stepUuid = processStepUuidList.getString(p);
                                processMapper.insertProcessStepSla(stepUuid, processSlaVo.getUuid());
                            }
                            slaUuidList.add(processSlaVo.getUuid());
                        } else {
                            //关联的多个步骤各用一个时效
                            for (int p = 0; p < processStepUuidList.size(); p++) {
                                String stepUuid = processStepUuidList.getString(p);
                                processSlaVo.setUuid(UuidUtil.randomUuid());
                                processMapper.insertProcessSla(processSlaVo);
                                processMapper.insertProcessStepSla(stepUuid, processSlaVo.getUuid());
                                slaUuidList.add(processSlaVo.getUuid());
                            }
                        }

                        JSONArray notifyPolicyList = slaObj.getJSONArray("notifyPolicyList");
                        if (CollectionUtils.isNotEmpty(notifyPolicyList)) {
                            INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
                            for (int j = 0; j < notifyPolicyList.size(); j++) {
                                JSONObject notifyPolicy = notifyPolicyList.getJSONObject(j);
                                if (MapUtils.isNotEmpty(notifyPolicy)) {
                                    InvokeNotifyPolicyConfigVo notifyPolicyConfig = notifyPolicy.getObject("notifyPolicyConfig", InvokeNotifyPolicyConfigVo.class);
                                    if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(notifyPolicyConfig)) {
                                        for (String slaUuid : slaUuidList) {
                                            DependencyManager.insert(NotifyPolicyProcessSlaDependencyHandler.class, notifyPolicyConfig.getPolicyId(), slaUuid);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (Objects.equals(action, "delete")) {
                List<String> slaUuidList = processMapper.getSlaUuidListByProcessUuid(processVo.getUuid());
                DependencyManager.delete(NotifyPolicyProcessSlaDependencyHandler.class, slaUuidList);
                processMapper.deleteProcessSlaByProcessUuid(processVo.getUuid());
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
                processStepVo.setProcessUuid(processVo.getUuid());
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
                        JSONObject stepConfigObj = stepObj.getJSONObject("stepConfig");
                        if (stepConfigObj != null) {
                            processStepVo.setProcessUuid(processVo.getUuid());
                            processStepUtilHandler.makeupProcessStep(processStepVo, stepConfigObj, action);
                        }
                    } else {
                        throw new ProcessStepHandlerNotFoundException(handler);
                    }
                }
                stepMap.put(processStepVo.getUuid(), processStepVo);
            }
            if (Objects.equals(action, "save")) {
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
                for (Map.Entry<String, ProcessStepVo> entry : stepMap.entrySet()) {
                    ProcessStepVo processStepVo = entry.getValue();
                    processMapper.insertProcessStep(processStepVo);
                }
            }
            if (Objects.equals(action, "delete")) {
                processMapper.deleteProcessStepByProcessUuid(processVo.getUuid());
            }
        }

        JSONArray relList = processObj.getJSONArray("connectionList");
        if (CollectionUtils.isNotEmpty(relList)) {
            if (Objects.equals(action, "save")) {
                for (int i = 0; i < relList.size(); i++) {
                    JSONObject relObj = relList.getJSONObject(i);
                    String uuid = relObj.getString("uuid");
                    if (processMapper.getProcessStepRelByUuid(uuid) != null) {
                        relObj.put("uuid", UuidUtil.randomUuid());
                    }
                    ProcessStepRelVo processStepRelVo = relObj.toJavaObject(ProcessStepRelVo.class);
                    processStepRelVo.setProcessUuid(processVo.getUuid());
                    String type = processStepRelVo.getType();
                    if (!ProcessFlowDirection.BACKWARD.getValue().equals(type)) {
                        type = ProcessFlowDirection.FORWARD.getValue();
                    }
                    processStepRelVo.setType(type);
                    processMapper.insertProcessStepRel(processStepRelVo);
                }
            } else if (Objects.equals(action, "delete")) {
                processMapper.deleteProcessStepRelByProcessUuid(processVo.getUuid());
            }
        }

        /* 组装评分设置 */
        JSONObject scoreConfig = processObj.getJSONObject("scoreConfig");
        if (MapUtils.isNotEmpty(scoreConfig)) {
            Integer isActive = scoreConfig.getInteger("isActive");
            if (Objects.equals(isActive, 1)) {
                if (Objects.equals(action, "save")) {
                    ProcessScoreTemplateVo processScoreTemplateVo = JSON.toJavaObject(scoreConfig, ProcessScoreTemplateVo.class);
                    processScoreTemplateVo.setProcessUuid(processVo.getUuid());
                    scoreTemplateMapper.insertProcessScoreTemplate(processScoreTemplateVo);
                } else if (Objects.equals(action, "delete")) {
                    scoreTemplateMapper.deleteProcessScoreTemplateByProcessUuid(processVo.getUuid());
                }

            }
        }

        /* 组装通知策略 **/
        JSONObject processConfig = processObj.getJSONObject("processConfig");
        if (MapUtils.isNotEmpty(processConfig)) {
            InvokeNotifyPolicyConfigVo notifyPolicyConfig = processConfig.getObject("notifyPolicyConfig", InvokeNotifyPolicyConfigVo.class);
            if (notifyPolicyConfig != null) {
                INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
                if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(notifyPolicyConfig)) {
                    if (Objects.equals(action, "save")) {
                        DependencyManager.insert(NotifyPolicyProcessDependencyHandler.class, notifyPolicyConfig.getPolicyId(), processVo.getUuid());
                    } else if (Objects.equals(action, "delete")) {
                        DependencyManager.delete(NotifyPolicyProcessDependencyHandler.class, processVo.getUuid());
                    }
                }
            }

            JSONObject actionConfig = processConfig.getJSONObject("actionConfig");
            if (MapUtils.isNotEmpty(actionConfig)) {
                JSONArray actionList = actionConfig.getJSONArray("actionList");
                if (CollectionUtils.isNotEmpty(actionList)) {
                    if (Objects.equals(action, "save")) {
                        for (int i = 0; i < actionList.size(); i++) {
                            JSONObject ationObj = actionList.getJSONObject(i);
                            String integrationUuid = ationObj.getString("integrationUuid");
                            if (StringUtils.isNotBlank(integrationUuid)) {
                                if (integrationMapper.checkIntegrationExists(integrationUuid) == 0) {
                                    throw new IntegrationNotFoundException(integrationUuid);
                                }
                                DependencyManager.insert(IntegrationProcessDependencyHandler.class, integrationUuid, processVo.getUuid());
                            }
                        }
                    } else if (Objects.equals(action, "delete")) {
                        DependencyManager.delete(IntegrationProcessDependencyHandler.class, processVo.getUuid());
                    }
                }
            }
        }
    }

}
