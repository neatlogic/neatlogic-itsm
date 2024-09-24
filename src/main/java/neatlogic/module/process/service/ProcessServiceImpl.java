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
import neatlogic.framework.process.crossover.IProcessCrossoverService;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.process.ProcessNameRepeatException;
import neatlogic.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import neatlogic.framework.process.sla.core.ISlaCalculateHandler;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerFactory;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import neatlogic.module.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.module.process.dependency.handler.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class ProcessServiceImpl implements ProcessService, IProcessCrossoverService {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTagMapper processTagMapper;

    @Resource
    private FormMapper formMapper;

    //@Resource
    //private NotifyMapper notifyMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Resource
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public int saveProcess(ProcessVo processVo) throws ProcessNameRepeatException {
        INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
        if (processMapper.checkProcessNameIsRepeat(processVo) > 0) {
            throw new ProcessNameRepeatException(processVo.getName());
        }
        String uuid = processVo.getUuid();
        if (processMapper.checkProcessIsExists(uuid) > 0) {
            deleteProcessRelevantData(uuid);
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
                    for (InvokeNotifyPolicyConfigVo notifyPolicyConfig : slaVo.getNotifyPolicyConfigList()) {
                        if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(notifyPolicyConfig)) {
                            DependencyManager.insert(NotifyPolicyProcessSlaDependencyHandler.class, notifyPolicyConfig.getPolicyId(), slaVo.getUuid());
                        }
                    }
                } else {
                    //关联的多个步骤各用一个时效
                    for (String stepUuid : slaVo.getProcessStepUuidList()) {
                        slaVo.setUuid(UuidUtil.randomUuid());
                        processMapper.insertProcessSla(slaVo);
                        processMapper.insertProcessStepSla(stepUuid, slaVo.getUuid());
                        for (InvokeNotifyPolicyConfigVo notifyPolicyConfig : slaVo.getNotifyPolicyConfigList()) {
                            if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(notifyPolicyConfig)) {
                                DependencyManager.insert(NotifyPolicyProcessSlaDependencyHandler.class, notifyPolicyConfig.getPolicyId(), slaVo.getUuid());
                            }
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
                if (CollectionUtils.isNotEmpty(stepVo.getEoaTemplateIdList())) {
                    for (Long eoaTemplateId : stepVo.getEoaTemplateIdList()) {
                        JSONObject config = new JSONObject();
                        config.put("processUuid", uuid);
                        config.put("processName", processVo.getName());
                        config.put("stepUuid", stepVo.getUuid());
                        config.put("stepName", stepVo.getName());
                        DependencyManager.insert(EoaTemplate2ProcessStepDependencyHandler.class, eoaTemplateId, stepVo.getUuid(), config);
                    }
                }
                if (CollectionUtils.isNotEmpty(stepVo.getWorkerPolicyList())) {
                    for (ProcessStepWorkerPolicyVo processStepWorkerPolicyVo : stepVo.getWorkerPolicyList()) {
                        processMapper.insertProcessStepWorkerPolicy(processStepWorkerPolicyVo);
                    }
                }
                InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = stepVo.getNotifyPolicyConfig();
                if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(invokeNotifyPolicyConfigVo)) {
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
        if (notifyServiceCrossoverService.checkNotifyPolicyIsExists(notifyPolicyConfig)) {
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

    @Override
    public void deleteProcessRelevantData(String uuid) {
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
        processMapper.deleteProcessFormByProcessUuid(uuid);
        processMapper.deleteProcessSlaByProcessUuid(uuid);
        scoreTemplateMapper.deleteProcessScoreTemplateByProcessUuid(uuid);
        processMapper.deleteProcessStepTagByProcessUuid(uuid);
        for (String stepUuid : processStepUuidList) {
            DependencyManager.delete(FormScene2ProcessStepDependencyHandler.class, stepUuid);
            DependencyManager.delete(EoaTemplate2ProcessStepDependencyHandler.class, stepUuid);
        }
    }

}
