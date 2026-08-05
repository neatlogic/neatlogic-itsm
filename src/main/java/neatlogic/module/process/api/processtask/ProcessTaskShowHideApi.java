package neatlogic.module.process.api.processtask;

import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;

import java.util.Objects;

@Service
@Transactional
@AuthAction(action = PROCESSTASK_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskShowHideApi extends PrivateApiComponentBase {

    @Autowired
    ProcessTaskMapper taskMapper;
       
    @Override
    public String getToken() {
        return "processtask/show/hide";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskshowhideapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "nmpap.processtaskshowhideapi.input.param.desc.processtaskid", isRequired = true),
        @Param(name = "isShow", type = ApiParamType.INTEGER, desc = "nmpap.processtaskshowhideapi.input.param.desc.isshow", isRequired = true)
    })
    @Description(desc = "nmpap.processtaskshowhideapi.getname")
    @Override
    @ResubmitInterval(3)
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Integer isShow = jsonObj.getInteger("isShow");
        ProcessTaskOperationType operationType = ProcessTaskOperationType.PROCESSTASK_SHOW;
        if (Objects.equals(isShow, 0)) {
            operationType = ProcessTaskOperationType.PROCESSTASK_HIDE;
        }
        new ProcessAuthManager.TaskOperationChecker(processTaskId, operationType)
                .build()
                .checkAndNoPermissionThrowException();
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setId(processTaskId);
        processTaskVo.setIsShow(isShow);
        taskMapper.updateProcessTaskIsShow(processTaskVo);
        return null;
    }

}
