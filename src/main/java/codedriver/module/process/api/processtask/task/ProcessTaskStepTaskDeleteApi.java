/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.task;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepTaskUserVo;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.exception.processtask.task.ProcessTaskStepTaskNotFoundException;
import codedriver.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lvzk
 * @since 2021/8/31 11:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskDeleteApi extends PrivateApiComponentBase {
    @Resource
    ProcessTaskStepTaskMapper processTaskStepTaskMapper;
    @Resource
    ProcessTaskMapper processTaskMapper;
    @Resource
    IProcessStepHandlerUtil IProcessStepHandlerUtil;
    @Resource
    ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/step/task/delete";
    }

    @Override
    public String getName() {
        return "删除任务";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskStepTaskId", type = ApiParamType.LONG, isRequired = true, desc = "任务id"),
    })
    @Output({})
    @Description(desc = "任务删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskStepTaskId = jsonObj.getLong("processTaskStepTaskId");
        processTaskStepTaskMapper.getStepTaskLockById(processTaskStepTaskId);
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskDetailById(processTaskStepTaskId);
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(processTaskStepTaskId.toString());
        }
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(stepTaskVo.getProcessTaskStepId());
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(stepTaskVo.getProcessTaskStepId().toString());
        }
        //校验执行权限
        new ProcessAuthManager.StepOperationChecker(processTaskStepVo.getId(), ProcessTaskOperationType.TASK_DELETE)
                .build()
                .checkAndNoPermissionThrowException();
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskStepVo.getProcessTaskId());
        List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = processTaskStepTaskMapper.getStepTaskUserListByStepTaskId(stepTaskVo.getId());

        processTaskStepTaskMapper.deleteTaskById(processTaskStepTaskId);
        processTaskStepTaskMapper.deleteTaskUserByTaskId(processTaskStepTaskId);
        processTaskStepTaskMapper.deleteTaskUserContentByTaskId(processTaskStepTaskId);
        processTaskStepTaskMapper.deleteProcessTaskStepTaskUserAgentByStepTaskId(processTaskStepTaskId);
//        processTaskService.refreshStepMinorWorker(processTaskStepVo, new ProcessTaskStepTaskVo(processTaskStepTaskId));
//        processTaskService.refreshStepMinorUser(processTaskStepVo, new ProcessTaskStepTaskVo(processTaskStepTaskId));
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        handler.updateProcessTaskStepUserAndWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());
        //活动参数
        JSONObject paramObj = new JSONObject();
        paramObj.put("replaceable_task", stepTaskVo.getTaskConfigName());
        processTaskStepVo.getParamObj().putAll(paramObj);
        IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.DELETETASK);
        stepTaskVo.setStepTaskUserVoList(processTaskStepTaskUserList);
        processTaskStepVo.setProcessTaskStepTaskVo(stepTaskVo);
        IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.DELETETASK);
        IProcessStepHandlerUtil.action(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.DELETETASK);
        return null;
    }
}
