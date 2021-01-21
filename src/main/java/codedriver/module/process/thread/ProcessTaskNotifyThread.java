package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.notify.core.INotifyTriggerType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyReceiverVo;
import codedriver.framework.notify.dto.ParamMappingVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskStepNotifyPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.module.process.message.handler.ProcessTaskMessageHandler;
import codedriver.framework.process.notify.constvalue.TaskNotifyTriggerType;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.util.NotifyPolicyUtil;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: NotifyHandler
 * @Package codedriver.module.process.thread
 * @Description: TODO
 * @Author: linbq
 * @Date: 2021/1/20 17:21
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class ProcessTaskNotifyThread extends CodeDriverThread {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static SelectContentByHashMapper selectContentByHashMapper;
    private static ProcessStepHandlerMapper processStepHandlerMapper;
    private static NotifyMapper notifyMapper;
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

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private INotifyTriggerType notifyTriggerType;

    public ProcessTaskNotifyThread(){}
    public ProcessTaskNotifyThread(ProcessTaskStepVo _currentProcessTaskStepVo, INotifyTriggerType _trigger) {
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        notifyTriggerType = _trigger;
        if (_currentProcessTaskStepVo != null) {
            this.setThreadName("PROCESSTASK-NOTIFY-" + _currentProcessTaskStepVo.getId());
        }
    }

    @Override
    protected void execute() {
        try {
            JSONObject notifyPolicyConfig = null;
            if (notifyTriggerType instanceof TaskNotifyTriggerType) {
                /** 获取工单配置信息 **/
                ProcessTaskVo processTaskVo =
                        processTaskMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
                String config =
                        selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                notifyPolicyConfig = (JSONObject) JSONPath.read(config, "process.processConfig.notifyPolicyConfig");
            } else {
                /** 获取步骤配置信息 **/
                ProcessTaskStepVo stepVo =
                        processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(stepVo.getHandler());
                if (processStepUtilHandler == null) {
                    throw new ProcessStepUtilHandlerNotFoundException(stepVo.getHandler());
                }
                String stepConfig =
                        selectContentByHashMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
                currentProcessTaskStepVo.setConfig(stepConfig);
                notifyPolicyConfig = (JSONObject)JSONPath.read(stepConfig, "notifyPolicyConfig");
                if (MapUtils.isEmpty(notifyPolicyConfig)) {
                    ProcessStepHandlerVo processStepHandlerVo =
                            processStepHandlerMapper.getProcessStepHandlerByHandler(stepVo.getHandler());
                    JSONObject globalConfig = processStepUtilHandler
                            .makeupConfig(processStepHandlerVo != null ? processStepHandlerVo.getConfig() : null);
                    notifyPolicyConfig =
                            (JSONObject)JSONPath.read(JSON.toJSONString(globalConfig), "notifyPolicyConfig");
                }
            }

            /** 从步骤配置信息中获取通知策略信息 **/
            if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                Long policyId = notifyPolicyConfig.getLong("policyId");
                if (policyId != null) {
                    NotifyPolicyConfigVo policyConfig = null;
                    ProcessTaskStepNotifyPolicyVo processTaskStepNotifyPolicyVo =
                            new ProcessTaskStepNotifyPolicyVo();
                    processTaskStepNotifyPolicyVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                    processTaskStepNotifyPolicyVo.setPolicyId(policyId);
                    processTaskStepNotifyPolicyVo =
                            processTaskMapper.getProcessTaskStepNotifyPolicy(processTaskStepNotifyPolicyVo);
                    if (processTaskStepNotifyPolicyVo != null) {
                        policyConfig = JSON.parseObject(processTaskStepNotifyPolicyVo.getPolicyConfig(),
                                NotifyPolicyConfigVo.class);
                    } else {
                        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
                        if (notifyPolicyVo != null) {
                            policyConfig = notifyPolicyVo.getConfig();
                        }
                    }
                    ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
                    processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
                    processTaskVo.setCurrentProcessTaskStep(processTaskService.getCurrentProcessTaskStepDetail(currentProcessTaskStepVo));
                    JSONObject conditionParamData = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
                    JSONObject templateParamData = ProcessTaskUtil.getProcessTaskParamData(processTaskVo);
                    Map<String, List<NotifyReceiverVo>> receiverMap = new HashMap<>();
                    processTaskService.getReceiverMap(currentProcessTaskStepVo, receiverMap);
                    /** 参数映射列表 **/
                    List<ParamMappingVo> paramMappingList =
                            JSON.parseArray(JSON.toJSONString(notifyPolicyConfig.getJSONArray("paramMappingList")),
                                    ParamMappingVo.class);
                    NotifyPolicyUtil.execute(notifyTriggerType, ProcessTaskMessageHandler.class, policyConfig, paramMappingList, templateParamData,
                            conditionParamData, receiverMap);
                }
            }
        } catch (Exception ex) {
            logger.error("通知失败：" + ex.getMessage(), ex);
        }
    }

}
