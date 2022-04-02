/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.notify.core.INotifyTriggerType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyReceiverVo;
import codedriver.framework.notify.dto.ParamMappingVo;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ConditionProcessTaskOptions;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.util.NotifyPolicyUtil;
import codedriver.module.process.message.handler.ProcessTaskMessageHandler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessTaskNotifyThread extends CodeDriverThread {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static SelectContentByHashMapper selectContentByHashMapper;
    private static ProcessStepHandlerMapper processStepHandlerMapper;
    private static NotifyMapper notifyMapper;
    private static FileMapper fileMapper;
    private static ProcessTaskService processTaskService;

    @Autowired
    public void setProcessTaskService(ProcessTaskService _processTaskService) {
        processTaskService = _processTaskService;
    }

    @Autowired
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }

    @Autowired
    public void setSelectContentByHashMapper(SelectContentByHashMapper _selectContentByHashMapper) {
        selectContentByHashMapper = _selectContentByHashMapper;
    }

    @Autowired
    public void setProcessStepHandlerMapper(ProcessStepHandlerMapper _processStepHandlerMapper) {
        processStepHandlerMapper = _processStepHandlerMapper;
    }

    @Autowired
    public void setNotifyMapper(NotifyMapper _notifyMapper) {
        notifyMapper = _notifyMapper;
    }

    @Autowired
    public void setFileMapper(FileMapper _fileMapper) {
        fileMapper = _fileMapper;
    }

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private INotifyTriggerType notifyTriggerType;

    public ProcessTaskNotifyThread() {
        super("PROCESSTASK-NOTIFY");
    }

    public ProcessTaskNotifyThread(ProcessTaskStepVo _currentProcessTaskStepVo, INotifyTriggerType _trigger) {
        super("PROCESSTASK-NOTIFY" + (_currentProcessTaskStepVo != null ? "-" + _currentProcessTaskStepVo.getId() : ""));
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        notifyTriggerType = _trigger;
    }

    @Override
    protected void execute() {
        try {
            JSONObject notifyPolicyConfig = null;
            Long policyId = null;
            if (notifyTriggerType instanceof ProcessTaskNotifyTriggerType) {
                /** 获取工单配置信息 **/
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(currentProcessTaskStepVo.getProcessTaskId());
                String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                notifyPolicyConfig = (JSONObject) JSONPath.read(config, "process.processConfig.notifyPolicyConfig");
                if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                    policyId = notifyPolicyConfig.getLong("policyId");
                }
            } else {
                /* 获取步骤配置信息 **/
                ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(stepVo.getHandler());
                if (processStepUtilHandler == null) {
                    throw new ProcessStepUtilHandlerNotFoundException(stepVo.getHandler());
                }
                String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
                notifyPolicyConfig = (JSONObject) JSONPath.read(stepConfig, "notifyPolicyConfig");
                if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                    policyId = notifyPolicyConfig.getLong("policyId");
                }
                if (policyId == null) {
                    String processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerConfigByHandler(stepVo.getHandler());
                    JSONObject globalConfig = null;
                    if (StringUtils.isNotBlank(processStepHandlerConfig)) {
                        globalConfig = JSONObject.parseObject(processStepHandlerConfig);
                    }
                    globalConfig = processStepUtilHandler.makeupConfig(globalConfig);
                    notifyPolicyConfig = globalConfig.getJSONObject("notifyPolicyConfig");
                    if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                        policyId = notifyPolicyConfig.getLong("policyId");
                    }
                }
            }

            /* 从步骤配置信息中获取通知策略信息 **/
            if (policyId != null) {
                NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
                if (notifyPolicyVo != null) {
                    NotifyPolicyConfigVo policyConfig = notifyPolicyVo.getConfig();
                    if (policyConfig != null) {
                        JSONObject conditionParamData = ProcessTaskConditionFactory.getConditionParamData(Arrays.stream(ConditionProcessTaskOptions.values()).map(ConditionProcessTaskOptions::getValue).collect(Collectors.toList()), currentProcessTaskStepVo);
//                        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
//                        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
//                        processTaskVo.setCurrentProcessTaskStep(processTaskService.getCurrentProcessTaskStepDetail(currentProcessTaskStepVo));
//                        JSONObject conditionParamData = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
//                        JSONObject templateParamData = ProcessTaskUtil.getProcessTaskParamData(processTaskVo);
                        Map<String, List<NotifyReceiverVo>> receiverMap = new HashMap<>();
                        processTaskService.getReceiverMap(currentProcessTaskStepVo, receiverMap);
                        /* 参数映射列表 **/
                        List<ParamMappingVo> paramMappingList = new ArrayList<>();
                        JSONArray paramMappingArray = notifyPolicyConfig.getJSONArray("paramMappingList");
                        if (CollectionUtils.isNotEmpty(paramMappingArray)) {
                            paramMappingList = paramMappingArray.toJavaList(ParamMappingVo.class);
                        }
                        List<FileVo> fileList = fileMapper.getFileListByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                        if (CollectionUtils.isNotEmpty(fileList)) {
                            fileList = fileList.stream().filter(o -> o.getSize() <= 10 * 1024 * 1024).collect(Collectors.toList());
                        }
                        String notifyPolicyHandler = notifyPolicyVo.getHandler();
                        NotifyPolicyUtil.execute(notifyPolicyHandler, notifyTriggerType, ProcessTaskMessageHandler.class, policyConfig, paramMappingList, conditionParamData, receiverMap, currentProcessTaskStepVo, fileList);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("通知失败：" + ex.getMessage(), ex);
        }
    }

}
