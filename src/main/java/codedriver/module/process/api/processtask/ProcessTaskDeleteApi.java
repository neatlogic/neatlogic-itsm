package codedriver.module.process.api.processtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskRelationVo;
import codedriver.framework.process.dto.ProcessTaskSlaVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskDeleteApi extends PrivateApiComponentBase {

    @Autowired
    ProcessTaskMapper taskMapper;
    
    @Override
    public String getToken() {
        return "processtask/delete";
    }

    @Override
    public String getName() {
        return "删除工单";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id", isRequired = true)
    })
    @Description(desc = "删除工单")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        
        
        //taskMapper.deleteProcessTaskStepRemind(processTaskStepRemindVo)
        //步骤附件
        taskMapper.deleteProcessTaskStepFileByProcessTaskId(processTaskId);
        //步骤回复内容
        taskMapper.deleteProcessTaskStepContentByProcessTaskId(processTaskId);
        //活动记录
        taskMapper.deleteProcessTaskStepAuditByProcessTaskId(processTaskId);
        //sla通知
        List<ProcessTaskSlaVo> processTaskSlaList = taskMapper.getProcessTaskSlaByProcessTaskId(processTaskId);
        for(ProcessTaskSlaVo processTaskSla : processTaskSlaList) {
            taskMapper.deleteProcessTaskSlaTransferBySlaId(processTaskSla.getId());
            taskMapper.deleteProcessTaskSlaNotifyById(processTaskSla.getId());
        }
        //关系
        List<ProcessTaskRelationVo>  relationList = taskMapper.getProcessTaskRelationList(new ProcessTaskRelationVo(processTaskId));
        for(ProcessTaskRelationVo relation : relationList) {
            taskMapper.deleteProcessTaskRelationById(relation.getId());
        }
        //表单值
        taskMapper.deleteProcessTaskFormAttributeDataByProcessTaskId(processTaskId);
        //关注人
        taskMapper.deleteProcessTaskFocus(new ProcessTaskVo(processTaskId), null);
        //流程汇聚
        taskMapper.deleteProcessTaskConvergeByProcessTaskId(processTaskId);
        //指派
        taskMapper.deleteProcessTaskAssignWorkerByProcessTaskId(processTaskId);
        
        
        return null;
    }

}
