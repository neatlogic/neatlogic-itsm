package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskNextStepListApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/nextstep/list";
    }

    @Override
    public String getName() {
        return "下一可流转步骤列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
        @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "当前步骤Id"),
        @Param(name = "action", type = ApiParamType.ENUM, rule = "complete,back", desc = "操作类型"),})
    @Output({@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "下一可流转步骤列表")})
    @Description(desc = "下一可流转步骤列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskOperationType operationType = ProcessTaskOperationType.STEP_COMPLETE;
        String action = jsonObj.getString("action");
        if (ProcessTaskOperationType.STEP_BACK.getValue().equals(action)) {
            operationType = ProcessTaskOperationType.STEP_BACK;
        }

        new ProcessAuthManager.StepOperationChecker(processTaskStepId, operationType).build()
            .checkAndNoPermissionThrowException();

        if (operationType == ProcessTaskOperationType.STEP_COMPLETE) {
            return processTaskService.getForwardNextStepListByProcessTaskStepId(processTaskStepId);
        } else {
            return processTaskService.getBackwardNextStepListByProcessTaskStepId(processTaskStepId);
        }
    }

}
