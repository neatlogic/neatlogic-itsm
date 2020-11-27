package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
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
