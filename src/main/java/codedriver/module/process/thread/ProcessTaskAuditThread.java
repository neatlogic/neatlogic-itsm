package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.audithandler.core.IProcessTaskAuditDetailType;
import codedriver.framework.process.audithandler.core.IProcessTaskAuditType;
import codedriver.framework.process.audithandler.core.ProcessTaskAuditDetailTypeFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Autowired
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }

    private ProcessTaskStepVo currentProcessTaskStepVo;
    private IProcessTaskAuditType action;

    public ProcessTaskAuditThread(){}
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
        Thread.currentThread()
                .setName("PROCESSTASK-AUDIT-" + currentProcessTaskStepVo.getId() + "-" + action.getValue());
        try {
            ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
            processTaskStepAuditVo.setAction(action.getValue());
            processTaskStepAuditVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
            processTaskStepAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
            processTaskStepAuditVo.setUserVo(new UserVo(UserContext.get().getUserUuid()));// 兼容automatic作业无用户
            processTaskStepAuditVo.setStepStatus(currentProcessTaskStepVo.getStatus());
            processTaskStepAuditVo.setOriginalUser(currentProcessTaskStepVo.getOriginalUser());
            processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);
            JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
            if (MapUtils.isNotEmpty(paramObj)) {
                for (IProcessTaskAuditDetailType auditDetailType : ProcessTaskAuditDetailTypeFactory
                        .getAuditDetailTypeList()) {
                    String newDataHash = null;
                    String newData = paramObj.getString(auditDetailType.getParamName());
                    if (StringUtils.isNotBlank(newData)) {
                        ProcessTaskContentVo contentVo = new ProcessTaskContentVo(newData);
                        processTaskMapper.replaceProcessTaskContent(contentVo);
                        newDataHash = contentVo.getHash();
                    }
                    String oldDataHash = paramObj.getString(auditDetailType.getOldDataParamName());
                    if (!Objects.equals(oldDataHash, newDataHash)) {
                        processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(
                                processTaskStepAuditVo.getId(), auditDetailType.getValue(), oldDataHash, newDataHash));
                    }
                }
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }
}
