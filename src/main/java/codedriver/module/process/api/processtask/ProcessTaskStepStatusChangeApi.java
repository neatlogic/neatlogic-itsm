/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */


package codedriver.module.process.api.processtask;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskStepFoundMultipleException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskStepStatusChangeApi extends PublicApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "processtask/step/status/change";
    }

    @Override
    public String getName() {
        return "手动更改工单步骤状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id"),
            @Param(name = "processTaskStepName", type = ApiParamType.STRING, desc = "工单步骤名称"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤Id"),
            @Param(name = "status", type = ApiParamType.ENUM, rule = "pending,running,succeed,failed,hang,draft", desc = "工单步骤状态"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "处理人userId")
    })
    @Description(desc = "手动更改工单步骤状态")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        String processTaskStepName = jsonObj.getString("processTaskStepName");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        String status = jsonObj.getString("status");
        String userId = jsonObj.getString("userId");
        if (processTaskId == null && processTaskStepId == null) {
            throw new ParamNotExistsException("processTaskId", "processTaskStepId");
        }
        ProcessTaskStepVo processTaskStep;
        if (processTaskId != null) {
            if (StringUtils.isBlank(processTaskStepName)) {
                throw new ParamNotExistsException("processTaskStepName");
            }
            List<ProcessTaskStepVo> stepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndStepName(new ProcessTaskStepVo(processTaskId, processTaskStepName));
            if (stepList.isEmpty()) {
                throw new ProcessTaskStepNotFoundException(processTaskStepName);
            }
            if (stepList.size() > 1) {
                throw new ProcessTaskStepFoundMultipleException(processTaskStepName);
            }
            processTaskStep = stepList.get(0);
        } else {
            processTaskStep = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStep == null) {
                throw new ProcessTaskStepNotFoundException(processTaskStepId);
            }
        }
        if (StringUtils.isNotBlank(userId)) {
            UserVo user = userMapper.getUserByUserId(userId);
            if (user == null) {
                throw new UserNotFoundException(userId);
            }
            processTaskStep.setOriginalUserVo(user);
        }
        map.get(status).accept(processTaskStep);
        return null;
    }

    static Map<String, Consumer<ProcessTaskStepVo>> map = new HashMap<>();

    @PostConstruct
    private void init() {
        map.put(ProcessTaskStatus.PENDING.getValue(), processTaskStepVo -> {
            if (processTaskStepVo.getOriginalUserVo() == null) {
                throw new ApiRuntimeException("必须指定处理人");
            }
            processTaskMapper.deleteProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue()));
            processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue()));
            processTaskMapper.insertIgnoreProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId()
                    , GroupSearch.USER.getValue(), processTaskStepVo.getOriginalUserVo().getUuid(), ProcessUserType.MAJOR.getValue()));
            processTaskMapper.updateProcessTaskStepStatusByStepId(new ProcessTaskStepVo(processTaskStepVo.getId(), ProcessTaskStatus.PENDING.getValue(), 1));
            processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.RUNNING.getValue()));
        });
        map.put(ProcessTaskStatus.RUNNING.getValue(), processTaskStepVo -> {
            if (processTaskStepVo.getOriginalUserVo() == null) {
                // 不指定处理人时，旧处理人必须存在
                List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
                if (processTaskStepUserList.isEmpty()) {
                    throw new ApiRuntimeException("旧处理人不存在");
                }
                ProcessTaskStepUserVo majorUser = processTaskStepUserList.get(0);
                changeStatus(processTaskStepVo, majorUser.getUserUuid(), majorUser.getUserName());
            } else {
                changeStatus(processTaskStepVo, processTaskStepVo.getOriginalUserVo().getUuid(), processTaskStepVo.getOriginalUserVo().getUserName());
            }
            processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.RUNNING.getValue()));
        });
        map.put(ProcessTaskStatus.SUCCEED.getValue(), processTaskStepVo -> {
            processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue()));
            ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
            processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
            processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
            processTaskStepVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
            processTaskStepVo.setIsActive(2);
            processTaskStepVo.setUpdateEndTime(1);
            processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
            // todo 更改工单状态
            // todo 要不要激活下一步，如果知道激活哪一步？如何激活？
        });
        map.put(ProcessTaskStatus.FAILED.getValue(), processTaskStepVo -> {
            processTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
            processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
            processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.FAILED.getValue()));
        });
        map.put(ProcessTaskStatus.HANG.getValue(), processTaskStepVo -> {
            ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
            processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
            processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
            processTaskStepVo.setIsActive(0);
            processTaskStepVo.setStatus(ProcessTaskStatus.HANG.name());
            processTaskStepVo.setUpdateEndTime(1);
            processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
            processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.HANG.getValue()));
        });
        map.put(ProcessTaskStatus.DRAFT.getValue(), processTaskStepVo -> {
            processTaskStepVo.setIsActive(1);
            processTaskStepVo.setStatus(ProcessTaskStatus.DRAFT.getValue());
            processTaskStepVo.setUpdateActiveTime(1);
            processTaskStepVo.setUpdateStartTime(1);
            processTaskMapper.updateProcessTaskStepStatus(processTaskStepVo);
            processTaskMapper.updateProcessTaskStatus(new ProcessTaskVo(processTaskStepVo.getProcessTaskId(), ProcessTaskStatus.HANG.getValue()));
        });

    }

    /**
     * 更改步骤状态为处理中
     *
     * @param processTaskStep 步骤
     * @param userUuid        处理人uuid
     * @param userName        处理人userName
     */
    private void changeStatus(ProcessTaskStepVo processTaskStep, String userUuid, String userName) {
        processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStep.getId(), ProcessUserType.MAJOR.getValue()));
        processTaskMapper.insertIgnoreProcessTaskStepWorker(new ProcessTaskStepWorkerVo(processTaskStep.getProcessTaskId(), processTaskStep.getId(), GroupSearch.USER.getValue(), userUuid, ProcessUserType.MAJOR.getValue()));
        processTaskMapper.updateProcessTaskStepStatusByStepId(new ProcessTaskStepVo(processTaskStep.getId(), ProcessTaskStatus.RUNNING.getValue(), 1));
        processTaskMapper.updateProcessTaskStepMajorUserAndStatus(new ProcessTaskStepUserVo(processTaskStep.getId(), userUuid, userName, ProcessTaskStepUserStatus.DOING.getValue()));
    }

}
