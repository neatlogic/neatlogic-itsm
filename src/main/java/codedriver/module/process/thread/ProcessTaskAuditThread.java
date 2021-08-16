package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.process.audithandler.core.IProcessTaskAuditDetailType;
import codedriver.framework.process.audithandler.core.IProcessTaskAuditType;
import codedriver.framework.process.audithandler.core.ProcessTaskAuditDetailTypeFactory;
import codedriver.framework.process.audithandler.core.ProcessTaskAuditTypeFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.framework.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Title: AuditHandler
 * @Package codedriver.module.process.thread
 * @Description: TODO
 * @Author: linbq
 * @Date: 2021/1/20 17:29
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class ProcessTaskAuditThread extends CodeDriverThread {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskActionThread.class);
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
    }

    public ProcessTaskAuditThread(ProcessTaskStepVo _currentProcessTaskStepVo, IProcessTaskAuditType _action) {
        currentProcessTaskStepVo = _currentProcessTaskStepVo;
        action = _action;
    }

    public static synchronized void audit(ProcessTaskStepVo currentProcessTaskStepVo, IProcessTaskAuditType action) {
        ProcessTaskAuditThread handler = new ProcessTaskAuditThread(currentProcessTaskStepVo, action);
        CommonThreadPool.execute(handler);
    }

    @Override
    public void execute() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName("PROCESSTASK-AUDIT-" + currentProcessTaskStepVo.getId() + "-" + action.getValue());
        try {
            /** 活动类型 **/
            JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
            ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
            processTaskStepAuditVo.setAction(action.getValue());
            processTaskStepAuditVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
            processTaskStepAuditVo.setUserUuid(UserContext.get().getUserUuid());// 兼容automatic作业无用户
            processTaskStepAuditVo.setStepStatus(currentProcessTaskStepVo.getStatus());
            processTaskStepAuditVo.setOriginalUser(currentProcessTaskStepVo.getOriginalUser());
            if (currentProcessTaskStepVo.getId() != null) {
                processTaskStepAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                String processTaskStepName = currentProcessTaskStepVo.getName();
                if (StringUtils.isBlank(processTaskStepName)) {
                    processTaskStepName = processTaskMapper.getProcessTaskStepNameById(currentProcessTaskStepVo.getId());
                }
                paramObj.put("processTaskStepName", processTaskStepName);
                String configHash = currentProcessTaskStepVo.getConfigHash();
                if (StringUtils.isBlank(configHash)) {
                    ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                    if (processTaskStepVo != null) {
                        currentProcessTaskStepVo = processTaskStepVo;
                    }
                }
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
            } else {
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
            /** 活动内容 **/
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
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }
}
