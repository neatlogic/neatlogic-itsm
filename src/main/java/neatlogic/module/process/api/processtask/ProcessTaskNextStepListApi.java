package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.AssignableWorkerStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AuthAction(action = PROCESS_BASE.class)
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
        return "nmpap.processtasknextsteplistapi.getname";
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtasknextsteplistapi.input.param.desc.processtaskid"),
        @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtasknextsteplistapi.input.param.desc.processtaskstepid"),
        @Param(name = "action", type = ApiParamType.ENUM, rule = "complete,back", desc = "nmpap.processtasknextsteplistapi.input.param.desc.action"),})
    @Output({@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "nmpap.processtasknextsteplistapi.output.param.desc.return.name")})
    @Description(desc = "nmpap.processtasknextsteplistapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        IOperationType operationType = ProcessTaskStepOperationType.STEP_COMPLETE;
        String action = jsonObj.getString("action");
        if (ProcessTaskStepOperationType.STEP_BACK.getValue().equals(action)) {
            operationType = ProcessTaskStepOperationType.STEP_BACK;
        }

        new ProcessAuthManager.StepOperationChecker(processTaskStepId, operationType).build()
            .checkAndNoPermissionThrowException();
        Map<Long, List<AssignableWorkerStepVo>> assignableWorkerStepMap = processTaskService.getAssignableWorkerStepMap(processTaskVo.getCurrentProcessTaskStep());
        List<ProcessTaskStepVo> nextStepList = null;
        if (operationType == ProcessTaskStepOperationType.STEP_COMPLETE) {
            nextStepList =  processTaskService.getForwardNextStepListByProcessTaskStepId(processTaskVo.getCurrentProcessTaskStep());
        } else {
            nextStepList =  processTaskService.getBackwardNextStepListByProcessTaskStepId(processTaskVo.getCurrentProcessTaskStep());
        }
        if (CollectionUtils.isEmpty(nextStepList)) {
            return nextStepList;
        }
        for (ProcessTaskStepVo nextStepVo : nextStepList) {
            List<AssignableWorkerStepVo> assignableWorkerStepList = assignableWorkerStepMap.get(nextStepVo.getId());
            if (CollectionUtils.isNotEmpty(assignableWorkerStepList)) {
                nextStepVo.setAssignableWorkerStepList(assignableWorkerStepList);
            }
        }
        return nextStepList;
    }

}
