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

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.crossover.IProcessTaskStepTaskCompleteApiCrossoverService;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.task.ProcessTaskStepTaskNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandlerUtil;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
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
