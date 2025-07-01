package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "term.itsm.processtaskstepid"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "common.type"),
            @Param(name = "fcu", type = ApiParamType.STRING, desc = "common.createuser"),
    })
    @Output({
            @Param(type = ApiParamType.JSONOBJECT)
    })
    @Description(desc = "获取工单步骤数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        if (id != null) {
            ProcessTaskStepDataVo processTaskStepDataVo = processTaskStepDataMapper.getProcessTaskStepDataById(id);
            if (processTaskStepDataVo != null) {
                return processTaskStepDataVo.getData();
            }
        } else {
            Long processTaskId = jsonObj.getLong("processTaskId");
            Long processTaskStepId = jsonObj.getLong("processTaskStepId");
            String type = jsonObj.getString("type");
            String fcu = jsonObj.getString("fcu");
            ProcessTaskStepDataVo searchVo = new ProcessTaskStepDataVo();
            searchVo.setProcessTaskId(processTaskId);
            searchVo.setProcessTaskStepId(processTaskStepId);
            searchVo.setType(type);
            searchVo.setFcu(fcu);
            List<ProcessTaskStepDataVo> processTaskStepDataList = processTaskStepDataMapper.searchProcessTaskStepData(searchVo);
            if (CollectionUtils.isNotEmpty(processTaskStepDataList)) {
                if (processTaskStepDataList.size() == 1) {
                    return processTaskStepDataList.get(0).getData();
                } else {
                    return TableResultUtil.getResult(processTaskStepDataList);
                }
            }
        }
        return null;
    }
}
