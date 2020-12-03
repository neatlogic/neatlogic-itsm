package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.notify.core.NotifyPolicyInvokerManager;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyInvokerVo;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessDraftVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessSlaVo;
import codedriver.framework.process.dto.ProcessStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessStepRelVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;

@Service
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private ProcessMapper processMapper;

    @Autowired
    private FormMapper formMapper;

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private NotifyPolicyInvokerManager notifyPolicyInvokerManager;

    @Autowired
    private IntegrationMapper integrationMapper;

    @Autowired
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public int saveProcess(ProcessVo processVo) {
        if (processMapper.checkProcessNameIsRepeat(processVo) > 0) {
            throw new ProcessNameRepeatException(processVo.getName());
        }
        String uuid = processVo.getUuid();
        if (processMapper.checkProcessIsExists(processVo.getUuid()) > 0) {
            processMapper.deleteProcessStepWorkerPolicyByProcessUuid(uuid);
            processMapper.deleteProcessStepByProcessUuid(uuid);
            processMapper.deleteProcessStepNotifyTemplateByProcessUuid(uuid);
            processMapper.deleteProcessStepRelByProcessUuid(uuid);
            processMapper.deleteProcessStepFormAttributeByProcessUuid(uuid);
            processMapper.deleteProcessFormByProcessUuid(uuid);
            processMapper.deleteProcessSlaByProcessUuid(uuid);
            notifyPolicyInvokerManager.removeInvoker(uuid);
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

        if (processVo.getSlaList() != null && processVo.getSlaList().size() > 0) {
            for (ProcessSlaVo slaVo : processVo.getSlaList()) {
                if (slaVo.getProcessStepUuidList().size() > 0) {
                    processMapper.insertProcessSla(slaVo);
                    for (String stepUuid : slaVo.getProcessStepUuidList()) {
                        processMapper.insertProcessStepSla(stepUuid, slaVo.getUuid());
                    }
                    for (Long notifyPolicyId : slaVo.getNotifyPolicyIdList()) {
                        if (notifyMapper.checkNotifyPolicyIsExists(notifyPolicyId) != 0) {
                            NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
                            notifyPolicyInvokerVo.setPolicyId(notifyPolicyId);
                            notifyPolicyInvokerVo.setInvoker(processVo.getUuid());
                            JSONObject notifyPolicyInvokerConfig = new JSONObject();
                            notifyPolicyInvokerConfig.put("function", "processSla");
                            notifyPolicyInvokerConfig.put("name",
                                "流程管理-" + processVo.getName() + "-" + "时效设置" + "-" + slaVo.getName());
                            notifyPolicyInvokerConfig.put("processUuid", processVo.getUuid());
                            notifyPolicyInvokerConfig.put("slaUuid", slaVo.getUuid());
                            notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
                            notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
                        }
                    }
                }
            }
        }

        if (processVo.getStepList() != null && processVo.getStepList().size() > 0) {

            for (ProcessStepVo stepVo : processVo.getStepList()) {
                /** 判断引用的外部调用是否存在 **/
                List<String> integrationUuidList = stepVo.getIntegrationUuidList();
                if (CollectionUtils.isNotEmpty(integrationUuidList)) {
                    for (String integrationUuid : integrationUuidList) {
                        if (integrationMapper.getIntegrationByUuid(integrationUuid) == null) {
                            throw new IntegrationNotFoundException(integrationUuid);
                        }
                    }
                }
                processMapper.insertProcessStep(stepVo);
                if (stepVo.getFormAttributeList() != null && stepVo.getFormAttributeList().size() > 0) {
                    for (ProcessStepFormAttributeVo processStepAttributeVo : stepVo.getFormAttributeList()) {
                        processMapper.insertProcessStepFormAttribute(processStepAttributeVo);
                    }
                }
                if (stepVo.getWorkerPolicyList() != null && stepVo.getWorkerPolicyList().size() > 0) {
                    for (ProcessStepWorkerPolicyVo processStepWorkerPolicyVo : stepVo.getWorkerPolicyList()) {
                        processMapper.insertProcessStepWorkerPolicy(processStepWorkerPolicyVo);
                    }
                }

                if (stepVo.getNotifyPolicyId() != null) {
                    if (notifyMapper.checkNotifyPolicyIsExists(stepVo.getNotifyPolicyId()) != 0) {
                        NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
                        notifyPolicyInvokerVo.setPolicyId(stepVo.getNotifyPolicyId());
                        notifyPolicyInvokerVo.setInvoker(processVo.getUuid());
                        JSONObject notifyPolicyInvokerConfig = new JSONObject();
                        notifyPolicyInvokerConfig.put("function", "processstep");
                        notifyPolicyInvokerConfig.put("name", "流程管理-" + processVo.getName() + "-" + stepVo.getName());
                        notifyPolicyInvokerConfig.put("processUuid", processVo.getUuid());
                        notifyPolicyInvokerConfig.put("processStepUuid", stepVo.getUuid());
                        notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
                        notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
                    }
                }
            }
        }

        if (processVo.getStepRelList() != null && processVo.getStepRelList().size() > 0) {
            for (ProcessStepRelVo stepRelVo : processVo.getStepRelList()) {
                processMapper.insertProcessStepRel(stepRelVo);
            }
        }

        /** 保存评分设置 */
        if (processVo.getProcessScoreTemplateVo() != null) {
            scoreTemplateMapper.insertProcessScoreTemplate(processVo.getProcessScoreTemplateVo());
        }

        if (processVo.getNotifyPolicyId() != null) {
            if (notifyMapper.checkNotifyPolicyIsExists(processVo.getNotifyPolicyId()) != 0) {
                NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
                notifyPolicyInvokerVo.setPolicyId(processVo.getNotifyPolicyId());
                notifyPolicyInvokerVo.setInvoker(processVo.getUuid());
                JSONObject notifyPolicyInvokerConfig = new JSONObject();
                notifyPolicyInvokerConfig.put("function", "process");
                notifyPolicyInvokerConfig.put("name", "流程管理-" + processVo.getName());
                notifyPolicyInvokerConfig.put("processUuid", processVo.getUuid());
                notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
                notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
            }
        }
        return 1;
    }

}
