/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.processtask.task;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepTaskUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import neatlogic.framework.process.exception.processtask.task.ProcessTaskStepTaskNotFoundException;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
    IProcessStepHandlerUtil processStepHandlerUtil;
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
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
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
        paramObj.put("source", jsonObj.getString("source"));
        processTaskStepVo.getParamObj().putAll(paramObj);
        processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.DELETETASK);
        stepTaskVo.setStepTaskUserVoList(processTaskStepTaskUserList);
        processTaskStepVo.setProcessTaskStepTaskVo(stepTaskVo);
        processStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.DELETETASK);
        processStepHandlerUtil.action(processTaskStepVo, ProcessTaskStepTaskNotifyTriggerType.DELETETASK);
        return null;
    }
}
