package neatlogic.module.process.api.processtask.tmp;

import neatlogic.framework.util.$;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
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
        return "nmpapt.processtaskmobileisfitapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpapt.processtaskmobileisfitapi.input.param.desc.processtaskid")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.BOOLEAN, desc = "nmpapt.processtaskmobileisfitapi.output.param.desc.return.name")
    })
    @Description(desc = "nmpapt.processtaskmobileisfitapi.getname")
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
                result.put("msg", $.t("nmpapt.processtaskmobileisfitapi.unsupportedstep", ProcessStepHandlerTypeFactory.getName(handler)));
                break;
            }
        }
        return result;
    }

}
