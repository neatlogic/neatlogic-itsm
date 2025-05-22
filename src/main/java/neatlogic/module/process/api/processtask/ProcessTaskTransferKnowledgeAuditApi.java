package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
@Transactional
public class ProcessTaskTransferKnowledgeAuditApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/transfer/knowledge/audit";
    }

    @Override
    public String getName() {
        return "nmpap.processtasktransferknowledgeauditapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "common.source"),// ok
            @Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "common.title")
    })
    @Description(desc = "nmpap.processtasktransferknowledgeauditapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        processTaskMapper.getProcessTaskLockById(processTaskId);
        //生成活动  
        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
        processTaskStepVo.setProcessTaskId(processTaskId);
        processTaskStepVo.getParamObj().putAll(jsonObj);
        processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.TRANSFERKNOWLEDGE);
        return null;
    }

}
