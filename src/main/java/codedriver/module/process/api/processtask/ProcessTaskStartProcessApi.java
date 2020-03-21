package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFileVo;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskStartProcessApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/startprocess";
	}

	@Override
	public String getName() {
		return "工单上报提交接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true)
	})
	@Description(desc = "工单上报提交接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.STARTPROCESS)) {
			throw new ProcessTaskRuntimeException("您没有权限执行此操作");
		}
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		
		ProcessTaskStepVo startTaskStep = new ProcessTaskStepVo();
		startTaskStep.setHandler(ProcessStepHandler.START.getHandler());
		startTaskStep.setId(processTaskStepList.get(0).getId());
		startTaskStep.setProcessTaskId(processTaskId);
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(ProcessStepHandler.START.getHandler());
		handler.startProcess(startTaskStep);
		

		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		//生成活动
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo(processTaskId, startTaskStep.getId(), UserContext.get().getUserId(true), ProcessTaskStepAction.STARTPROCESS.getValue());
		processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);
		
		processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.TITLE.getValue(), null, processTaskVo.getTitle()));
		processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.PRIORITY.getValue(), null, processTaskVo.getPriorityUuid()));
		processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.WORKER.getValue(), null, processTaskVo.getOwner()));
		//获取上报描述内容
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startTaskStep.getId());
		if(CollectionUtils.isNotEmpty(processTaskStepContentList)) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo != null && StringUtils.isNotBlank(processTaskContentVo.getContent())) {
				processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.CONTENT.getValue(), null, processTaskContentVo.getContent()));
			}
		}
		//获取上传文件uuid
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(processTaskId);
		processTaskFileVo.setProcessTaskStepId(startTaskStep.getId());
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		if(CollectionUtils.isNotEmpty(processTaskFileList)) {
			List<String> fileUuidList = new ArrayList<>();
			for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
				fileUuidList.add(processTaskFile.getFileUuid());
			}
			processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.FILE.getValue(), null, processTaskVo.getTitle()));
		}
		return null;
	}

}
