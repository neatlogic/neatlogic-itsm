package neatlogic.module.process.api.processtask;

import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
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
        return "隐藏显示工单";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id", isRequired = true),
        @Param(name = "isShow", type = ApiParamType.INTEGER, desc = "是否 1显示/0隐藏", isRequired = true)
    })
    @Description(desc = "隐藏显示工单")
    @Override
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
