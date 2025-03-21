package neatlogic.module.process.stephandler.utilhandler;

import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dto.ProcessTaskStepTaskUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.stephandler.core.IProcessStepAssistantHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.module.process.notify.handler.OmnipotentNotifyPolicyHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OmnipotentProcessUtilHandler extends ProcessStepInternalHandlerBase implements IProcessStepAssistantHandler {

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.OMNIPOTENT.getHandler();
    }

    @Override
    public Object getStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getNonStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
        defaultUpdateProcessTaskStepUserAndWorker(processTaskId, processTaskStepId);
    }

    public IOperationType[] getStepActions() {
        return new IOperationType[]{
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER,
                ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskStepOperationType.STEP_RETREAT
        };
    }

    @Override
    public IOperationType[] getStepButtons() {
        return new IOperationType[]{
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskStepOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER,
                ProcessTaskStepOperationType.STEP_REAPPROVAL
        };
    }

    @Override
    public Class<? extends INotifyPolicyHandler> getNotifyPolicyHandlerClass() {
        return OmnipotentNotifyPolicyHandler.class;
    }

    @Override
    public String[] getRegulateKeyList() {
        return new String[]{"authorityList", "notifyPolicyConfig", "actionConfig", "customButtonList", "customStatusList", "replaceableTextList", "workerPolicyConfig", "taskConfig", "enableReapproval", "autoComplete", "autoApproval", "showAutoApproval", "formSceneUuid", "formSceneName", "autoStart", "isNeedUploadFile", "isNeedContent", "isRequired", "commentTemplateId", "tagList"};
    }

    @Override
    public List<ProcessTaskStepUserVo> getMinorUserListForNotifyReceiver(ProcessTaskStepVo currentProcessTaskStepVo) {
        List<ProcessTaskStepUserVo> resultList = new ArrayList<>();
        /* 当前任务处理人 */
        ProcessTaskStepTaskVo stepTaskVo = currentProcessTaskStepVo.getProcessTaskStepTaskVo();
        if (stepTaskVo != null) {
            List<ProcessTaskStepTaskUserVo> taskUserVoList = stepTaskVo.getStepTaskUserVoList();
            if (CollectionUtils.isNotEmpty(taskUserVoList)) {
                for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                    processTaskStepUserVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                    processTaskStepUserVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                    processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
                    processTaskStepUserVo.setUserUuid(taskUserVo.getUserUuid());
                    resultList.add(processTaskStepUserVo);
                }
            }
        }
        return resultList;
    }
}
