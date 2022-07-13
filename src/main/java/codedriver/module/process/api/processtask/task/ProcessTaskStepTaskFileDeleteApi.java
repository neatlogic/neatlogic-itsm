/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.task;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.crossover.IProcessTaskStepTaskCompleteApiCrossoverService;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.task.ProcessTaskStepTaskNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.module.process.service.ProcessTaskStepTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author linbq
 * @since 2022/5/31 11:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskFileDeleteApi extends PrivateApiComponentBase {
    @Resource
    private FileMapper fileMapper;
    @Resource
    private ProcessTaskStepTaskMapper processTaskStepTaskMapper;
    @Resource
    private ProcessTaskStepTaskService processTaskStepTaskService;
    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private ProcessTaskService processTaskService;
    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/step/task/file/delete";
    }

    @Override
    public String getName() {
        return "任务删除附件接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "任务id"),
            @Param(name = "fileId", isRequired = true, type = ApiParamType.LONG, desc = "附件id"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
    })
    @Output({})
    @Description(desc = "任务删除附件接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        Long fileId = jsonObj.getLong("fileId");
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskDetailById(id);
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(id);
        }
        FileVo fileVo = fileMapper.getFileById(fileId);
        if (fileVo == null) {
            throw new FileNotFoundException(fileId);
        }
        Long processTaskId = stepTaskVo.getProcessTaskId();
        Long processTaskStepId = stepTaskVo.getProcessTaskStepId();
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskId);
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();

        List<ProcessTaskStepTaskUserVo> canHandleStepTaskUserList = new ArrayList<>();
        List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = processTaskStepTaskMapper.getStepTaskUserListByStepTaskId(id);
        for (ProcessTaskStepTaskUserVo oldProcessTaskStepTaskUserVo : processTaskStepTaskUserList) {
            if (Objects.equals(oldProcessTaskStepTaskUserVo.getIsDelete(), 1)) {
                continue;
            }
            try {
                Long stepTaskUserId = oldProcessTaskStepTaskUserVo.getId();
                processTaskStepTaskService.checkIsReplyable(processTaskVo, processTaskStepVo, oldProcessTaskStepTaskUserVo.getUserUuid(), stepTaskUserId);
                oldProcessTaskStepTaskUserVo.setOriginalUserUuid(UserContext.get().getUserUuid());
                canHandleStepTaskUserList.add(oldProcessTaskStepTaskUserVo);
                processTaskStepTaskService.stepMinorUserRegulate(oldProcessTaskStepTaskUserVo);

                //删除附件
                ProcessTaskStepTaskUserFileVo processTaskStepTaskUserFileVo = new ProcessTaskStepTaskUserFileVo(id, oldProcessTaskStepTaskUserVo.getId(), fileId);
                processTaskStepTaskMapper.deleteProcessTaskStepTaskUserFile(processTaskStepTaskUserFileVo);
                if (!Objects.equals(oldProcessTaskStepTaskUserVo.getUserUuid(), UserContext.get().getUserUuid())) {
                    ProcessTaskStepTaskUserVo processTaskStepTaskUserVo = new ProcessTaskStepTaskUserVo();
                    processTaskStepTaskUserVo.setId(stepTaskUserId);
                    processTaskStepTaskUserVo.setUserUuid(UserContext.get().getUserUuid());
                    processTaskStepTaskUserVo.setStatus(oldProcessTaskStepTaskUserVo.getStatus());
                    processTaskStepTaskMapper.updateTaskUserById(processTaskStepTaskUserVo);
                }
            } catch (ProcessTaskPermissionDeniedException processTaskPermissionDeniedException) {
            }
        }
        //活动参数
        JSONObject paramObj = new JSONObject();
        paramObj.put("replaceable_task", stepTaskVo.getTaskConfigName());
        paramObj.put(ProcessTaskAuditDetailType.FILE.getParamName(), Arrays.asList(fileId));
        paramObj.put("source", jsonObj.getString("source"));
        processTaskStepVo.getParamObj().putAll(paramObj);
//        stepTaskVo.setStepTaskUserVoList(canHandleStepTaskUserList);
//        processTaskStepVo.setProcessTaskStepTaskVo(stepTaskVo);

        processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.DELETETASKFILE);

        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        handler.updateProcessTaskStepUserAndWorker(processTaskId, processTaskStepId);
        return null;
    }
}
