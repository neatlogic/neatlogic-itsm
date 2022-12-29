/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.process.dao.mapper.ProcessTagMapper;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.dto.processconfig.NotifyPolicyConfigVo;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;
import codedriver.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import codedriver.framework.process.sla.core.ISlaCalculateHandler;
import codedriver.framework.process.sla.core.SlaCalculateHandlerFactory;
import codedriver.framework.util.UuidUtil;
import codedriver.module.process.dao.mapper.ProcessMapper;
import codedriver.module.process.dependency.handler.*;
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
            processMapper.deleteProcessStepFormAttributeByProcessUuid(uuid);
            processMapper.deleteProcessFormByProcessUuid(uuid);
            processMapper.deleteProcessSlaByProcessUuid(uuid);
            scoreTemplateMapper.deleteProcessScoreTemplateByProcessUuid(uuid);
            processMapper.deleteProcessStepTagByProcessUuid(uuid);
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
                    for (NotifyPolicyConfigVo notifyPolicyConfig : slaVo.getNotifyPolicyConfigList()) {
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
                        for (NotifyPolicyConfigVo notifyPolicyConfig : slaVo.getNotifyPolicyConfigList()) {
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
                if (CollectionUtils.isNotEmpty(stepVo.getFormAttributeList())) {
                    for (ProcessStepFormAttributeVo processStepAttributeVo : stepVo.getFormAttributeList()) {
                        processMapper.insertProcessStepFormAttribute(processStepAttributeVo);
                    }
                }
                if (CollectionUtils.isNotEmpty(stepVo.getWorkerPolicyList())) {
                    for (ProcessStepWorkerPolicyVo processStepWorkerPolicyVo : stepVo.getWorkerPolicyList()) {
                        processMapper.insertProcessStepWorkerPolicy(processStepWorkerPolicyVo);
                    }
                }
                NotifyPolicyConfigVo notifyPolicyConfigVo = stepVo.getNotifyPolicyConfig();
                if (notifyPolicyConfigVo != null && notifyPolicyConfigVo.getPolicyId() != null) {
                    if (notifyMapper.checkNotifyPolicyIsExists(notifyPolicyConfigVo.getPolicyId()) == 0) {
                        if (StringUtils.isNotBlank(notifyPolicyConfigVo.getPolicyPath())) {
                            throw new NotifyPolicyNotFoundException(notifyPolicyConfigVo.getPolicyPath());
                        } else {
                            throw new NotifyPolicyNotFoundException(notifyPolicyConfigVo.getPolicyId());
                        }
                    }
                    DependencyManager.insert(NotifyPolicyProcessStepDependencyHandler.class, notifyPolicyConfigVo.getPolicyId(), stepVo.getUuid());
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

        NotifyPolicyConfigVo notifyPolicyConfig = processVo.getNotifyPolicyConfig();
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
