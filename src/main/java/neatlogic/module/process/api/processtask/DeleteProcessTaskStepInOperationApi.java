package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskStepInOperationVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class DeleteProcessTaskStepInOperationApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/step/inoperation";
    }

    @Override
    public String getName() {
        return "nmpap.deleteprocesstaskstepinoperationapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid")
    })
    @Output({
            @Param(name = "tbodyList", explode = ProcessTaskStepInOperationVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmpap.deleteprocesstaskstepinoperationapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        List<ProcessTaskStepInOperationVo> processTaskStepInOperationList = processTaskMapper.getProcessTaskStepInOperationListByProcessTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(processTaskStepInOperationList)) {
            processTaskMapper.deleteProcessTaskStepInOperationByProcessTaskId(processTaskId);
        }
        return TableResultUtil.getResult(processTaskStepInOperationList);
    }
}
