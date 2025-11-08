/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.process.audithandler.core.IProcessTaskAuditDetailType;
import neatlogic.framework.process.audithandler.core.IProcessTaskAuditType;
import neatlogic.framework.process.audithandler.core.ProcessTaskAuditDetailTypeFactory;
import neatlogic.framework.process.audithandler.core.ProcessTaskAuditTypeFactory;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.util.FreemarkerUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Service
public class ProcessTaskAuditThread extends NeatLogicThread {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
    private static ProcessTaskMapper processTaskMapper;
    private static ProcessTaskService processTaskService;

    @Autowired
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }

    @Resource
    public void setProcessTaskService(ProcessTaskService _processTaskService) {
        processTaskService = _processTaskService;
    }

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private IProcessTaskAuditType action;

    public ProcessTaskAuditThread() {
        super("PROCESSTASK-AUDIT");
    }

    public ProcessTaskAuditThread(ProcessTaskStepVo _currentProcessTaskStepVo, IProcessTaskAuditType _action) {
        super("PROCESSTASK-AUDIT-" + _currentProcessTaskStepVo.getId() + "-" + _action.getValue());
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        currentProcessTaskStepVo.setIsAutoGenerateId(false);
        action = _action;
    }

    public static synchronized void audit(ProcessTaskStepVo currentProcessTaskStepVo, IProcessTaskAuditType action) {
        ProcessTaskAuditThread handler = new ProcessTaskAuditThread(currentProcessTaskStepVo, action);
        CachedThreadPool.execute(handler);
    }

    @Override
    public void execute() {
        try {
            /* 活动类型 **/
            JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
            ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
            processTaskStepAuditVo.setAction(action.getValue());
            processTaskStepAuditVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
            processTaskStepAuditVo.setUserUuid(UserContext.get().getUserUuid());// 兼容automatic作业无用户
            processTaskStepAuditVo.setStepStatus(currentProcessTaskStepVo.getStatus());
            processTaskStepAuditVo.setOriginalUser(currentProcessTaskStepVo.getOriginalUser());
            Date operateTime = paramObj.getDate("operateTime");
            if (operateTime == null) {
                operateTime = new Date();
            }
            processTaskStepAuditVo.setActionTime(operateTime);
            String source = paramObj.getString("source");
            if (StringUtils.isNotBlank(source)) {
                processTaskStepAuditVo.setSource(source);
            }
            ProcessTaskStepVo stepVo = null;
            if (currentProcessTaskStepVo.getId() != null) {
                stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                if (stepVo != null) {
                    currentProcessTaskStepVo.setProcessTaskId(stepVo.getProcessTaskId());
                    currentProcessTaskStepVo.setName(stepVo.getName());
                    currentProcessTaskStepVo.setProcessStepUuid(stepVo.getProcessStepUuid());
                    currentProcessTaskStepVo.setStatus(stepVo.getStatus());
                    currentProcessTaskStepVo.setType(stepVo.getType());
                    currentProcessTaskStepVo.setHandler(stepVo.getHandler());
                    currentProcessTaskStepVo.setIsActive(stepVo.getIsActive());
                    currentProcessTaskStepVo.setConfigHash(stepVo.getConfigHash());
                    currentProcessTaskStepVo.setActiveTime(stepVo.getActiveTime());
                    currentProcessTaskStepVo.setStartTime(stepVo.getStartTime());
                    currentProcessTaskStepVo.setEndTime(stepVo.getEndTime());
                    currentProcessTaskStepVo.setError(stepVo.getError());

                    processTaskStepAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                    String processTaskStepName = currentProcessTaskStepVo.getName();
                    paramObj.put("processTaskStepName", processTaskStepName);
                    JSONArray replaceableTextList = processTaskService.getReplaceableTextList(currentProcessTaskStepVo);
                    for (int i = 0; i < replaceableTextList.size(); i++) {
                        JSONObject replaceableText = replaceableTextList.getJSONObject(i);
                        String name = replaceableText.getString("name");
                        String value = replaceableText.getString("value");
                        if (StringUtils.isBlank(value)) {
                            value = replaceableText.getString("text");
                        }
                        paramObj.put(name, value);
                    }
                }
            }
            if (stepVo == null) {
                ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
                if (processTaskVo != null) {
                    paramObj.put("processTaskStepName", processTaskVo.getTitle());
                }
            }
            Long nextStepId = paramObj.getLong("nextStepId");
            if (nextStepId != null) {
                String nextStepName = processTaskMapper.getProcessTaskStepNameById(nextStepId);
                paramObj.put("nextStepName", nextStepName);
            }
            String description = FreemarkerUtil.transform(paramObj, ProcessTaskAuditTypeFactory.getDescription(action.getValue()));
            if (description != null) {
                ProcessTaskContentVo descriptionVo = new ProcessTaskContentVo(description);
                processTaskMapper.insertIgnoreProcessTaskContent(descriptionVo);
                processTaskStepAuditVo.setDescriptionHash(descriptionVo.getHash());
            }
            processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);
            /* 活动内容 **/
            for (IProcessTaskAuditDetailType auditDetailType : ProcessTaskAuditDetailTypeFactory.getAuditDetailTypeList()) {
                String newData = paramObj.getString(auditDetailType.getParamName());
                String oldData = paramObj.getString(auditDetailType.getOldDataParamName());
                if (Objects.equals(oldData, newData)) {
                    continue;
                }

                if (auditDetailType.getNeedCompression()) {
                    if (StringUtils.isNotBlank(newData)) {
                        ProcessTaskContentVo contentVo = new ProcessTaskContentVo(newData);
                        processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
                        newData = contentVo.getHash();
                    }
                    if (StringUtils.isNotBlank(oldData)) {
                        ProcessTaskContentVo contentVo = new ProcessTaskContentVo(oldData);
                        processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
                        oldData = contentVo.getHash();
                    }
                }
                processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), auditDetailType.getValue(), oldData, newData));
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
