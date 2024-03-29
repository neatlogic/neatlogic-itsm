package neatlogic.module.process.api.processtask.tmp;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessStepHandlerVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerTypeFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskMobileisFitApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/mobile/isfit";
    }

    @Override
    public String getName() {
        return "查看工单是否支持移动端";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.BOOLEAN, desc = "工单是否支持移动端，1：支持，0：不支持;不支持则移动端提示不支持")
    })
    @Description(desc = "临时屏蔽移动端工单查看处理接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject result = new JSONObject();
        result.put("isfit", true);
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskCurrentStepByProcessTaskId(jsonObj.getLong("processTaskId"));
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            String handler = processTaskStepVo.getHandler().toLowerCase();
            List<ProcessStepHandlerVo> processStepHandlerVos = ProcessStepHandlerFactory.getActiveProcessStepHandler();
            if (processStepHandlerVos.stream().noneMatch(o -> Objects.equals(o.getHandler(), handler) && o.getFitMobile())) {
                result.put("isfit", false);
                result.put("msg", String.format("抱歉！移动端暂时不支持处理含有‘%s’步骤节点的工单", ProcessStepHandlerTypeFactory.getName(handler)));
                break;
            }
        }
        return result;
    }

}
