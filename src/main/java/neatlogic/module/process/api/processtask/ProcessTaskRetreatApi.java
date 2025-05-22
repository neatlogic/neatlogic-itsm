package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.crossover.IProcessTaskRetreatApiCrossoverService;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskRetreatApi extends PrivateApiComponentBase implements IProcessTaskRetreatApiCrossoverService {

    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/retreat";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskretreatapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskstepid"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "common.source"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "common.content")
    })
    @Description(desc = "nmpap.processtaskretreatapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        processTaskStepVo.getParamObj().putAll(jsonObj);
        handler.retreat(processTaskStepVo);
        return null;
    }

}
