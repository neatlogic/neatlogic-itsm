/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.process.dao.mapper.ProcessTagMapper;
import neatlogic.framework.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.exception.process.ProcessNameRepeatException;
import neatlogic.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import neatlogic.framework.process.sla.core.ISlaCalculateHandler;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerFactory;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import neatlogic.module.process.dependency.handler.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class ProcessServiceImpl implements ProcessService {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTagMapper processTagMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private NotifyMapper notifyMapper;

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
        if (processMapper.checkProcessIsExists(uuid) > 0) {
            List<String> slaUuidList = processMapper.getSlaUuidListByProcessUuid(uuid);
            List<String> processStepUuidList = processMapper.getProcessStepUuidListByProcessUuid(uuid);
            DependencyManager.delete(NotifyPolicyProcessSlaDependencyHandler.class, slaUuidList);
            DependencyManager.delete(NotifyPolicyProcessStepDependencyHandler.class, processStepUuidList);
            DependencyManager.delete(IntegrationProcessStepDependencyHandler.class, processStepUuidList);
            DependencyManager.delete(NotifyPolicyProcessDependencyHandler.class, uuid);
            DependencyManager.delete(IntegrationProcessDependencyHandler.class, uuid);
            processMapper.deleteProcessStepWorkerPolicyByProcessUuid(uuid);
            processMapper.deleteProcessStepByProcessUuid(uuid);
            processMapper.deleteProcessStepRelByProcessUuid(uuid);
//            processMapper.deleteProcessStepFormAttributeByProcessUuid(uuid);
            processMapper.deleteProcessFormByProcessUuid(uuid);
            processMapper.deleteProcessSlaByProcessUuid(uuid);
            scoreTemplateMapper.deleteProcessScoreTemplateByProcessUuid(uuid);
            processMapper.deleteProcessStepTagByProcessUuid(uuid);
            processMapper.updateProcess(processVo);
            if (CollectionUtils.isNotEmpty(processVo.getStepList())) {
                for (ProcessStepVo stepVo : processVo.getStepList()) {
                    DependencyManager.delete(FormScene2ProcessStepDependencyHandler.class, stepVo.getUuid());
                }
            }
        } else {
            processVo.setFcu(UserContext.get().getUserUuid(true));
            processMapper.insertProcess(processVo);
        }

        /* 清空自己的草稿 **/
        ProcessDraftVo processDraftVo = new ProcessDraftVo();
        processDraftVo.setProcessUuid(uuid);
        processDraftVo.setFcu(UserContext.get().getUserUuid(true));
        processMapper.deleteProcessDraft(processDraftVo);

        String formUuid = processVo.getFormUuid();
        if (StringUtils.isNotBlank(formUuid)) {
            if (formMapper.checkFormIsExists(formUuid) == 0) {
                throw new FormNotFoundException(formUuid);
            }
            processMapper.insertProcessForm(new ProcessFormVo(uuid, formUuid));
        }

        if (CollectionUtils.isNotEmpty(processVo.getSlaList())) {
            for (ProcessSlaVo slaVo : processVo.getSlaList()) {
                ISlaCalculateHandler slaCalculateHandler = SlaCalculateHandlerFactory.getHandler(slaVo.getCalculateHandler());
                if (slaCalculateHandler == null) {
                    throw new SlaCalculateHandlerNotFoundException(slaVo.getCalculateHandler());
                }
                if (CollectionUtils.isEmpty(slaVo.getProcessStepUuidList())) {
                    continue;
                }
                if (Objects.equals(slaCalculateHandler.isSum(), 1)) {
                    //关联的多个步骤共用一个时效
                    processMapper.insertProcessSla(slaVo);
                    for (String stepUuid : slaVo.getProcessStepUuidList()) {
                        processMapper.insertProcessStepSla(stepUuid, slaVo.getUuid());
                    }
                    for (InvokeNotifyPolicyConfigVo notifyPolicyConfig : slaVo.getNotifyPolicyConfigList()) {
                        if (notifyPolicyConfig.getPolicyId() == null) {
                            continue;
                        }
                        if (notifyMapper.checkNotifyPolicyIsExists(notifyPolicyConfig.getPolicyId()) == 0) {
                            if (StringUtils.isNotBlank(notifyPolicyConfig.getPolicyPath())) {
                                throw new NotifyPolicyNotFoundException(notifyPolicyConfig.getPolicyPath());
                            } else {
                                throw new NotifyPolicyNotFoundException(notifyPolicyConfig.getPolicyId());
                            }
                        }
                        DependencyManager.insert(NotifyPolicyProcessSlaDependencyHandler.class, notifyPolicyConfig.getPolicyId(), slaVo.getUuid());
                    }
                } else {
                    //关联的多个步骤各用一个时效
                    for (String stepUuid : slaVo.getProcessStepUuidList()) {
                        slaVo.setUuid(UuidUtil.randomUuid());
                        processMapper.insertProcessSla(slaVo);
                        processMapper.insertProcessStepSla(stepUuid, slaVo.getUuid());
                        for (InvokeNotifyPolicyConfigVo notifyPolicyConfig : slaVo.getNotifyPolicyConfigList()) {
                            if (notifyPolicyConfig.getPolicyId() == null) {
                                continue;
                            }
                            if (notifyMapper.checkNotifyPolicyIsExists(notifyPolicyConfig.getPolicyId()) == 0) {
                                if (StringUtils.isNotBlank(notifyPolicyConfig.getPolicyPath())) {
                                    throw new NotifyPolicyNotFoundException(notifyPolicyConfig.getPolicyPath());
                                } else {
                                    throw new NotifyPolicyNotFoundException(notifyPolicyConfig.getPolicyId());
                                }
                            }
                            DependencyManager.insert(NotifyPolicyProcessSlaDependencyHandler.class, notifyPolicyConfig.getPolicyId(), slaVo.getUuid());
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(processVo.getStepList())) {
            for (ProcessStepVo stepVo : processVo.getStepList()) {
                List<String> integrationUuidList = stepVo.getIntegrationUuidList();
                if (CollectionUtils.isNotEmpty(integrationUuidList)) {
                    for (String integrationUuid : integrationUuidList) {
                        if (integrationMapper.checkIntegrationExists(integrationUuid) == 0) {
                            throw new IntegrationNotFoundException(integrationUuid);
                        }
                        DependencyManager.insert(IntegrationProcessStepDependencyHandler.class, integrationUuid, stepVo.getUuid());
                    }
                }
                processMapper.insertProcessStep(stepVo);
//                if (CollectionUtils.isNotEmpty(stepVo.getFormAttributeList())) {
//                    for (ProcessStepFormAttributeVo processStepAttributeVo : stepVo.getFormAttributeList()) {
//                        processMapper.insertProcessStepFormAttribute(processStepAttributeVo);
//                    }
//                }
                if (StringUtils.isNotBlank(stepVo.getFormSceneUuid())) {
                    JSONObject config = new JSONObject();
                    config.put("processUuid", uuid);
                    config.put("processName", processVo.getName());
                    config.put("stepUuid", stepVo.getUuid());
                    config.put("stepName", stepVo.getName());
                    DependencyManager.insert(FormScene2ProcessStepDependencyHandler.class, stepVo.getFormSceneUuid(), stepVo.getUuid(), config);
                }
                if (CollectionUtils.isNotEmpty(stepVo.getWorkerPolicyList())) {
                    for (ProcessStepWorkerPolicyVo processStepWorkerPolicyVo : stepVo.getWorkerPolicyList()) {
                        processMapper.insertProcessStepWorkerPolicy(processStepWorkerPolicyVo);
                    }
                }
                InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = stepVo.getNotifyPolicyConfig();
                if (invokeNotifyPolicyConfigVo != null && invokeNotifyPolicyConfigVo.getPolicyId() != null) {
                    if (notifyMapper.checkNotifyPolicyIsExists(invokeNotifyPolicyConfigVo.getPolicyId()) == 0) {
                        if (StringUtils.isNotBlank(invokeNotifyPolicyConfigVo.getPolicyPath())) {
                            throw new NotifyPolicyNotFoundException(invokeNotifyPolicyConfigVo.getPolicyPath());
                        } else {
                            throw new NotifyPolicyNotFoundException(invokeNotifyPolicyConfigVo.getPolicyId());
                        }
                    }
                    DependencyManager.insert(NotifyPolicyProcessStepDependencyHandler.class, invokeNotifyPolicyConfigVo.getPolicyId(), stepVo.getUuid());
                }
                processMapper.deleteProcessStepCommentTemplate(stepVo.getUuid());
                if (stepVo.getCommentTemplateId() != null) {
                    processMapper.insertProcessStepCommentTemplate(stepVo);
                }
                List<String> tagNameList = stepVo.getTagList();
                if (CollectionUtils.isNotEmpty(tagNameList)) {
                    ProcessStepTagVo processStepTagVo = new ProcessStepTagVo();
                    processStepTagVo.setProcessUuid(stepVo.getProcessUuid());
                    processStepTagVo.setProcessStepUuid(stepVo.getUuid());
                    List<ProcessTagVo> processTagList = processTagMapper.getProcessTagByNameList(tagNameList);
                    for (ProcessTagVo processTagVo : processTagList) {
                        processStepTagVo.setTagId(processTagVo.getId());
                        tagNameList.remove(processTagVo.getName());
                        processMapper.insertProcessStepTag(processStepTagVo);
                    }
                    if (CollectionUtils.isNotEmpty(tagNameList)) {
                        for (String tagName : tagNameList) {
                            ProcessTagVo processTagVo = new ProcessTagVo(tagName);
                            processTagMapper.insertProcessTag(processTagVo);
                            processStepTagVo.setTagId(processTagVo.getId());
                            processMapper.insertProcessStepTag(processStepTagVo);
                        }
                    }
                }

                //子任务
                ProcessStepTaskConfigVo taskConfigVo = stepVo.getTaskConfigVo();
                processMapper.deleteProcessStepTaskByProcessStepUuid(stepVo.getUuid());
                if (taskConfigVo != null) {
                    if (CollectionUtils.isNotEmpty(taskConfigVo.getIdList())) {
                        taskConfigVo.getIdList().forEach(id -> {
                            ProcessStepTaskConfigVo tmpVo = new ProcessStepTaskConfigVo(stepVo.getUuid(), id);
                            processMapper.insertProcessStepTask(tmpVo);
                        });
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(processVo.getStepRelList())) {
            for (ProcessStepRelVo stepRelVo : processVo.getStepRelList()) {
                processMapper.insertProcessStepRel(stepRelVo);
            }
        }

        if (processVo.getProcessScoreTemplateVo() != null) {
            scoreTemplateMapper.insertProcessScoreTemplate(processVo.getProcessScoreTemplateVo());
        }

        InvokeNotifyPolicyConfigVo notifyPolicyConfig = processVo.getNotifyPolicyConfig();
        if (notifyPolicyConfig != null && notifyPolicyConfig.getPolicyId() != null) {
            if (notifyMapper.checkNotifyPolicyIsExists(notifyPolicyConfig.getPolicyId()) == 0) {
                if (StringUtils.isNotBlank(notifyPolicyConfig.getPolicyPath())) {
                    throw new NotifyPolicyNotFoundException(notifyPolicyConfig.getPolicyPath());
                } else {
                    throw new NotifyPolicyNotFoundException(notifyPolicyConfig.getPolicyId());
                }
            }
            DependencyManager.insert(NotifyPolicyProcessDependencyHandler.class, notifyPolicyConfig.getPolicyId(), uuid);
        }
        if (CollectionUtils.isNotEmpty(processVo.getIntegrationUuidList())) {
            for (String integrationUuid : processVo.getIntegrationUuidList()) {
                if (integrationMapper.checkIntegrationExists(integrationUuid) == 0) {
                    throw new IntegrationNotFoundException(integrationUuid);
                }
                DependencyManager.insert(IntegrationProcessDependencyHandler.class, integrationUuid, uuid);
            }
        }
        return 1;
    }

}
