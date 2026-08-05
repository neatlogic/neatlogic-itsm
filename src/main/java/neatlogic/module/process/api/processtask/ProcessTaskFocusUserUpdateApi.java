package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskFocusUserUpdateApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/focususer/update";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskfocususerupdateapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskfocususerupdateapi.input.param.desc.processtaskid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "nmpap.processtaskfocususerupdateapi.input.param.desc.source"),
            @Param(name = "focusUserUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "nmpap.processtaskfocususerupdateapi.input.param.desc.focususeruuidlist")
    })
    @Description(desc = "nmpap.processtaskfocususerupdateapi.getname")
    @ResubmitInterval(3)
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        JSONArray focusUserUuidList = jsonObj.getJSONArray("focusUserUuidList");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE)
                .build()
                .checkAndNoPermissionThrowException();
        List<String> oldFocusUser = processTaskMapper.getFocusUserListByTaskId(processTaskId);
        JSONObject paramObj = new JSONObject();
        paramObj.put("focusUserUuidList",focusUserUuidList);
        paramObj.put(ProcessTaskAuditDetailType.FOCUSUSER.getOldDataParamName(), JSON.toJSONString(oldFocusUser));
        paramObj.put("source",jsonObj.getString("source"));

        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
        processTaskStepVo.setProcessTaskId(processTaskVo.getId());
        processTaskStepVo.getParamObj().putAll(paramObj);
        processStepHandlerUtil.saveFocusUserList(processTaskStepVo);
        /** 生成活动 **/
        processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UPDATEFOCUSUSER);
        return null;
    }

}
