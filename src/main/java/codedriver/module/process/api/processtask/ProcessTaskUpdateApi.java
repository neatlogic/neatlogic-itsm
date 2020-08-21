package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskUpdateApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Autowired
	private FileMapper fileMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/update";
	}

	@Override
	public String getName() {
		return "更新工单信息";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "title", type = ApiParamType.STRING, xss = true, maxLength = 80, isRequired = true, desc = "标题"),
		@Param(name = "priorityUuid", type = ApiParamType.STRING, isRequired = true, desc = "优先级uuid"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
		@Param(name = "fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表")
	})
	@Description(desc = "更新工单信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		
		//获取开始步骤id
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		Long startProcessTaskStepId = processTaskStepList.get(0).getId();
				
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		handler.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.UPDATE);
		// 锁定当前流程
		processTaskMapper.getProcessTaskLockById(processTaskId);
		
		boolean isUpdate = false;
		String oldTitle = processTaskVo.getTitle();	
		String title = jsonObj.getString("title");
		if(!title.equals(oldTitle)) {	
			isUpdate = true;
			processTaskVo.setTitle(title);
			ProcessTaskContentVo oldTitleContentVo = new ProcessTaskContentVo(oldTitle);
			processTaskMapper.replaceProcessTaskContent(oldTitleContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.TITLE.getOldDataParamName(), oldTitleContentVo.getHash());
		}else {
			jsonObj.remove("title");
		}
		

		String priorityUuid = jsonObj.getString("priorityUuid");
		if(priorityMapper.checkPriorityIsExists(priorityUuid) == 0) {
			throw new PriorityNotFoundException(priorityUuid);
		}
		String oldPriorityUuid = processTaskVo.getPriorityUuid();
		if(!priorityUuid.equals(oldPriorityUuid)) {	
			isUpdate = true;
			processTaskVo.setPriorityUuid(priorityUuid);
			ProcessTaskContentVo oldPriorityUuidContentVo = new ProcessTaskContentVo(oldPriorityUuid);
			processTaskMapper.replaceProcessTaskContent(oldPriorityUuidContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.PRIORITY.getOldDataParamName(), oldPriorityUuidContentVo.getHash());
		}else {
			jsonObj.remove("priorityUuid");
		}
		if(isUpdate) {
			processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
		}
		
		String oldContentHash = processTaskMapper.getProcessTaskStepContentHashByProcessTaskStepId(startProcessTaskStepId);
		String content = jsonObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			if(Objects.equals(oldContentHash, contentVo.getHash())) {
				jsonObj.remove("content");
			}else {
				isUpdate = true;
				jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
				processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, startProcessTaskStepId, contentVo.getHash()));
			}
		}else if(oldContentHash != null){
			isUpdate = true;
			jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
			processTaskMapper.deleteProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, startProcessTaskStepId, oldContentHash));
		}else {
			jsonObj.remove("content");
		}
		
		List<Long> oldFileIdList = new ArrayList<>();
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(processTaskId);
		processTaskFileVo.setProcessTaskStepId(startProcessTaskStepId);
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		if(CollectionUtils.isNotEmpty(processTaskFileList)) {
			for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
				oldFileIdList.add(processTaskFile.getFileId());
			}
			
		}

		List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
		if(fileIdList == null) {
			fileIdList = new ArrayList<>();
		}
		if(Objects.equals(oldFileIdList, fileIdList)) {
			jsonObj.remove("fileIdList");
		}else {
			ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(JSON.toJSONString(oldFileIdList));
			processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), processTaskContentVo.getHash());
			processTaskMapper.deleteProcessTaskFile(processTaskFileVo);
			for (Long fileId : fileIdList) {
				if(fileMapper.getFileById(fileId) != null) {
					isUpdate = true;
					processTaskFileVo.setFileId(fileId);
					processTaskMapper.insertProcessTaskFile(processTaskFileVo);
				}
			}
			isUpdate = true;
		}

		//生成活动
		if(isUpdate) {
		    ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		    processTaskStepVo.setProcessTaskId(processTaskId);
		    processTaskStepVo.setId(processTaskStepId);
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.UPDATE);	
		}
		return null;
	}

}
