package codedriver.module.process.api.processtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.elasticsearch.core.ElasticSearchFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.score.ProcessTaskScoreMapper;
import codedriver.framework.process.dto.ProcessTaskRelationVo;
import codedriver.framework.process.dto.ProcessTaskSlaVo;
import codedriver.framework.process.elasticsearch.constvalue.ESHandler;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@Transactional
@AuthAction(name = "PROCESSTASK_MODIFY")
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskDeleteApi extends PrivateApiComponentBase {

    @Autowired
    ProcessTaskMapper taskMapper;
    
    @Autowired
    ProcessTaskScoreMapper scoreMapper;
    
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
        /** 删除数据库对应工单信息 **/
        //锁住当前工单
        Long taskId = taskMapper.getProcessTaskLockById(processTaskId);
        if(taskId == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        //步骤附件 processtask_file
        taskMapper.deleteProcessTaskStepFileByProcessTaskId(processTaskId);
        //sla processtask_sla_transfer processtask_sla_notify processtask_sla_time
        List<ProcessTaskSlaVo> processTaskSlaList = taskMapper.getProcessTaskSlaByProcessTaskId(processTaskId);
        for(ProcessTaskSlaVo processTaskSla : processTaskSlaList) {
            taskMapper.deleteProcessTaskSlaTransferBySlaId(processTaskSla.getId());
            taskMapper.deleteProcessTaskSlaNotifyById(processTaskSla.getId());
            taskMapper.deleteProcessTaskSlaTimeBySlaId(processTaskId);
        }
        //关系 processtask_relation
        List<ProcessTaskRelationVo>  relationList = taskMapper.getProcessTaskRelationList(new ProcessTaskRelationVo(processTaskId));
        for(ProcessTaskRelationVo relation : relationList) {
            taskMapper.deleteProcessTaskRelationById(relation.getId());
        }
        //表单 processtask_form processtask_formattribute_data
        taskMapper.deleteProcessTaskFormByProcessTaskId(processTaskId);
        taskMapper.deleteProcessTaskFormAttributeDataByProcessTaskId(processTaskId);
        //关注人 processtask_focus
        taskMapper.deleteProcessTaskFocusByProcessTaskId(processTaskId);
        //流程汇聚 processtask_converge
        taskMapper.deleteProcessTaskConvergeByProcessTaskId(processTaskId);
        //指派 processtask_assignworker
        taskMapper.deleteProcessTaskAssignWorkerByProcessTaskId(processTaskId);
        //评分 processtask_score processtask_score_content processtask_score_template
        scoreMapper.deleteProcessTaskByProcessTaskId(processTaskId);
        //步骤
        //processtask_step_audit processtask_step_audit_detail processtask_step_worker  processtask_step_user processtask_step_remind
        //processtask_step processtask_step_agent processtask_step_notify_policy processtask_step_comment processtask_step_content
        //processtask_step_data processtask_step_formattribute processtask_step_sla processtask_step_subtask processtask_step_rel
        //processtask_step_subtask_content processtask_step_timeaudit processtask_step_timeout_policy processtask_step_worker_policy
        taskMapper.deleteProcessTaskStepByProcessTaskId(processTaskId);
        //工单
        taskMapper.deleteProcessTaskByProcessTaskId(processTaskId);
        
        /** 删除es对应工单信息 **/
        ElasticSearchFactory.getHandler(ESHandler.PROCESSTASK.getValue()).delete(processTaskId.toString());
        return null;
    }

}
