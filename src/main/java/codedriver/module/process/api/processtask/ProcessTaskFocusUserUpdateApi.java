package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
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
    private IProcessStepHandlerUtil IProcessStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/focususer/update";
    }

    @Override
    public String getName() {
        return "更新工单关注人";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
            @Param(name = "focusUserUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工单关注人列表")
    })
    @Description(desc = "更新工单关注人")
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

        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
        processTaskStepVo.setProcessTaskId(processTaskVo.getId());
        processTaskStepVo.getParamObj().putAll(paramObj);
        IProcessStepHandlerUtil.saveFocusUserList(processTaskStepVo);
        /** 生成活动 **/
        IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UPDATEFOCUSUSER);
        return null;
    }

}
