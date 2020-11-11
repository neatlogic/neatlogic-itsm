package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskPriorityUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getToken() {
		return "processtask/priority/update";
	}

	@Override
	public String getName() {
		return "工单优先级更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "priorityUuid", type = ApiParamType.STRING, isRequired = true, desc = "优先级uuid")
	})
	@Description(desc = "工单优先级更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		// 锁定当前流程
		processTaskMapper.getProcessTaskLockById(processTaskId);
		String priorityUuid = jsonObj.getString("priorityUuid");
		if(priorityMapper.checkPriorityIsExists(priorityUuid) == 0) {
			throw new PriorityNotFoundException(priorityUuid);
		}

		String oldPriorityUuid = processTaskVo.getPriorityUuid();
		//如果优先级跟原来的优先级不一样，生成活动
		if(!priorityUuid.equals(oldPriorityUuid)) {			
			IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
			try {
	            handler.verifyOperationAuthoriy(processTaskVo, ProcessTaskOperationType.UPDATE, true);
	        }catch(ProcessTaskNoPermissionException e) {
	            throw new PermissionDeniedException();
	        }
			//更新优先级
			processTaskVo.setPriorityUuid(priorityUuid);
			processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
			//生成活动
			ProcessTaskContentVo oldPriorityUuidContentVo = new ProcessTaskContentVo(oldPriorityUuid);
			processTaskMapper.replaceProcessTaskContent(oldPriorityUuidContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.PRIORITY.getOldDataParamName(), oldPriorityUuidContentVo.getHash());
			ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskId);
            processTaskStepVo.setId(processTaskStepId);
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.UPDATE);
			handler.calculateSla(new ProcessTaskVo(processTaskId));
		}		
		return null;
	}

}
