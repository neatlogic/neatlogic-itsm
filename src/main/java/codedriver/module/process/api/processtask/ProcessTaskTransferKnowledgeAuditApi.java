package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
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
    private IProcessStepHandlerUtil IProcessStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/transfer/knowledge/audit";
    }

    @Override
    public String getName() {
        return "记录工单转知识活动";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
        @Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "知识标题")
    })
    @Description(desc = "记录工单转知识活动")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        processTaskMapper.getProcessTaskLockById(processTaskId);
        //生成活动  
        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
        processTaskStepVo.setProcessTaskId(processTaskId);
        processTaskStepVo.getParamObj().putAll(jsonObj);
        IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.TRANSFERKNOWLEDGE);
        return null;
    }

}
