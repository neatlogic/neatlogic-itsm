package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskRelationVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskRelationDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private IProcessStepHandlerUtil IProcessStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/relation/delete";
    }

    @Override
    public String getName() {
        return "删除工单关联";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskRelationId", type = ApiParamType.LONG, isRequired = true, desc = "工单关联id"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
    })
    @Description(desc = "删除工单关联")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskRelationId = jsonObj.getLong("processTaskRelationId");
        ProcessTaskRelationVo processTaskRelationVo =
            processTaskMapper.getProcessTaskRelationById(processTaskRelationId);
        if (processTaskRelationVo != null) {
            ProcessTaskVo processTaskVo =
                processTaskMapper.getProcessTaskBaseInfoById(processTaskRelationVo.getSource());
            new ProcessAuthManager.TaskOperationChecker(processTaskVo.getId(),
                    ProcessTaskOperationType.PROCESSTASK_TRANSFERREPORT).build().checkAndNoPermissionThrowException();
            processTaskMapper.deleteProcessTaskRelationById(processTaskRelationId);

            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskRelationVo.getSource());
            processTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CHANNELTYPERELATION.getParamName(),
                processTaskRelationVo.getChannelTypeRelationId());
            processTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.PROCESSTASKLIST.getParamName(),
                JSON.toJSONString(Arrays.asList(processTaskRelationVo.getTarget())));
            processTaskStepVo.getParamObj().put("source", jsonObj.getString("source"));
            IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.DELETERELATION);

            ProcessTaskStepVo processTaskStep = new ProcessTaskStepVo();
            processTaskStep.setProcessTaskId(processTaskRelationVo.getTarget());
            processTaskStep.getParamObj().put(ProcessTaskAuditDetailType.CHANNELTYPERELATION.getParamName(),
                processTaskRelationVo.getChannelTypeRelationId());
            processTaskStep.getParamObj().put(ProcessTaskAuditDetailType.PROCESSTASKLIST.getParamName(),
                JSON.toJSONString(Arrays.asList(processTaskRelationVo.getSource())));
            processTaskStep.getParamObj().put("source", jsonObj.getString("source"));
            IProcessStepHandlerUtil.audit(processTaskStep, ProcessTaskAuditType.DELETERELATION);
        }

        return null;
    }

}
