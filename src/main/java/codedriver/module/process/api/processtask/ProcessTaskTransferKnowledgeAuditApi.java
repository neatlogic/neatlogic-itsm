package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
public class ProcessTaskTransferKnowledgeAuditApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;

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
        processTaskStepVo.setParamObj(jsonObj);
        ProcessStepUtilHandlerFactory.getHandler().activityAudit(processTaskStepVo, ProcessTaskAuditType.TRANSFERKNOWLEDGE);
        return null;
    }

}
