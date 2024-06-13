package neatlogic.module.process.api.processtask;


import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStartApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/start";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskstartapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskstepid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "accept,start", isRequired = true, desc = "common.actiontype")
    })
    @Description(desc = "nmpap.processtaskstartapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        processTaskService.startProcessTaskStep(jsonObj);
        return null;
    }

}
