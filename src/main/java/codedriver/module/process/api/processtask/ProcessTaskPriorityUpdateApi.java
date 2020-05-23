package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class ProcessTaskPriorityUpdateApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		// 锁定当前流程
		processTaskMapper.getProcessTaskLockById(processTaskId);
		String oldPriorityUuid = processTaskVo.getPriorityUuid();
		String priorityUuid = jsonObj.getString("priorityUuid");
		//如果优先级跟原来的优先级不一样，生成活动
		if(!priorityUuid.equals(oldPriorityUuid)) {
			ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
			Long processTaskStepId = jsonObj.getLong("processTaskStepId");
			if(processTaskStepId != null) {
				processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
				if(processTaskStepVo == null) {
					throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
				}
				if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
					throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
				}
			}
			
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler();
			handler.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.UPDATE);
			
			//更新优先级
			processTaskVo.setPriorityUuid(priorityUuid);
			processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
			//生成活动	
			jsonObj.put(ProcessTaskAuditDetailType.PRIORITY.getOldDataParamName(), oldPriorityUuid);
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskStepAction.UPDATEPRIORITY);
		}
		
		return null;
	}

}
