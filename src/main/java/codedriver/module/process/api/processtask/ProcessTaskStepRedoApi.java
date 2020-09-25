package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskStepRedoApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/step/redo";
    }

    @Override
    public String getName() {
        return "打回重做步骤";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
        @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "激活步骤Id"),
        @Param(name = "content", type = ApiParamType.STRING, desc = "原因")
    })
    @Description(desc = "打回重做步骤")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo currentProcessTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
        currentProcessTaskStepVo.setParamObj(jsonObj);
        handler.redo(currentProcessTaskStepVo);
        return null;
    }

}
