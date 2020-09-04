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
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
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
public class ProcessTaskTitleUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
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
		@Param(name = "title", type = ApiParamType.STRING, xss = true, maxLength = 80, isRequired = true, desc = "标题")
	})
	@Description(desc = "工单标题更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		String oldTitle = processTaskVo.getTitle();	
		String title = jsonObj.getString("title");
		//如果标题跟原来的标题不一样，生成活动
		if(!title.equals(oldTitle)) {
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(processTaskId);
			IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
			handler.verifyOperationAuthoriy(processTaskVo, ProcessTaskOperationType.UPDATE, true);
			//更新标题
			processTaskVo.setTitle(title);
			processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
			//生成活动
			ProcessTaskContentVo oldTitleContentVo = new ProcessTaskContentVo(oldTitle);
			processTaskMapper.replaceProcessTaskContent(oldTitleContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.TITLE.getOldDataParamName(), oldTitleContentVo.getHash());
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskId);
            processTaskStepVo.setId(processTaskStepId);
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.UPDATE);
		}
		
		return null;
	}
}
