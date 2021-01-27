package codedriver.module.process.stephandler.utilhandler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.TransactionSynchronizationPool;
import codedriver.framework.common.RootComponent;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.notify.core.INotifyTriggerType;
import codedriver.framework.process.audithandler.core.IProcessTaskAuditType;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTimeAuditMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.process.stepremind.core.IProcessTaskStepRemindType;
import codedriver.module.process.thread.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: ProcessStepHandlerUtilServiceImpl
 * @Package codedriver.module.process.stephandler.service
 * @Description: TODO
 * @Author: linbq
 * @Date: 2021/1/20 16:26
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@RootComponent
public class ProcessStepHandlerUtil implements IProcessStepHandlerUtil {
    @Autowired
    private ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private FileMapper fileMapper;
    
    /**
     * @param currentProcessTaskStepVo
     * @param trigger
     * @Description: 触发动作
     * @Author: linbq
     * @Date: 2021/1/20 16:15
     * @Params:[currentProcessTaskStepVo, trigger]
     * @Returns:void
     */
    @Override
    public void action(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger) {
        TransactionSynchronizationPool.execute(new ProcessTaskActionThread(currentProcessTaskStepVo, trigger));
    }

    /**
     * @param currentProcessTaskStepVo
     * @param trigger
     * @Description: 触发通知
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskStepVo, trigger]
     * @Returns:void
     */
    @Override
    public void notify(ProcessTaskStepVo currentProcessTaskStepVo, INotifyTriggerType trigger) {
        TransactionSynchronizationPool.execute(new ProcessTaskNotifyThread(currentProcessTaskStepVo, trigger));
    }

    /**
     * @param currentProcessTaskVo
     * @param isAsync
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo, boolean isAsync) {
        calculateSla(currentProcessTaskVo, null, isAsync);
    }

    /**
     * @param currentProcessTaskVo
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo) {
        calculateSla(currentProcessTaskVo, null, true);
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskStepVo currentProcessTaskStepVo) {
        calculateSla(null, currentProcessTaskStepVo, true);
    }

    /**
     * @param currentProcessTaskVo
     * @param currentProcessTaskStepVo
     * @param isAsync
     * @Description: 计算时效
     * @Author: linbq
     * @Date: 2021/1/20 16:17
     * @Params:[currentProcessTaskVo, isAsync]
     * @Returns:void
     */
    @Override
    public void calculateSla(ProcessTaskVo currentProcessTaskVo, ProcessTaskStepVo currentProcessTaskStepVo, boolean isAsync) {
        if (!isAsync) {
            new ProcessTaskSlaThread(currentProcessTaskVo, currentProcessTaskStepVo).execute();
        }else{
            TransactionSynchronizationPool.execute(new ProcessTaskSlaThread(currentProcessTaskVo, currentProcessTaskStepVo));
        }
    }

    /**
     * @param currentProcessTaskStepVo
     * @param action
     * @Description: 记录操作时间
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     */
    @Override
    public void timeAudit(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskOperationType action) {
        ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper
                .getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
        ProcessTaskStepTimeAuditVo newAuditVo = new ProcessTaskStepTimeAuditVo();
        newAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
        switch (action) {
            case STEP_ACTIVE:
                newAuditVo.setActiveTime("now");
                if (processTaskStepTimeAuditVo == null
                        || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getActiveTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case STEP_START:
                newAuditVo.setStartTime("now");
                if (processTaskStepTimeAuditVo == null
                        || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getStartTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
                    newAuditVo.setId(processTaskStepTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case STEP_COMPLETE:
                /** 如果找不到审计记录并且completetime不为空，则新建审计记录 **/
                newAuditVo.setCompleteTime("now");
                if (processTaskStepTimeAuditVo == null
                        || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getCompleteTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getCompleteTime())) {// 如果completetime为空，则更新completetime
                    newAuditVo.setId(processTaskStepTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case TASK_ABORT:
                /** 如果找不到审计记录并且aborttime不为空，则新建审计记录 **/
                newAuditVo.setAbortTime("now");
                if (processTaskStepTimeAuditVo == null
                        || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getAbortTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getAbortTime())) {// 如果aborttime为空，则更新aborttime
                    newAuditVo.setId(processTaskStepTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case STEP_BACK:
                /** 如果找不到审计记录并且backtime不为空，则新建审计记录 **/
                newAuditVo.setBackTime("now");
                if (processTaskStepTimeAuditVo == null
                        || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getBackTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getBackTime())) {// 如果backtime为空，则更新backtime
                    newAuditVo.setId(processTaskStepTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
            case TASK_RECOVER:
                if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())) {
                    newAuditVo.setActiveTime("now");
                    if (processTaskStepTimeAuditVo == null
                            || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getActiveTime())) {
                        processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                    }
                } else if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())) {
                    newAuditVo.setStartTime("now");
                    if (processTaskStepTimeAuditVo == null
                            || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getStartTime())) {
                        processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                    } else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
                        newAuditVo.setId(processTaskStepTimeAuditVo.getId());
                        processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                    }
                }
                break;
            case STEP_PAUSE:
                /** 如果找不到审计记录并且pausetime不为空，则新建审计记录 **/
                newAuditVo.setPauseTime("now");
                if (processTaskStepTimeAuditVo == null
                        || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getPauseTime())) {
                    processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
                } else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getPauseTime())) {// 如果pausetime为空，则更新pausetime
                    newAuditVo.setId(processTaskStepTimeAuditVo.getId());
                    processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
                }
                break;
        }
    }

    /**
     * @param currentProcessTaskStepVo
     * @param action
     * @Description: 记录操作活动
     * @Author: linbq
     * @Date: 2021/1/20 16:19
     * @Params:[currentProcessTaskStepVo, action]
     * @Returns:void
     */
    @Override
    public void audit(ProcessTaskStepVo currentProcessTaskStepVo, IProcessTaskAuditType action) {
        TransactionSynchronizationPool.execute(new ProcessTaskAuditThread(currentProcessTaskStepVo, action));
    }

    /**
     * @param currentProcessTaskVo
     * @Description: 自动评分
     * @Author: linbq
     * @Date: 2021/1/20 16:22
     * @Params:[currentProcessTaskVo]
     * @Returns:void
     */
    @Override
    public void autoScore(ProcessTaskVo currentProcessTaskVo) {
        TransactionSynchronizationPool.execute(new ProcessTaskAutoScoreThread(currentProcessTaskVo));
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 获取需要验证表单数据，并校验
     * @Author: linbq
     * @Date: 2021/1/20 16:20
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
     */
    @Override
    public boolean formAttributeDataValidFromDb(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskFormVo processTaskFormVo =
                processTaskMapper.getProcessTaskFormByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent =
                    selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            FormVersionVo formVersionVo = new FormVersionVo();
            formVersionVo.setFormConfig(formContent);
            formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
            List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
            if (CollectionUtils.isNotEmpty(formAttributeList)) {
                Map<String, Object> formAttributeDataMap = new HashMap<>();
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList =
                        processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(
                                currentProcessTaskStepVo.getProcessTaskId());
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(),
                            processTaskFormAttributeDataVo.getDataObj());
                }
                // Map<String, String> formAttributeActionMap = new HashMap<>();
                // List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList =
                // processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(currentProcessTaskStepVo.getId());
                // for (ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo :
                // processTaskStepFormAttributeList) {
                // formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(),
                // processTaskStepFormAttributeVo.getAction());
                // }
                currentProcessTaskStepVo.setFormAttributeDataMap(formAttributeDataMap);
                currentProcessTaskStepVo.setFormAttributeVoList(formAttributeList);
                // currentProcessTaskStepVo.setFormAttributeActionMap(formAttributeActionMap);
                formAttributeDataValid(currentProcessTaskStepVo);
            }
        }

        return true;
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 验证表单数据是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:20
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
     */
    @Override
    public boolean formAttributeDataValid(ProcessTaskStepVo currentProcessTaskStepVo) {
        List<String> hidecomponentList = JSON.parseArray(
                JSON.toJSONString(currentProcessTaskStepVo.getParamObj().getJSONArray("hidecomponentList")),
                String.class);
        List<String> readcomponentList = JSON.parseArray(
                JSON.toJSONString(currentProcessTaskStepVo.getParamObj().getJSONArray("readcomponentList")),
                String.class);
        for (FormAttributeVo formAttributeVo : currentProcessTaskStepVo.getFormAttributeVoList()) {
            if (!formAttributeVo.isRequired()) {
                continue;
            }
            // if(formAttributeActionMap.containsKey(formAttributeVo.getUuid()) ||
            // formAttributeActionMap.containsKey("all")) {
            // continue;
            // }
            if (CollectionUtils.isNotEmpty(hidecomponentList)
                    && hidecomponentList.contains(formAttributeVo.getUuid())) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(readcomponentList)
                    && readcomponentList.contains(formAttributeVo.getUuid())) {
                continue;
            }
            Object data = currentProcessTaskStepVo.getFormAttributeDataMap().get(formAttributeVo.getUuid());
            if (data != null) {
                if (data instanceof String) {
                    if (StringUtils.isBlank(data.toString())) {
                        throw new ProcessTaskRuntimeException("表单属性：'" + formAttributeVo.getLabel() + "'不能为空");
                    }
                } else if (data instanceof JSONArray) {
                    if (CollectionUtils.isEmpty((JSONArray)data)) {
                        throw new ProcessTaskRuntimeException("表单属性：'" + formAttributeVo.getLabel() + "'不能为空");
                    }
                } else if (data instanceof JSONObject) {
                    if (MapUtils.isEmpty((JSONObject)data)) {
                        throw new ProcessTaskRuntimeException("表单属性：'" + formAttributeVo.getLabel() + "'不能为空");
                    }
                }
            } else {
                throw new ProcessTaskRuntimeException("表单属性：'" + formAttributeVo.getLabel() + "'不能为空");
            }
        }
        return true;
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 获取验证基本信息数据是否合法，并验证
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
     */
    @Override
    public boolean baseInfoValidFromDb(ProcessTaskStepVo currentProcessTaskStepVo) {
        ProcessTaskVo processTaskVo =
                processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
        baseInfoValid(currentProcessTaskStepVo, processTaskVo);
        return true;
    }

    /**
     * @param currentProcessTaskStepVo
     * @param processTaskVo
     * @Description: 验证基本信息数据是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo, processTaskVo]
     * @Returns:boolean
     */
    @Override
    public boolean baseInfoValid(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskVo processTaskVo) {
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        if (processTaskVo.getTitle() == null) {
            throw new ProcessTaskRuntimeException("工单标题格式不能为空");
        }
        /* 标题不限制输入
         * Pattern titlePattern = Pattern.compile("^[A-Za-z_\\d\\u4e00-\\u9fa5]+$"); if
         * (!titlePattern.matcher(processTaskVo.getTitle()).matches()) { throw new
         * ProcessTaskRuntimeException("工单标题格式不对"); }
         */
        paramObj.put(ProcessTaskAuditDetailType.TITLE.getParamName(), processTaskVo.getTitle());
        if (StringUtils.isBlank(processTaskVo.getOwner())) {
            throw new ProcessTaskRuntimeException("工单请求人不能为空");
        }
        if (userMapper.getUserBaseInfoByUuid(processTaskVo.getOwner()) == null) {
            throw new ProcessTaskRuntimeException("工单请求人账号:'" + processTaskVo.getOwner() + "'不存在");
        }
        if (StringUtils.isBlank(processTaskVo.getPriorityUuid())) {
            throw new ProcessTaskRuntimeException("工单优先级不能为空");
        }
        List<ChannelPriorityVo> channelPriorityList =
                channelMapper.getChannelPriorityListByChannelUuid(processTaskVo.getChannelUuid());
        List<String> priorityUuidlist = new ArrayList<>(channelPriorityList.size());
        for (ChannelPriorityVo channelPriorityVo : channelPriorityList) {
            priorityUuidlist.add(channelPriorityVo.getPriorityUuid());
        }
        if (!priorityUuidlist.contains(processTaskVo.getPriorityUuid())) {
            throw new ProcessTaskRuntimeException("工单优先级与服务优先级级不匹配");
        }
        paramObj.put(ProcessTaskAuditDetailType.PRIORITY.getParamName(), processTaskVo.getPriorityUuid());

        // 获取上报描述内容
        List<Long> fileIdList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList =
                processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(currentProcessTaskStepVo.getId());
        for (ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
            if (ProcessTaskOperationType.TASK_START.getValue().equals(processTaskStepContent.getType())) {
                paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), selectContentByHashMapper
                        .getProcessTaskContentStringByHash(processTaskStepContent.getContentHash()));
                fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepContent.getId());
                break;
            }
        }

        if (CollectionUtils.isNotEmpty(fileIdList)) {
            for (Long fileId : fileIdList) {
                if (fileMapper.getFileById(fileId) == null) {
                    throw new ProcessTaskRuntimeException("上传附件uuid:'" + fileId + "'不存在");
                }
            }
            paramObj.put(ProcessTaskAuditDetailType.FILE.getParamName(), JSON.toJSONString(fileIdList));
        }
        currentProcessTaskStepVo.setParamObj(paramObj);
        return true;
    }

    /**
     * @param currentProcessTaskStepVo
     * @Description: 验证前置步骤指派处理人是否合法
     * @Author: linbq
     * @Date: 2021/1/20 16:21
     * @Params:[currentProcessTaskStepVo]
     * @Returns:boolean
     */
    @Override
    public boolean assignWorkerValid(ProcessTaskStepVo currentProcessTaskStepVo) {
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        // 前置步骤指派处理人
        // "assignWorkerList": [
        // {
        // "processTaskStepId": 1,
        // "processStepUuid": "abc",
        // "workerList": [
        // "user#xxx",
        // "team#xxx",
        // "role#xxx"
        // ]
        // }
        // ]
        Map<Long, List<String>> assignWorkerMap = new HashMap<>();
        JSONArray assignWorkerList = paramObj.getJSONArray("assignWorkerList");
        if (CollectionUtils.isNotEmpty(assignWorkerList)) {
            for (int i = 0; i < assignWorkerList.size(); i++) {
                JSONObject assignWorker = assignWorkerList.getJSONObject(i);
                Long processTaskStepId = assignWorker.getLong("processTaskStepId");
                if (processTaskStepId == null) {
                    String processStepUuid = assignWorker.getString("processStepUuid");
                    if (processStepUuid != null) {
                        ProcessTaskStepVo processTaskStepVo =
                                processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskIdAndProcessStepUuid(
                                        currentProcessTaskStepVo.getProcessTaskId(), processStepUuid);
                        if (processTaskStepVo != null) {
                            processTaskStepId = processTaskStepVo.getId();
                        }
                    }
                }
                if (processTaskStepId != null) {
                    assignWorkerMap.put(processTaskStepId,
                            JSON.parseArray(assignWorker.getString("workerList"), String.class));
                }
            }
        }

        // 获取可分配处理人的步骤列表
        ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        processTaskStepWorkerPolicyVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList =
                processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
        if (CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
            for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
                if (WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    List<String> processStepUuidList = JSON
                            .parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
                    for (String processStepUuid : processStepUuidList) {
                        if (currentProcessTaskStepVo.getProcessStepUuid().equals(processStepUuid)) {
                            List<ProcessTaskStepUserVo> majorList =
                                    processTaskMapper.getProcessTaskStepUserByStepId(
                                            workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                            if (CollectionUtils.isEmpty(majorList)) {
                                ProcessTaskAssignWorkerVo assignWorkerVo = new ProcessTaskAssignWorkerVo();
                                assignWorkerVo.setProcessTaskId(workerPolicyVo.getProcessTaskId());
                                assignWorkerVo.setProcessTaskStepId(workerPolicyVo.getProcessTaskStepId());
                                assignWorkerVo.setFromProcessTaskStepId(currentProcessTaskStepVo.getId());
                                assignWorkerVo
                                        .setFromProcessStepUuid(currentProcessTaskStepVo.getProcessStepUuid());
                                processTaskMapper.deleteProcessTaskAssignWorker(assignWorkerVo);
                                List<String> workerList =
                                        assignWorkerMap.get(workerPolicyVo.getProcessTaskStepId());
                                if (CollectionUtils.isNotEmpty(workerList)) {
                                    for (String worker : workerList) {
                                        String[] split = worker.split("#");
                                        assignWorkerVo.setType(split[0]);
                                        assignWorkerVo.setUuid(split[1]);
                                        processTaskMapper.insertProcessTaskAssignWorker(assignWorkerVo);
                                    }
                                } else {
                                    Integer isRequired = workerPolicyVo.getConfigObj().getInteger("isRequired");
                                    if (isRequired != null && isRequired.intValue() == 1) {
                                        ProcessTaskStepVo assignableWorkerStep = processTaskMapper
                                                .getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                                        throw new ProcessTaskRuntimeException(
                                                "指派：" + assignableWorkerStep.getName() + "步骤处理人是必填");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param currentProcessTaskStepVo
     * @param targerProcessTaskStepId
     * @param reason
     * @param ation
     * @Description: 保存步骤提醒
     * @Author: linbq
     * @Date: 2021/1/21 11:30
     * @Params:[currentProcessTaskStepVo, targerProcessTaskStepId, reason, ation]
     * @Returns:int
     */
    @Override
    public int saveStepRemind(ProcessTaskStepVo currentProcessTaskStepVo, Long targerProcessTaskStepId, String reason, IProcessTaskStepRemindType ation) {
        ProcessTaskStepRemindVo processTaskStepRemindVo = new ProcessTaskStepRemindVo();
        processTaskStepRemindVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        processTaskStepRemindVo.setProcessTaskStepId(targerProcessTaskStepId);
        processTaskStepRemindVo.setAction(ation.getValue());
        processTaskStepRemindVo.setFcu(UserContext.get().getUserUuid(true));
        String title = ation.getTitle();
        title = title.replace("processTaskStepName", currentProcessTaskStepVo.getName());
        processTaskStepRemindVo.setTitle(title);
        if (StringUtils.isNotBlank(reason)) {
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(reason);
            processTaskMapper.replaceProcessTaskContent(contentVo);
            processTaskStepRemindVo.setContentHash(contentVo.getHash());
        }
        return processTaskMapper.insertProcessTaskStepRemind(processTaskStepRemindVo);
    }
}
