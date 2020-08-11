package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
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
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskTitleUpdateApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/title/update";
	}

	@Override
	public String getName() {
		return "工单标题更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "title", type = ApiParamType.REGEX, rule="^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", maxLength = 80, isRequired = true, desc = "标题")
		//@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "标题")
	})
	@Description(desc = "工单标题更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		String oldTitle = processTaskVo.getTitle();	
		String title = jsonObj.getString("title");
		//如果标题跟原来的标题不一样，生成活动
		if(!title.equals(oldTitle)) {
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(processTaskId);
			ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
			processTaskStepVo.setProcessTaskId(processTaskId);
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
						
			//更新标题
			processTaskVo.setTitle(title);
			processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
			//生成活动
			ProcessTaskContentVo oldTitleContentVo = new ProcessTaskContentVo(oldTitle);
			processTaskMapper.replaceProcessTaskContent(oldTitleContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.TITLE.getOldDataParamName(), oldTitleContentVo.getHash());
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.UPDATETITLE);
		}
		
		return null;
	}
}
