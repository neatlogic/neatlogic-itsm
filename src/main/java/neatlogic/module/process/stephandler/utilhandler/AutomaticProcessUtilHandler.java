package neatlogic.module.process.stephandler.utilhandler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AutomaticProcessUtilHandler extends ProcessStepInternalHandlerBase {
    @Resource
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.AUTOMATIC.getHandler();
    }

    @Override
    public Object getStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getNonStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskStepDataVo stepDataVo = processTaskStepDataMapper
                .getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),
                        currentProcessTaskStepVo.getId(), currentProcessTaskStepVo.getHandler(), SystemUser.SYSTEM.getUserUuid()));
        JSONObject stepDataJson = stepDataVo.getData();
        boolean hasComplete =
                new ProcessAuthManager.StepOperationChecker(currentProcessTaskStepVo.getId(), ProcessTaskStepOperationType.STEP_COMPLETE)
                        .build().check();
        if (hasComplete) {// 有处理权限
            stepDataJson.put("isStepUser", 1);
            if (currentProcessTaskStepVo.getHandler().equals(ProcessStepHandlerType.AUTOMATIC.getHandler())) {
                JSONObject requestAuditJson = stepDataJson.getJSONObject("requestAudit");
                if (requestAuditJson.containsKey("status") && requestAuditJson.getJSONObject("status")
                        .getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                    requestAuditJson.put("isRetry", 1);
                } else {
                    requestAuditJson.put("isRetry", 0);
                }
                JSONObject callbackAuditJson = stepDataJson.getJSONObject("callbackAudit");
                if (callbackAuditJson != null) {
                    if (callbackAuditJson.containsKey("status") && callbackAuditJson.getJSONObject("status")
                            .getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                        callbackAuditJson.put("isRetry", 1);
                    } else {
                        callbackAuditJson.put("isRetry", 0);
                    }
                }
            }
        }
        return stepDataJson;
    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
    }

    public IOperationType[] getStepActions() {
        return new IOperationType[]{
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER
        };
    }

    @Override
    public IOperationType[] getStepButtons() {
        return new IOperationType[]{
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT
        };
    }

    @Override
    public String[] getRegulateKeyList() {
        return new String[]{"authorityList", "notifyPolicyConfig", "customButtonList", "customStatusList", "replaceableTextList", "workerPolicyConfig", "formSceneUuid", "formSceneName", "tagList", "automaticConfig", "formTag"};
    }

}
