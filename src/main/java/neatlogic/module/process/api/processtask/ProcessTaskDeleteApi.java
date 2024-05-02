/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import neatlogic.module.process.dao.mapper.score.ProcessTaskScoreMapper;
import neatlogic.framework.process.dto.ProcessTaskRelationVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.process.fulltextindex.ProcessFullTextIndexType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskDeleteApi extends PrivateApiComponentBase {

    @Autowired
    ProcessTaskMapper taskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Autowired
    ProcessTaskScoreMapper scoreMapper;

    @Autowired
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Resource
    private ProcessTaskService processTaskService;

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

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id", isRequired = true)})
    @Description(desc = "删除工单")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        // 锁住当前工单
        if (taskMapper.getProcessTaskLockById(processTaskId) == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_DELETE)
                .build()
                .checkAndNoPermissionThrowException();
        // is_deleted置为1
        taskMapper.updateProcessTaskIsDeletedById(processTaskId, 1);
        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo(processTaskId, null);
        processStepHandlerUtil.action(processTaskStepVo, ProcessTaskNotifyTriggerType.DELETEPROCESSTASK);
        processStepHandlerUtil.notify(processTaskStepVo, ProcessTaskNotifyTriggerType.DELETEPROCESSTASK);
        return null;
    }

    @Deprecated
    private void deleteProcessTask(Long processTaskId) {
        // 步骤附件 processtask_file
        taskMapper.deleteProcessTaskStepFileByProcessTaskId(processTaskId);
        // sla processtask_sla_transfer processtask_sla_notify processtask_sla_time
        List<Long> slaIdList = processTaskSlaMapper.getSlaIdListByProcessTaskId(processTaskId);
        for (Long slaId : slaIdList) {
            processTaskSlaMapper.deleteProcessTaskSlaTransferBySlaId(slaId);
            processTaskSlaMapper.deleteProcessTaskSlaNotifyById(slaId);
        }
        processTaskSlaMapper.deleteProcessTaskSlaTimeBySlaId(processTaskId);
        // 关系 processtask_relation
        List<ProcessTaskRelationVo> relationList =
                taskMapper.getProcessTaskRelationList(new ProcessTaskRelationVo(processTaskId));
        for (ProcessTaskRelationVo relation : relationList) {
            taskMapper.deleteProcessTaskRelationById(relation.getId());
        }
        // 表单 processtask_form processtask_formattribute_data
        taskMapper.deleteProcessTaskFormByProcessTaskId(processTaskId);
        processTaskService.deleteProcessTaskFormAttributeDataByProcessTaskId(processTaskId);
        // 关注人 processtask_focus
        taskMapper.deleteProcessTaskFocusByProcessTaskId(processTaskId);
        // 流程汇聚 processtask_converge
        taskMapper.deleteProcessTaskConvergeByProcessTaskId(processTaskId);
        // 指派 processtask_assignworker
        taskMapper.deleteProcessTaskAssignWorkerByProcessTaskId(processTaskId);
        // 评分 processtask_score processtask_score_content processtask_score_template
        scoreMapper.deleteProcessTaskByProcessTaskId(processTaskId);
        // 步骤
        // processtask_step_audit processtask_step_audit_detail processtask_step_worker processtask_step_user
        // processtask_step_remind
        // processtask_step processtask_step_agent processtask_step_notify_policy processtask_step_comment
        // processtask_step_content
        // processtask_step_data processtask_step_formattribute processtask_step_sla
        // processtask_step_rel
        // processtask_step_timeaudit processtask_step_timeout_policy
        // processtask_step_worker_policy
        taskMapper.deleteProcessTaskStepByProcessTaskId(processTaskId);
        // 工单
        taskMapper.deleteProcessTaskByProcessTaskId(processTaskId);
        /** 删除工单号 **/
        processTaskSerialNumberMapper.deleteProcessTaskSerialNumberByProcessTaskId(processTaskId);
        /** 删除es对应工单信息 **/
//        ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue()).delete(processTaskId.toString());

        //删除全文检索索引
        IFullTextIndexHandler indexHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexHandler != null) {
            indexHandler.deleteIndex(processTaskId);
        }
    }

}
