package codedriver.module.process.api.processtask;


import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.crossover.IProcessTaskStartApiCrossoverService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskStartService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStartApi extends PrivateApiComponentBase implements IProcessTaskStartApiCrossoverService {

    @Resource
    private ProcessTaskStartService processTaskStartService;

    @Override
    public String getToken() {
        return "processtask/start";
    }

    @Override
    public String getName() {
        return "工单步骤开始接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤Id"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "accept,start", isRequired = true, desc = "操作类型")
    })
    @Description(desc = "工单步骤开始接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        processTaskStartService.start(jsonObj);
        return null;
    }

}
