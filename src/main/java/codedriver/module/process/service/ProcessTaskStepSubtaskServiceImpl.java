package codedriver.module.process.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepSubtaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;

//@Service
@Deprecated
public class ProcessTaskStepSubtaskServiceImpl implements ProcessTaskStepSubtaskService {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IProcessStepHandlerUtil IProcessStepHandlerUtil;

    @Override
    public void createSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
        JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
        Long targetTime = paramObj.getLong("targetTime");
        if (targetTime != null) {
            processTaskStepSubtaskVo.setTargetTime(new Date(targetTime));
        }
        processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
        //插入子任务 
        processTaskStepSubtaskMapper.insertProcessTaskStepSubtask(processTaskStepSubtaskVo);
        paramObj.put("processTaskStepSubtaskId", processTaskStepSubtaskVo.getId());
        String content = paramObj.getString("content");
        paramObj.remove("content");
        processTaskStepSubtaskVo.setContent(content);
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
//        processTaskStepSubtaskMapper.insertProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskOperationType.SUBTASK_CREATE.getValue(), processTaskContentVo.getHash()));

        ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
        if (currentProcessTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
        }
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        if (handler != null) {
            handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());
            //记录活动
            ProcessTaskStepSubtaskVo subtaskVo = new ProcessTaskStepSubtaskVo();
            subtaskVo.setId(processTaskStepSubtaskVo.getId());
            subtaskVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
            subtaskVo.setUserName(processTaskStepSubtaskVo.getUserName());
            subtaskVo.setTargetTime(processTaskStepSubtaskVo.getTargetTime());
            subtaskVo.setContentHash(processTaskContentVo.getHash());
//            paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getParamName(), JSON.toJSONString(subtaskVo));
            currentProcessTaskStepVo.getParamObj().putAll(paramObj);
//            IProcessStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.CREATESUBTASK);
//            currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
//            currentProcessTaskStepVo.setCurrentSubtaskVo(processTaskStepSubtaskVo);
//            IProcessStepHandlerUtil.notify(currentProcessTaskStepVo, SubtaskNotifyTriggerType.CREATESUBTASK);
//            IProcessStepHandlerUtil.action(currentProcessTaskStepVo, SubtaskNotifyTriggerType.CREATESUBTASK);
        } else {
            throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
        }

    }

    @Override
    public void editSubtask(ProcessTaskStepSubtaskVo oldProcessTaskStepSubtask) {
        ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(oldProcessTaskStepSubtask.getProcessTaskStepId());
        if (currentProcessTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(oldProcessTaskStepSubtask.getProcessTaskStepId().toString());
        } else if (currentProcessTaskStepVo.getIsActive().intValue() != 1) {
            throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
        }
        List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(oldProcessTaskStepSubtask.getId());
        for (ProcessTaskStepSubtaskContentVo subtaskContentVo : processTaskStepSubtaskContentList) {
//            if (ProcessTaskOperationType.SUBTASK_CREATE.getValue().equals(subtaskContentVo.getAction())) {
//                oldProcessTaskStepSubtask.setContentHash(subtaskContentVo.getContentHash());
//            }
        }
        JSONObject paramObj = oldProcessTaskStepSubtask.getParamObj();
        String content = paramObj.getString("content");
        paramObj.remove("content");
        ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
        processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
//        processTaskStepSubtaskMapper.updateProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(oldProcessTaskStepSubtask.getId(), ProcessTaskOperationType.SUBTASK_CREATE.getValue(), processTaskContentVo.getHash()));
        ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
        processTaskStepSubtaskVo.setId(oldProcessTaskStepSubtask.getId());
        processTaskStepSubtaskVo.setContentHash(processTaskContentVo.getHash());

        Long targetTime = paramObj.getLong("targetTime");
        if (targetTime != null) {
            processTaskStepSubtaskVo.setTargetTime(new Date(targetTime));
        }

        String workers = paramObj.getString("workerList");
        paramObj.remove("workerList");
        String[] split = workers.split("#");
        UserVo userVo = userMapper.getUserBaseInfoByUuid(split[1]);
        processTaskStepSubtaskVo.setUserUuid(userVo.getUuid());
        processTaskStepSubtaskVo.setUserName(userVo.getUserName());

        processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
        processTaskStepSubtaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);

        if (processTaskStepSubtaskVo.equals(oldProcessTaskStepSubtask)) {//如果子任务信息没有被修改，则不进行下面操作
            return;
        }

        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        if (handler != null) {
            if (!processTaskStepSubtaskVo.getUserUuid().equals(oldProcessTaskStepSubtask.getUserUuid())) {//更新了处理人
                handler.updateProcessTaskStepUserAndWorker(oldProcessTaskStepSubtask.getProcessTaskId(), oldProcessTaskStepSubtask.getProcessTaskStepId());
            }

            //记录活动
            ProcessTaskStepSubtaskVo subtaskVo = new ProcessTaskStepSubtaskVo();
            subtaskVo.setId(processTaskStepSubtaskVo.getId());
            subtaskVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
            subtaskVo.setUserName(processTaskStepSubtaskVo.getUserName());
            subtaskVo.setTargetTime(processTaskStepSubtaskVo.getTargetTime());
            subtaskVo.setContentHash(processTaskStepSubtaskVo.getContentHash());
//            paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getParamName(), JSON.toJSONString(subtaskVo));

            ProcessTaskStepSubtaskVo oldSubtaskVo = new ProcessTaskStepSubtaskVo();
            oldSubtaskVo.setId(oldProcessTaskStepSubtask.getId());
            oldSubtaskVo.setUserUuid(oldProcessTaskStepSubtask.getUserUuid());
            oldSubtaskVo.setUserName(oldProcessTaskStepSubtask.getUserName());
            oldSubtaskVo.setTargetTime(oldProcessTaskStepSubtask.getTargetTime());
            oldSubtaskVo.setContentHash(oldProcessTaskStepSubtask.getContentHash());
//            paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getOldDataParamName(), JSON.toJSONString(oldSubtaskVo));
            currentProcessTaskStepVo.getParamObj().putAll(paramObj);
//            IProcessStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.EDITSUBTASK);
//            currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
            processTaskStepSubtaskVo.setContent(content);
//            currentProcessTaskStepVo.setCurrentSubtaskVo(processTaskStepSubtaskVo);
//            IProcessStepHandlerUtil.notify(currentProcessTaskStepVo, SubtaskNotifyTriggerType.EDITSUBTASK);
//            IProcessStepHandlerUtil.action(currentProcessTaskStepVo, SubtaskNotifyTriggerType.EDITSUBTASK);
        } else {
            throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
        }
    }

    @Override
    public void redoSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
        ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
        if (currentProcessTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
        } else if (currentProcessTaskStepVo.getIsActive().intValue() != 1) {
            throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
        }
        processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
        processTaskStepSubtaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
        JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
        String content = paramObj.getString("content");
        if (StringUtils.isNotBlank(content)) {
            ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
            processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
//            processTaskStepSubtaskMapper.insertProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskOperationType.SUBTASK_REDO.getValue(), processTaskContentVo.getHash()));
        }
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        if (handler != null) {
            handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());
            //记录活动
            currentProcessTaskStepVo.getParamObj().putAll(processTaskStepSubtaskVo.getParamObj());
//            IProcessStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.REDOSUBTASK);
//            currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
            List<ProcessTaskStepSubtaskContentVo> subtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtaskVo.getId());
            for (ProcessTaskStepSubtaskContentVo subtaskContent : subtaskContentList) {
//                if (subtaskContent.getAction().equals(ProcessTaskOperationType.SUBTASK_CREATE.getValue())) {
//                    processTaskStepSubtaskVo.setContent(subtaskContent.getContent());
//                }
            }
//            currentProcessTaskStepVo.setCurrentSubtaskVo(processTaskStepSubtaskVo);
//            IProcessStepHandlerUtil.notify(currentProcessTaskStepVo, SubtaskNotifyTriggerType.REDOSUBTASK);
//            IProcessStepHandlerUtil.action(currentProcessTaskStepVo, SubtaskNotifyTriggerType.REDOSUBTASK);
        } else {
            throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
        }

    }

    @Override
    public void completeSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
        ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
        if (currentProcessTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
        } else if (currentProcessTaskStepVo.getIsActive().intValue() != 1) {
            throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
        }
        processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
        processTaskStepSubtaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
        JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
        String content = paramObj.getString("content");
        if (StringUtils.isNotBlank(content)) {
            ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
            processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
//            processTaskStepSubtaskMapper.insertProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskOperationType.SUBTASK_COMPLETE.getValue(), processTaskContentVo.getHash()));
        }
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
        }
        handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());
        //记录活动
        currentProcessTaskStepVo.getParamObj().putAll(processTaskStepSubtaskVo.getParamObj());
//        IProcessStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.COMPLETESUBTASK);
//        currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
        List<ProcessTaskStepSubtaskContentVo> subtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtaskVo.getId());
        for (ProcessTaskStepSubtaskContentVo subtaskContent : subtaskContentList) {
//            if (subtaskContent.getAction().equals(ProcessTaskOperationType.SUBTASK_CREATE.getValue())) {
//                processTaskStepSubtaskVo.setContent(subtaskContent.getContent());
//            }
        }
//        currentProcessTaskStepVo.setCurrentSubtaskVo(processTaskStepSubtaskVo);
//        IProcessStepHandlerUtil.notify(currentProcessTaskStepVo, SubtaskNotifyTriggerType.COMPLETESUBTASK);
//        IProcessStepHandlerUtil.action(currentProcessTaskStepVo, SubtaskNotifyTriggerType.COMPLETESUBTASK);

        /** 判断当前步骤的所有子任务是否都完成了 **/
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
        if(CollectionUtils.isNotEmpty(processTaskStepSubtaskList)){
            for (ProcessTaskStepSubtaskVo subtask : processTaskStepSubtaskList) {
                if (!subtask.getStatus().equals(ProcessTaskStatus.SUCCEED.getValue()) && !subtask.getStatus().equals(ProcessTaskStatus.ABORTED.getValue())) {
                    return;
                }
            }
//            IProcessStepHandlerUtil.notify(currentProcessTaskStepVo, SubtaskNotifyTriggerType.COMPLETEALLSUBTASK);
//            IProcessStepHandlerUtil.action(currentProcessTaskStepVo, SubtaskNotifyTriggerType.COMPLETEALLSUBTASK);
        }
    }

    @Override
    public void abortSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
        ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
        if (currentProcessTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
        } else if (currentProcessTaskStepVo.getIsActive().intValue() != 1) {
            throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
        }
        processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.ABORTED.getValue());
        processTaskStepSubtaskVo.setCancelUser(UserContext.get().getUserUuid(true));
        processTaskStepSubtaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);

        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        if (handler != null) {
            handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());
            //记录活动
            currentProcessTaskStepVo.getParamObj().putAll(processTaskStepSubtaskVo.getParamObj());
//            IProcessStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.ABORTSUBTASK);
//            currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
            List<ProcessTaskStepSubtaskContentVo> subtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtaskVo.getId());
            for (ProcessTaskStepSubtaskContentVo subtaskContent : subtaskContentList) {
//                if (subtaskContent.getAction().equals(ProcessTaskOperationType.SUBTASK_CREATE.getValue())) {
//                    processTaskStepSubtaskVo.setContent(subtaskContent.getContent());
//                }
            }
//            currentProcessTaskStepVo.setCurrentSubtaskVo(processTaskStepSubtaskVo);
//            IProcessStepHandlerUtil.notify(currentProcessTaskStepVo, SubtaskNotifyTriggerType.ABORTSUBTASK);
//            IProcessStepHandlerUtil.action(currentProcessTaskStepVo, SubtaskNotifyTriggerType.ABORTSUBTASK);
        } else {
            throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
        }
    }

    @Override
    public List<ProcessTaskStepSubtaskContentVo> commentSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
        ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
        if (currentProcessTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
        } else if (currentProcessTaskStepVo.getIsActive().intValue() != 1) {
            throw new ProcessTaskRuntimeException("步骤未激活，不能回复子任务");
        }
        JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
        String content = paramObj.getString("content");
        if (StringUtils.isNotBlank(content)) {
            ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
            processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
//            ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskOperationType.SUBTASK_COMMENT.getValue(), processTaskContentVo.getHash());
//            processTaskStepSubtaskMapper.insertProcessTaskStepSubtaskContent(processTaskStepSubtaskContentVo);
        }
        List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtaskVo.getId());
        Iterator<ProcessTaskStepSubtaskContentVo> iterator = processTaskStepSubtaskContentList.iterator();
        while (iterator.hasNext()) {
            ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = iterator.next();
            if (processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
//                if (ProcessTaskOperationType.SUBTASK_CREATE.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
//                    processTaskStepSubtaskVo.setContent(processTaskStepSubtaskContentVo.getContent());
//                    iterator.remove();
//                }
            }
        }
        return processTaskStepSubtaskContentList;
    }

    @Override
    public List<ProcessTaskStepSubtaskVo> getProcessTaskStepSubtaskListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
        for (ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
            List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtask.getId());
            Iterator<ProcessTaskStepSubtaskContentVo> iterator = processTaskStepSubtaskContentList.iterator();
            while (iterator.hasNext()) {
                ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = iterator.next();
                if (processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
//                    if (ProcessTaskOperationType.SUBTASK_CREATE.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
//                        processTaskStepSubtask.setContent(processTaskStepSubtaskContentVo.getContent());
//                        iterator.remove();
//                    }
                }
            }
            processTaskStepSubtask.setContentList(processTaskStepSubtaskContentList);
        }
        return processTaskStepSubtaskList;
    }
}
