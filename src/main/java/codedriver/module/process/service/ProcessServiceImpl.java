package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.*;
import codedriver.module.process.dependency.handler.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;

import javax.annotation.Resource;

@Service
public class ProcessServiceImpl implements ProcessService {

    @Resource
    private ProcessMapper processMapper;

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
            processMapper.updateProcess(processVo);
        } else {
            processVo.setFcu(UserContext.get().getUserUuid(true));
            processMapper.insertProcess(processVo);
        }

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
                if (slaVo.getProcessStepUuidList().size() > 0) {
                    processMapper.insertProcessSla(slaVo);
                    for (String stepUuid : slaVo.getProcessStepUuidList()) {
                        processMapper.insertProcessStepSla(stepUuid, slaVo.getUuid());
                    }
                    for (Long notifyPolicyId : slaVo.getNotifyPolicyIdList()) {
                        if (notifyMapper.checkNotifyPolicyIsExists(notifyPolicyId) == 0) {
                            throw new NotifyPolicyNotFoundException(notifyPolicyId.toString());
                        }
                        DependencyManager.insert(NotifyPolicyProcessSlaDependencyHandler.class, notifyPolicyId, slaVo.getUuid());
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
                if (stepVo.getNotifyPolicyId() != null) {
                    if (notifyMapper.checkNotifyPolicyIsExists(stepVo.getNotifyPolicyId()) == 0) {
                        throw new NotifyPolicyNotFoundException(stepVo.getNotifyPolicyId().toString());
                    }
                    DependencyManager.insert(NotifyPolicyProcessStepDependencyHandler.class, stepVo.getNotifyPolicyId(), stepVo.getUuid());
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
                    List<ProcessTagVo> processTagList = processMapper.getProcessTagByNameList(tagNameList);
                    for (ProcessTagVo processTagVo : processTagList) {
                        processStepTagVo.setTagId(processTagVo.getId());
                        tagNameList.remove(processTagVo.getName());
                        processMapper.insertProcessStepTag(processStepTagVo);
                    }
                    if (CollectionUtils.isNotEmpty(tagNameList)) {
                        for (String tagName : tagNameList) {
                            ProcessTagVo processTagVo = new ProcessTagVo(tagName);
                            processMapper.insertProcessTag(processTagVo);
                            processStepTagVo.setTagId(processTagVo.getId());
                            processMapper.insertProcessStepTag(processStepTagVo);
                        }
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

        if (processVo.getNotifyPolicyId() != null) {
            if (notifyMapper.checkNotifyPolicyIsExists(processVo.getNotifyPolicyId()) == 0) {
                throw new NotifyPolicyNotFoundException(processVo.getNotifyPolicyId().toString());
            }
            DependencyManager.insert(NotifyPolicyProcessDependencyHandler.class, processVo.getNotifyPolicyId(), uuid);
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
