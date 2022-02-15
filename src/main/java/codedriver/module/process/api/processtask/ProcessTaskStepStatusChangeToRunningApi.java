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
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
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

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskStepStatusChangeToRunningApi extends PublicApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "processtask/step/status/changetorunning";
    }

    @Override
    public String getName() {
        return "更改工单步骤状态为处理中";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤Id"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "处理人userId")
    })
    @Description(desc = "更改工单步骤状态为处理中")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        String userId = jsonObj.getString("userId");
        ProcessTaskStepVo processTaskStep = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        if (processTaskStep == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepId);
        }
        UserVo user = null;
        if (StringUtils.isNotBlank(userId)) {
            user = userMapper.getUserByUserId(userId);
            if (user == null) {
                throw new UserNotFoundException(userId);
            }
        }
        if (user != null) {
            changeStatus(processTaskStep, user.getUuid(), user.getUserName());
        } else {
            // 不指定处理人时，旧处理人必须存在
            List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
            if (processTaskStepUserList.isEmpty()) {
                throw new ApiRuntimeException("旧处理人不存在");
            }
            ProcessTaskStepUserVo majorUser = processTaskStepUserList.get(0);
            changeStatus(processTaskStep, majorUser.getUserUuid(), majorUser.getUserName());
        }
        return null;
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
