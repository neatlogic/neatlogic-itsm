package codedriver.module.process.stephandler.utilhandler;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.dao.mapper.ProcessTaskStepSubtaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.dto.processconfig.ActionConfigActionVo;
import codedriver.framework.process.dto.processconfig.ActionConfigVo;
import codedriver.framework.process.dto.processconfig.NotifyPolicyConfigVo;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.module.process.notify.handler.OmnipotentNotifyPolicyHandler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OmnipotentProcessUtilHandler extends ProcessStepInternalHandlerBase {


    @Autowired
    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.OMNIPOTENT.getHandler();
    }

    @Override
    public Object getHandlerStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getHandlerStepInitInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
        /** 组装通知策略id **/
        JSONObject notifyPolicyConfig = stepConfigObj.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo != null) {
            Long policyId = notifyPolicyConfigVo.getPolicyId();
            if (policyId != null) {
                processStepVo.setNotifyPolicyId(policyId);
            }
        }

        JSONObject actionConfig = stepConfigObj.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSONObject.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo != null) {
            List<ActionConfigActionVo> actionList = actionConfigVo.getActionList();
            if (CollectionUtils.isNotEmpty(actionList)) {
                List<String> integrationUuidList = new ArrayList<>();
                for (ActionConfigActionVo actionVo : actionList) {
                    String integrationUuid = actionVo.getIntegrationUuid();
                    if (StringUtils.isNotBlank(integrationUuid)) {
                        integrationUuidList.add(integrationUuid);
                    }
                }
                processStepVo.setIntegrationUuidList(integrationUuidList);
            }
        }

        /** 组装分配策略 **/
        JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
        if (MapUtils.isNotEmpty(workerPolicyConfig)) {
            JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
            if (CollectionUtils.isNotEmpty(policyList)) {
                List<ProcessStepWorkerPolicyVo> workerPolicyList = new ArrayList<>();
                for (int k = 0; k < policyList.size(); k++) {
                    JSONObject policyObj = policyList.getJSONObject(k);
                    if (!"1".equals(policyObj.getString("isChecked"))) {
                        continue;
                    }
                    ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
                    processStepWorkerPolicyVo.setProcessUuid(processStepVo.getProcessUuid());
                    processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
                    processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
                    processStepWorkerPolicyVo.setSort(k + 1);
                    processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
                    workerPolicyList.add(processStepWorkerPolicyVo);
                }
                processStepVo.setWorkerPolicyList(workerPolicyList);
            }
        }
        //保存回复模版ID
        Long commentTemplateId = stepConfigObj.getLong("commentTemplateId");
        processStepVo.setCommentTemplateId(commentTemplateId);

        JSONArray tagList = stepConfigObj.getJSONArray("tagList");
        if (CollectionUtils.isNotEmpty(tagList)) {
            processStepVo.setTagList(tagList.toJavaList(String.class));
        }

        //保存子任务
        JSONObject taskConfig = stepConfigObj.getJSONObject("taskConfig");
        if(MapUtils.isNotEmpty(taskConfig)){
            ProcessStepTaskConfigVo taskConfigVo = JSONObject.toJavaObject(taskConfig,ProcessStepTaskConfigVo.class);
            processStepVo.setTaskConfigVo(taskConfigVo);
        }
    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
        /* 查出processtask_step_subtask表中当前步骤子任务处理人列表 */
        Set<String> runningSubtaskUserUuidSet = new HashSet<>();
        Set<String> succeedSubtaskUserUuidSet = new HashSet<>();
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
        for (ProcessTaskStepSubtaskVo subtaskVo : processTaskStepSubtaskList) {
            if (ProcessTaskStatus.RUNNING.getValue().equals(subtaskVo.getStatus())) {
                runningSubtaskUserUuidSet.add(subtaskVo.getUserUuid());
            } else if (ProcessTaskStatus.SUCCEED.getValue().equals(subtaskVo.getStatus())) {
                succeedSubtaskUserUuidSet.add(subtaskVo.getUserUuid());
            }
        }

        /* 查出processtask_step_worker表中当前步骤子任务处理人列表 */
        Set<String> workerMinorUserUuidSet = new HashSet<>();
        List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskId, processTaskStepId);
        for (ProcessTaskStepWorkerVo workerVo : workerList) {
            if (ProcessUserType.MINOR.getValue().equals(workerVo.getUserType())) {
                workerMinorUserUuidSet.add(workerVo.getUuid());
            }
        }

        /* 查出processtask_step_user表中当前步骤子任务处理人列表 */
        Set<String> doingMinorUserUuidSet = new HashSet<>();
        Set<String> doneMinorUserUuidSet = new HashSet<>();
        List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
        for (ProcessTaskStepUserVo userVo : minorUserList) {
            if (ProcessTaskStepUserStatus.DOING.getValue().equals(userVo.getStatus())) {
                doingMinorUserUuidSet.add(userVo.getUserVo().getUuid());
            } else if (ProcessTaskStepUserStatus.DONE.getValue().equals(userVo.getStatus())) {
                doneMinorUserUuidSet.add(userVo.getUserVo().getUuid());
            }
        }

        ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo();
        processTaskStepWorkerVo.setProcessTaskId(processTaskId);
        processTaskStepWorkerVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepWorkerVo.setType(GroupSearch.USER.getValue());
        processTaskStepWorkerVo.setUserType(ProcessUserType.MINOR.getValue());

        ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
        processTaskStepUserVo.setProcessTaskId(processTaskId);
        processTaskStepUserVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
        /* 删除processtask_step_worker表中当前步骤多余的子任务处理人 */
        List<String> needDeleteUserList = ListUtils.removeAll(workerMinorUserUuidSet, runningSubtaskUserUuidSet);
        for (String userUuid : needDeleteUserList) {
            processTaskStepWorkerVo.setUuid(userUuid);
            processTaskMapper.deleteProcessTaskStepWorker(processTaskStepWorkerVo);
            if (succeedSubtaskUserUuidSet.contains(userUuid)) {
                if (doingMinorUserUuidSet.contains(userUuid)) {
                    /* 完成子任务 */
                    processTaskStepUserVo.setUserVo(new UserVo(userUuid));
                    processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
                    processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
                }
            } else {
                if (doingMinorUserUuidSet.contains(userUuid)) {
                    /* 取消子任务 */
                    processTaskStepUserVo.setUserVo(new UserVo(userUuid));
                    processTaskMapper.deleteProcessTaskStepUser(processTaskStepUserVo);
                }
            }
        }
        /* 向processtask_step_worker表中插入当前步骤的子任务处理人 */
        List<String> needInsertUserList = ListUtils.removeAll(runningSubtaskUserUuidSet, workerMinorUserUuidSet);
        for (String userUuid : needInsertUserList) {
            processTaskStepWorkerVo.setUuid(userUuid);
            processTaskMapper.insertIgnoreProcessTaskStepWorker(processTaskStepWorkerVo);

            if (doneMinorUserUuidSet.contains(userUuid)) {
                /* 重做子任务 */
                processTaskStepUserVo.setUserVo(new UserVo(userUuid));
                processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
                processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
            } else if (!doingMinorUserUuidSet.contains(userUuid)) {
                /* 创建子任务 */
                processTaskStepUserVo.setUserVo(new UserVo(userUuid));
                processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
                processTaskMapper.insertProcessTaskStepUser(processTaskStepUserVo);
            }
        }
    }

    @SuppressWarnings("serial")
    @Override
    public JSONObject makeupConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /* 授权 */
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.STEP_VIEW,
                ProcessTaskOperationType.STEP_TRANSFER,
                ProcessTaskOperationType.STEP_RETREAT
        };
        JSONArray authorityList = null;
        Integer enableAuthority = configObj.getInteger("enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = configObj.getJSONArray("authorityList");
        } else {
            enableAuthority = 0;
        }
        resultObj.put("enableAuthority", enableAuthority);
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /* 按钮映射列表 */
        ProcessTaskOperationType[] stepButtons = {
                ProcessTaskOperationType.STEP_COMPLETE,
                ProcessTaskOperationType.STEP_BACK,
                ProcessTaskOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskOperationType.STEP_START,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER
        };
        /* 子任务按钮映射列表 */
        ProcessTaskOperationType[] subtaskButtons = {
                ProcessTaskOperationType.SUBTASK_ABORT,
                ProcessTaskOperationType.SUBTASK_COMMENT,
                ProcessTaskOperationType.SUBTASK_COMPLETE,
                ProcessTaskOperationType.SUBTASK_CREATE,
                ProcessTaskOperationType.SUBTASK_REDO,
                ProcessTaskOperationType.SUBTASK_EDIT
        };

        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        JSONArray subtaskCustomButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, subtaskButtons, "子任务");
        customButtonArray.addAll(subtaskCustomButtonArray);
        resultObj.put("customButtonList", customButtonArray);

        /* 状态映射列表 */
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /* 可替换文本列表 */
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /* 通知 */
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo == null) {
            notifyPolicyConfigVo = new NotifyPolicyConfigVo();
        }
        notifyPolicyConfigVo.setHandler(OmnipotentNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyConfigVo);

        /* 任务 */
        JSONObject taskConfig = configObj.getJSONObject("taskConfig");
        resultObj.put("taskConfig",taskConfig);
        return resultObj;
    }

    @Override
    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.STEP_VIEW,
                ProcessTaskOperationType.STEP_TRANSFER,
                ProcessTaskOperationType.STEP_RETREAT
        };
        JSONArray authorityList = null;
        Integer enableAuthority = configObj.getInteger("enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = configObj.getJSONArray("authorityList");
        } else {
            enableAuthority = 0;
        }
        resultObj.put("enableAuthority", enableAuthority);
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /** 通知 **/
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo == null) {
            notifyPolicyConfigVo = new NotifyPolicyConfigVo();
        }
        notifyPolicyConfigVo.setHandler(OmnipotentNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyConfigVo);

        /** 动作 **/
        JSONObject actionConfig = configObj.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSONObject.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo == null) {
            actionConfigVo = new ActionConfigVo();
        }
        actionConfigVo.setHandler(OmnipotentNotifyPolicyHandler.class.getName());
        resultObj.put("actionConfig", actionConfigVo);

        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        /** 按钮映射列表 **/
        ProcessTaskOperationType[] stepButtons = {
                ProcessTaskOperationType.STEP_COMPLETE,
                ProcessTaskOperationType.STEP_BACK,
                ProcessTaskOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskOperationType.STEP_START,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER
        };

        /** 子任务按钮映射列表 **/
        ProcessTaskOperationType[] subtaskButtons = {
                ProcessTaskOperationType.SUBTASK_ABORT,
                ProcessTaskOperationType.SUBTASK_COMMENT,
                ProcessTaskOperationType.SUBTASK_COMPLETE,
                ProcessTaskOperationType.SUBTASK_CREATE,
                ProcessTaskOperationType.SUBTASK_REDO,
                ProcessTaskOperationType.SUBTASK_EDIT
        };
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        JSONArray subtaskCustomButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, subtaskButtons, "子任务");
        customButtonArray.addAll(subtaskCustomButtonArray);
        resultObj.put("customButtonList", customButtonArray);
        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /** 可替换文本列表 **/
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /** 分配处理人 **/
        JSONObject workerPolicyConfig = configObj.getJSONObject("workerPolicyConfig");
        JSONObject workerPolicyObj = ProcessConfigUtil.regulateWorkerPolicyConfig(workerPolicyConfig);
        resultObj.put("workerPolicyConfig", workerPolicyObj);

        /* 任务 */
        JSONObject taskConfig = configObj.getJSONObject("taskConfig");
        resultObj.put("taskConfig",taskConfig);

        JSONObject simpleSettings = ProcessConfigUtil.regulateSimpleSettings(configObj);
        resultObj.putAll(simpleSettings);
        return resultObj;
    }

}
