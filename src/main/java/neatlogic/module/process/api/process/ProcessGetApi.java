package neatlogic.module.process.api.process;

import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.PROCESS_MODIFY;

import neatlogic.module.process.dao.mapper.ProcessMapper;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessGetApi extends PrivateApiComponentBase {

    @Resource
    private ProcessMapper processMapper;

    @Override
    public String getToken() {
        return "process/get";
    }

    @Override
    public String getName() {
        return "获取单个流程图数据接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
			@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid")
    })
    @Output({
			@Param(explode = ProcessVo.class)
    })
    @Description(desc = "获取单个流程图数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        ProcessVo processVo = processMapper.getProcessByUuid(uuid);
        if (processVo == null) {
            throw new ProcessNotFoundException(uuid);
        }
        processVo.setConfig(ProcessConfigUtil.regulateProcessConfig(processVo.getConfig()));
        int count = processMapper.getProcessReferenceCount(uuid);
        processVo.setReferenceCount(count);
        return processVo;
    }
}