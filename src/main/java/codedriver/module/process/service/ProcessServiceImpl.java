package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
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
import codedriver.framework.process.dto.ProcessDraftVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessSlaVo;
import codedriver.framework.process.dto.ProcessStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessStepRelVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessVo;
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
            processMapper.deleteProcessStepWorkerPolicyByProcessUuid(uuid);
            processMapper.deleteProcessStepByProcessUuid(uuid);
            processMapper.deleteProcessStepRelByProcessUuid(uuid);
            processMapper.deleteProcessStepFormAttributeByProcessUuid(uuid);
            processMapper.deleteProcessFormByProcessUuid(uuid);
            processMapper.deleteProcessSlaByProcessUuid(uuid);
//            notifyPolicyInvokerManager.removeInvoker(uuid);
            DependencyManager.delete(NotifyPolicyProcessDependencyHandler.class, uuid);
            DependencyManager.delete(IntegrationProcessDependencyHandler.class, uuid);
            scoreTemplateMapper.deleteProcessScoreTemplateByProcessUuid(uuid);
            processMapper.updateProcess(processVo);
        } else {
            processVo.setFcu(UserContext.get().getUserUuid(true));
            processMapper.insertProcess(processVo);
        }
        // 删除草稿
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
                    DependencyManager.delete(NotifyPolicyProcessSlaDependencyHandler.class, slaVo.getUuid());
                    for (Long notifyPolicyId : slaVo.getNotifyPolicyIdList()) {
                        if (notifyMapper.checkNotifyPolicyIsExists(notifyPolicyId) == 0) {
                            throw new NotifyPolicyNotFoundException(notifyPolicyId.toString());
//                            NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
//                            notifyPolicyInvokerVo.setPolicyId(notifyPolicyId);
//                            notifyPolicyInvokerVo.setInvoker(processVo.getUuid());
//                            JSONObject notifyPolicyInvokerConfig = new JSONObject();
//                            notifyPolicyInvokerConfig.put("function", "processSla");
//                            notifyPolicyInvokerConfig.put("name",
//                                "流程管理-" + processVo.getName() + "-" + "时效设置" + "-" + slaVo.getName());
//                            notifyPolicyInvokerConfig.put("processUuid", processVo.getUuid());
//                            notifyPolicyInvokerConfig.put("slaUuid", slaVo.getUuid());
//                            notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
//                            notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
                        }
                        DependencyManager.insert(NotifyPolicyProcessSlaDependencyHandler.class, notifyPolicyId, slaVo.getUuid());
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(processVo.getStepList())) {
            for (ProcessStepVo stepVo : processVo.getStepList()) {
                /** 判断引用的外部调用是否存在 **/
                DependencyManager.delete(IntegrationProcessStepDependencyHandler.class, stepVo.getUuid());
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
                DependencyManager.delete(NotifyPolicyProcessStepDependencyHandler.class, stepVo.getUuid());
                if (stepVo.getNotifyPolicyId() != null) {
                    if (notifyMapper.checkNotifyPolicyIsExists(stepVo.getNotifyPolicyId()) == 0) {
                        throw new NotifyPolicyNotFoundException(stepVo.getNotifyPolicyId().toString());
//                        NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
//                        notifyPolicyInvokerVo.setPolicyId(stepVo.getNotifyPolicyId());
//                        notifyPolicyInvokerVo.setInvoker(processVo.getUuid());
//                        JSONObject notifyPolicyInvokerConfig = new JSONObject();
//                        notifyPolicyInvokerConfig.put("function", "processstep");
//                        notifyPolicyInvokerConfig.put("name", "流程管理-" + processVo.getName() + "-" + stepVo.getName());
//                        notifyPolicyInvokerConfig.put("processUuid", processVo.getUuid());
//                        notifyPolicyInvokerConfig.put("processStepUuid", stepVo.getUuid());
//                        notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
//                        notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
                    }
                    DependencyManager.insert(NotifyPolicyProcessStepDependencyHandler.class, stepVo.getNotifyPolicyId(), stepVo.getUuid());
                }
                //保存回复模版配置
                processMapper.deleteProcessStepCommentTemplate(stepVo.getUuid());
                if(stepVo.getCommentTemplateId() != null){
                    processMapper.insertProcessStepCommentTemplate(stepVo);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(processVo.getStepRelList())) {
            for (ProcessStepRelVo stepRelVo : processVo.getStepRelList()) {
                processMapper.insertProcessStepRel(stepRelVo);
            }
        }

        /** 保存评分设置 */
        if (processVo.getProcessScoreTemplateVo() != null) {
            scoreTemplateMapper.insertProcessScoreTemplate(processVo.getProcessScoreTemplateVo());
        }

        if (processVo.getNotifyPolicyId() != null) {
            if (notifyMapper.checkNotifyPolicyIsExists(processVo.getNotifyPolicyId()) == 0) {
                throw new NotifyPolicyNotFoundException(processVo.getNotifyPolicyId().toString());
//                NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
//                notifyPolicyInvokerVo.setPolicyId(processVo.getNotifyPolicyId());
//                notifyPolicyInvokerVo.setInvoker(processVo.getUuid());
//                JSONObject notifyPolicyInvokerConfig = new JSONObject();
//                notifyPolicyInvokerConfig.put("function", "process");
//                notifyPolicyInvokerConfig.put("name", "流程管理-" + processVo.getName());
//                notifyPolicyInvokerConfig.put("processUuid", processVo.getUuid());
//                notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
//                notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
            }
            DependencyManager.insert(NotifyPolicyProcessDependencyHandler.class, processVo.getNotifyPolicyId(), uuid);
        }
        if(CollectionUtils.isNotEmpty(processVo.getIntegrationUuidList())){
            for(String integrationUuid : processVo.getIntegrationUuidList()){
                if(integrationMapper.checkIntegrationExists(integrationUuid) == 0){
                    throw new IntegrationNotFoundException(integrationUuid);
                }
                DependencyManager.insert(IntegrationProcessDependencyHandler.class, integrationUuid, uuid);
            }
        }
        return 1;
    }

}
