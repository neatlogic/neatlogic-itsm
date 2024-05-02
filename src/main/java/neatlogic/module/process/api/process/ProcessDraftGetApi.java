package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.dto.ProcessDraftVo;
import neatlogic.framework.process.exception.process.ProcessDraftNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessDraftGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessMapper processMapper;

    @Override
    public String getToken() {
        return "process/draft/get";
    }

    @Override
    public String getName() {
        return "nmpap.processdraftgetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "common.uuid")
    })
    @Output({
            @Param(explode = ProcessDraftVo.class)
    })
    @Description(desc = "nmpap.processdraftgetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        ProcessDraftVo processDraftVo = processMapper.getProcessDraftByUuid(uuid);
        if (processDraftVo == null) {
            throw new ProcessDraftNotFoundException(uuid);
        }
//		processDraftVo.setConfig(ProcessConfigUtil.regulateProcessConfig(processDraftVo.getConfig()));
        return processDraftVo;
    }

}
