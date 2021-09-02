/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.task;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepUnRunningException;
import codedriver.framework.process.exception.processtask.task.ProcessTaskStepTaskNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskStepTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

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
    ProcessTaskStepTaskService processTaskStepTaskService;

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
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "任务id"),
    })
    @Output({@Param(name = "Return", type = ApiParamType.LONG, desc = "任务id")})
    @Description(desc = "任务删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskStepTaskId = jsonObj.getLong("id");
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskById(processTaskStepTaskId);
        if (stepTaskVo == null) {
            throw new ProcessTaskStepTaskNotFoundException(processTaskStepTaskId.toString());
        }
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(stepTaskVo.getProcessTaskStepId());
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(stepTaskVo.getProcessTaskStepId().toString());
        }
        if (Objects.equals(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo.getStatus())) {
            throw new ProcessTaskStepUnRunningException();
        }
        processTaskStepTaskMapper.deleteTaskById(processTaskStepTaskId);
        processTaskStepTaskMapper.deleteTaskUserByTaskId(processTaskStepTaskId);
        processTaskStepTaskMapper.deleteTaskUserContentByTaskId(processTaskStepTaskId);
        //TODO 活动&通知
        return null;
    }
}
