package codedriver.module.process.api.process;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessStepHandlerListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "process/step/handler/list";
    }

    @Override
    public String getName() {
        return "流程组件列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "Retrun", explode = ProcessStepHandlerVo[].class, desc = "流程组件列表")})
    @Description(desc = "流程组件列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ProcessStepHandlerVo> processStepHandlerVoList = ProcessStepHandlerFactory.getActiveProcessStepHandler();
        for (ProcessStepHandlerVo processStepHandlerVo : processStepHandlerVoList) {
            IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(processStepHandlerVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processStepHandlerVo.getHandler());
            }
            processStepHandlerVo.setConfig(JSONObject.toJSONString(processStepUtilHandler.regulateProcessStepConfig(null)));
        }
        return processStepHandlerVoList;
    }
}
