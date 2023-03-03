package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepDataGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Override
    public String getToken() {
        return "processtask/step/data/get";
    }

    @Override
    public String getName() {
        return "获取工单步骤数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤id")})
    @Output({@Param(type = ApiParamType.STRING)})
    @Description(desc = "获取工单步骤数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessTaskStepDataVo p = jsonObj.toJavaObject(ProcessTaskStepDataVo.class);
        ProcessTaskStepDataVo processTaskStepDataVo = processTaskStepDataMapper.getProcessTaskStepData(p);
        if (processTaskStepDataVo != null) {
            return processTaskStepDataVo.getData();
        }
        return null;
    }
}
