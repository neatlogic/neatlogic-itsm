package neatlogic.module.process.api.processtask;


import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepStartIfNecessaryApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/step/start/ifnecessary";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskstepstartifnecessaryapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskstepid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source")
    })
    @Description(desc = "nmpap.processtaskstepstartifnecessaryapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        Map<Long, Set<ProcessTaskOperationType>> operationTypeSetMap = new ProcessAuthManager.Builder()
                .addProcessTaskId(processTaskId)
                .addProcessTaskStepId(processTaskStepId)
                .addOperationType(ProcessTaskOperationType.STEP_START)
                .addOperationType(ProcessTaskOperationType.STEP_ACCEPT)
                .build()
                .getOperateMap();
        Set<ProcessTaskOperationType> processTaskOperationTypeList = operationTypeSetMap.get(processTaskStepId);
        if (CollectionUtils.isNotEmpty(processTaskOperationTypeList)) {
            if (processTaskOperationTypeList.contains(ProcessTaskOperationType.STEP_ACCEPT)) {
                jsonObj.put("action", ProcessTaskOperationType.STEP_ACCEPT.getValue());
                System.out.println("jsonObj = " + jsonObj);
                processTaskService.startProcessTaskStep(jsonObj);
            } else if (processTaskOperationTypeList.contains(ProcessTaskOperationType.STEP_START)) {
                jsonObj.put("action", ProcessTaskOperationType.STEP_START.getValue());
                System.out.println("jsonObj = " + jsonObj);
                processTaskService.startProcessTaskStep(jsonObj);
            }
        }
        return null;
    }

}
