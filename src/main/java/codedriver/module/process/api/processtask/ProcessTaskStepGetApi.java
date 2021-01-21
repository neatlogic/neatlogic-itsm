package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import com.alibaba.fastjson.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.common.config.ProcessConfig;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.module.process.service.ProcessTaskStepSubtaskService;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;

    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @Autowired
    private ScoreTemplateMapper scoreTemplateMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PriorityMapper priorityMapper;

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Override
    public String getToken() {
        return "processtask/step/get";
    }

    @Override
    public String getName() {
        return "工单步骤基本信息获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")})
    @Output({@Param(name = "processTask", explode = ProcessTaskVo.class, desc = "工单信息")})
    @Description(desc = "工单步骤基本信息获取接口，当前步骤名称、激活时间、状态、处理人、协助处理人、处理时效、表单属性显示控制等")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");

        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.TASK_VIEW).build()
                .checkAndNoPermissionThrowException();
        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);

        if (new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.TASK_SCORE).build().check()) {
            ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskId);
            if (processTaskScoreTemplateVo != null) {
                if (StringUtils.isNotBlank(processTaskScoreTemplateVo.getConfigHash())) {
                    String configStr = selectContentByHashMapper.getProcessTaskScoreTempleteConfigStringIsByHash(processTaskScoreTemplateVo.getConfigHash());
                    List<String> stepUuidList = JSON.parseArray(JSON.toJSONString(JSONPath.read(configStr, "stepUuidList")), String.class);
                    if (CollectionUtils.isNotEmpty(stepUuidList)) {
                        processTaskVo.setRedoStepList(processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskIdAndProcessStepUuidList(processTaskId, stepUuidList));
                    }
                }
                processTaskVo.setScoreTemplateVo(scoreTemplateMapper.getScoreTemplateById(processTaskScoreTemplateVo.getScoreTemplateId()));
            }
        }
        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));
        Map<String, String> formAttributeActionMap = new HashMap<>();
        if (processTaskStepId != null) {
            ProcessTaskStepVo currentProcessTaskStepVo = getCurrentProcessTaskStepById(processTaskStepId);
            if (currentProcessTaskStepVo != null) {
                if (new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.STEP_SAVE)
                        .build().check()) {
                    // 回复框内容和附件暂存回显
                    setTemporaryData(processTaskVo, currentProcessTaskStepVo);
                }
                processTaskVo.setCurrentProcessTaskStep(currentProcessTaskStepVo);
                if (MapUtils.isNotEmpty(currentProcessTaskStepVo.getFormAttributeDataMap())) {
                    processTaskVo.setFormAttributeDataMap(currentProcessTaskStepVo.getFormAttributeDataMap());
                }
                if (StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
                    // 表单属性显示控制
                    List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList =
                            processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
                    for (ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
                        processTaskStepFormAttributeVo
                                .setProcessStepUuid(currentProcessTaskStepVo.getProcessStepUuid());
                        formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(),
                                processTaskStepFormAttributeVo.getAction());
                    }
                    currentProcessTaskStepVo.setStepFormConfig(processTaskStepFormAttributeList);
                }
            }
        }
        if (StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
            boolean isAuthority = false;
            if (processTaskStepId != null) {
                isAuthority = new ProcessAuthManager.StepOperationChecker(processTaskStepId,
                        ProcessTaskOperationType.STEP_WORK).build().check();
            }
            processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap,
                    isAuthority ? 1 : 0);
        }

        // TODO 兼容老工单表单（判断是否存在旧表单）
        Map<String, String> oldFormPropMap = processTaskMapper.getProcessTaskOldFormAndPropByTaskId(processTaskId);
        if (oldFormPropMap != null && oldFormPropMap.size() > 0) {
            processTaskVo.setIsHasOldFormProp(1);
        }

        // 标签列表
        processTaskVo.setTagVoList(processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskId));

        // 移动端默认展开表单
        processTaskVo.setMobileFormUIType(Integer.valueOf(ProcessConfig.MOBILE_FORM_UI_TYPE()));
        JSONObject resultObj = new JSONObject();
        resultObj.put("processTask", processTaskVo);
        return resultObj;
    }

    /**
     * @param processTaskStepId 步骤id
     * @return ProcessTaskStepVo
     * @Author: linbq
     * @Time:2020年8月21日
     * @Description: 获取当前步骤信息
     */
    private ProcessTaskStepVo getCurrentProcessTaskStepById(Long processTaskStepId) {
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        Long processTaskId = processTaskStepVo.getProcessTaskId();
        if (new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.STEP_VIEW).build()
                .check()) {
            // 处理人列表
            processTaskService.setProcessTaskStepUser(processTaskStepVo);

            /** 当前步骤特有步骤信息 **/
            IProcessStepInternalHandler processStepUtilHandler =
                    ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getHandlerStepInitInfo(processTaskStepVo));
            // 步骤评论列表
            List<String> typeList = new ArrayList<>();
            typeList.add(ProcessTaskOperationType.STEP_COMMENT.getValue());
            typeList.add(ProcessTaskOperationType.STEP_COMPLETE.getValue());
            typeList.add(ProcessTaskOperationType.STEP_BACK.getValue());
            typeList.add(ProcessTaskOperationType.STEP_RETREAT.getValue());
            typeList.add(ProcessTaskOperationType.STEP_TRANSFER.getValue());
            processTaskStepVo.setCommentList(
                    processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepId, typeList));

            // 获取当前用户有权限的所有子任务
            // 子任务列表
            if (processTaskStepVo.getIsActive().intValue() == 1
                    && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList =
                        processTaskStepSubtaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
                if (CollectionUtils.isNotEmpty(processTaskStepSubtaskList)) {
                    Map<String, String> customButtonMap = processStepUtilHandler.getCustomButtonMapByConfigHashAndHandler(
                            processTaskStepVo.getConfigHash(), processTaskStepVo.getHandler());
                    for (ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
                        String currentUser = UserContext.get().getUserUuid(true);
                        if ((currentUser.equals(processTaskStepSubtask.getMajorUser())
                                && !ProcessTaskStatus.ABORTED.getValue().equals(processTaskStepSubtask.getStatus()))
                                || (currentUser.equals(processTaskStepSubtask.getUserUuid())
                                && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus()))) {
                            if (processTaskStepSubtask.getIsAbortable() == 1) {
                                String value = ProcessTaskOperationType.SUBTASK_ABORT.getValue();
                                String text = customButtonMap.get(value);
                                if (StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.SUBTASK_ABORT.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if (processTaskStepSubtask.getIsCommentable() == 1) {
                                String value = ProcessTaskOperationType.SUBTASK_COMMENT.getValue();
                                String text = customButtonMap.get(value);
                                if (StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.SUBTASK_COMMENT.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if (processTaskStepSubtask.getIsCompletable() == 1) {
                                String value = ProcessTaskOperationType.SUBTASK_COMPLETE.getValue();
                                String text = customButtonMap.get(value);
                                if (StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.SUBTASK_COMPLETE.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if (processTaskStepSubtask.getIsEditable() == 1) {
                                String value = ProcessTaskOperationType.SUBTASK_EDIT.getValue();
                                String text = customButtonMap.get(value);
                                if (StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.SUBTASK_EDIT.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if (processTaskStepSubtask.getIsRedoable() == 1) {
                                String value = ProcessTaskOperationType.SUBTASK_REDO.getValue();
                                String text = customButtonMap.get(value);
                                if (StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.SUBTASK_REDO.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            processTaskStepVo.getProcessTaskStepSubtaskList().add(processTaskStepSubtask);
                        }
                    }
                }
            }

            // 获取可分配处理人的步骤列表
            processTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepList(
                    processTaskStepVo.getProcessTaskId(), processTaskStepVo.getProcessStepUuid()));

            // 时效列表
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
            processTaskStepVo.setSlaTimeList(processTaskService
                    .getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(processTaskStepId, processTaskVo.getWorktimeUuid()));

            // 补充 automatic processtaskStepData
            ProcessTaskStepDataVo stepDataVo = processTaskStepDataMapper
                    .getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),
                            processTaskStepVo.getId(), processTaskStepVo.getHandler(), "system"));
            boolean hasComplete =
                    new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.STEP_COMPLETE)
                            .build().check();
            if (stepDataVo != null) {
                JSONObject stepDataJson = stepDataVo.getData();
                processTaskStepVo.setProcessTaskStepData(stepDataJson);
                if (hasComplete) {// 有处理权限
                    stepDataJson.put("isStepUser", 1);
                    if (processTaskStepVo.getHandler().equals(ProcessStepHandlerType.AUTOMATIC.getHandler())) {
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
            }
            /** 下一步骤列表 **/
            processTaskStepVo.setForwardNextStepList(
                    processTaskService.getForwardNextStepListByProcessTaskStepId(processTaskStepVo.getId()));
            processTaskStepVo.setBackwardNextStepList(
                    processTaskService.getBackwardNextStepListByProcessTaskStepId(processTaskStepVo.getId()));
            ;
            /** 提醒列表 **/
            List<ProcessTaskStepRemindVo> processTaskStepRemindList =
                    processTaskService.getProcessTaskStepRemindListByProcessTaskStepId(processTaskStepId);
            processTaskStepVo.setProcessTaskStepRemindList(processTaskStepRemindList);

            ProcessTaskStepAgentVo processTaskStepAgentVo =
                    processTaskMapper.getProcessTaskStepAgentByProcessTaskStepId(processTaskStepId);
            if (processTaskStepAgentVo != null) {
                processTaskStepVo.setOriginalUser(processTaskStepAgentVo.getUserUuid());
                UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepAgentVo.getUserUuid());
                if (userVo != null) {
                    UserVo vo = new UserVo();
                    BeanUtils.copyProperties(userVo, vo);
                    processTaskStepVo.setOriginalUserVo(vo);
//                    processTaskStepVo.setOriginalUserName(userVo.getUserName());
                }
            }
            /** 如果当前用户有处理权限，则获取其有权看到的配置的回复模版 */
            if (hasComplete) {
                List<String> authList = new ArrayList<>();
                List<String> teamUuidList = userMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid());
                List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid());
                authList.addAll(teamUuidList);
                authList.addAll(roleUuidList);
                authList.add(UserType.ALL.getValue());
                authList.add(UserContext.get().getUserUuid());
                ProcessCommentTemplateVo commentTemplate = commentTemplateMapper
                        .getTemplateByStepUuidAndAuth(processTaskStepVo.getProcessStepUuid(), authList);
                processTaskStepVo.setCommentTemplate(commentTemplate);
            }

            return processTaskStepVo;
        }
        return null;
    }

    /**
     * @Description: 设置步骤当前用户的暂存数据
     * @Author: linbq
     * @Date: 2020/8/21 10:13
     * @Params:[processTaskVo, processTaskStepVo]
     * @Returns:void
     **/
    private void setTemporaryData(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepVo.getId());
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        ProcessTaskStepDataVo stepDraftSaveData =
                processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if (stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if (MapUtils.isNotEmpty(dataObj)) {
                JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
                    Map<String, Object> formAttributeDataMap = new HashMap<>();
                    for (int i = 0; i < formAttributeDataList.size(); i++) {
                        JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                        formAttributeDataMap.put(formAttributeDataObj.getString("attributeUuid"),
                                formAttributeDataObj.get("dataList"));
                    }
                    processTaskStepVo.setFormAttributeDataMap(formAttributeDataMap);
                }
                ProcessTaskStepReplyVo commentVo = new ProcessTaskStepReplyVo();
                String content = dataObj.getString("content");
                commentVo.setContent(content);
                List<Long> fileIdList =
                        JSON.parseArray(JSON.toJSONString(dataObj.getJSONArray("fileIdList")), Long.class);
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    commentVo.setFileList(fileMapper.getFileListByIdList(fileIdList));
                }
                processTaskStepVo.setComment(commentVo);
                /** 当前步骤特有步骤信息 **/
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if (handlerStepInfo != null) {
                    processTaskStepVo.setHandlerStepInfo(handlerStepInfo);
                }
                String priorityUuid = dataObj.getString("priorityUuid");
                if (StringUtils.isNotBlank(priorityUuid)) {
                    processTaskVo.setPriorityUuid(priorityUuid);
                    PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priorityUuid);
                    if (priorityVo == null) {
                        priorityVo = new PriorityVo();
                        priorityVo.setUuid(priorityUuid);
                    }
                    processTaskVo.setPriority(priorityVo);
                }
            }
        }
    }
}
