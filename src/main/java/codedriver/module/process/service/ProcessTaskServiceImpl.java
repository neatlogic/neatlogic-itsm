package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.FormAttributeAction;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.constvalue.automatic.CallbackType;
import codedriver.framework.process.constvalue.automatic.FailPolicy;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskConfigVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.integration.handler.ProcessRequestFrom;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.framework.util.ConditionUtil;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.module.process.schedule.plugin.ProcessTaskAutomaticJob;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

	private final static Logger logger = LoggerFactory.getLogger(ProcessTaskServiceImpl.class);
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Autowired
	private IntegrationMapper integrationMapper;
	
	@Autowired
	private ProcessStepHandlerMapper processStepHandlerMapper;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Autowired
	ProcessTaskStepDataMapper processTaskStepDataMapper;
	
	
	@Override
	public List<ProcessTaskStepFormAttributeVo> getProcessTaskStepFormAttributeByStepId(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo){
		return processTaskMapper.getProcessTaskStepFormAttributeByStepId(processTaskStepFormAttributeVo);
	}

	@Override
	public ProcessTaskStepVo getProcessTaskStepDetailById(Long processTaskStepId) {
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		ProcessTaskFormVo form = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskStepVo.getProcessTaskId());
		if (form != null) {
			processTaskStepVo.setFormUuid(form.getFormUuid());
		}
		return processTaskStepVo;
	}

	@Override
	public ProcessTaskStepVo getProcessTaskStepBaseInfoById(Long processTaskStepId) {
		return processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
	}

	@Override
	public ProcessTaskFormVo getProcessTaskFormByProcessTaskId(Long processTaskId) {
		return processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
	}

	@Override
	public ProcessTaskVo getProcessTaskBaseInfoById(Long processTaskId) {
		return processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
	}

	@Override
	public void createSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
		Long targetTime = paramObj.getLong("targetTime");
		if(targetTime != null) {
			processTaskStepSubtaskVo.setTargetTime(new Date(targetTime));
		}
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		//插入子任务	
		processTaskMapper.insertProcessTaskStepSubtask(processTaskStepSubtaskVo);
		paramObj.put("processTaskStepSubtaskId", processTaskStepSubtaskVo.getId());
		String content = paramObj.getString("content");
		ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
		processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
		processTaskMapper.insertProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskStepAction.CREATESUBTASK.getValue(), processTaskContentVo.getHash()));

		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
		}
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			List<ProcessTaskStepUserVo> userList = new ArrayList<>();
			ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
			processTaskStepUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
			processTaskStepUserVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), GroupSearch.USER.getValue(), processTaskStepSubtaskVo.getUserUuid()));
			handler.updateProcessTaskStepUserAndWorker(workerList, userList);	
			//记录活动
			ProcessTaskStepSubtaskVo subtaskVo = new ProcessTaskStepSubtaskVo();
			subtaskVo.setId(processTaskStepSubtaskVo.getId());
			subtaskVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
			subtaskVo.setUserName(processTaskStepSubtaskVo.getUserName());
			subtaskVo.setTargetTime(processTaskStepSubtaskVo.getTargetTime());
			subtaskVo.setContentHash(processTaskContentVo.getHash());
			paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getParamName(), JSON.toJSONString(subtaskVo));
			currentProcessTaskStepVo.setParamObj(paramObj);
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.CREATESUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.CREATESUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
		
	}

	@Override
	public void editSubtask(ProcessTaskStepSubtaskVo oldProcessTaskStepSubtask) {
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(oldProcessTaskStepSubtask.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(oldProcessTaskStepSubtask.getProcessTaskStepId().toString());
		}else if(currentProcessTaskStepVo.getIsActive().intValue() != 1){
			throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
		}
		List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(oldProcessTaskStepSubtask.getId());
		for(ProcessTaskStepSubtaskContentVo subtaskContentVo : processTaskStepSubtaskContentList) {
			if(ProcessTaskStepAction.CREATESUBTASK.getValue().equals(subtaskContentVo.getAction())) {
				oldProcessTaskStepSubtask.setContentHash(subtaskContentVo.getContentHash());
			}
		}
		JSONObject paramObj = oldProcessTaskStepSubtask.getParamObj();
		String content = paramObj.getString("content");
		paramObj.remove("content");
		ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
		processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
		processTaskMapper.updateProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(oldProcessTaskStepSubtask.getId(), ProcessTaskStepAction.CREATESUBTASK.getValue(), processTaskContentVo.getHash()));
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
		processTaskStepSubtaskVo.setId(oldProcessTaskStepSubtask.getId());
		processTaskStepSubtaskVo.setContentHash(processTaskContentVo.getHash());
		
		Long targetTime = paramObj.getLong("targetTime");
		if(targetTime != null) {
			processTaskStepSubtaskVo.setTargetTime(new Date(targetTime));
		}
		
		String workers = paramObj.getString("workerList");
		paramObj.remove("workerList");
		String[] split = workers.split("#");
		UserVo userVo = userMapper.getUserBaseInfoByUuid(split[1]);
		processTaskStepSubtaskVo.setUserUuid(userVo.getUuid());
		processTaskStepSubtaskVo.setUserName(userVo.getUserName());
		
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		
		if(processTaskStepSubtaskVo.equals(oldProcessTaskStepSubtask)) {//如果子任务信息没有被修改，则不进行下面操作
			return;
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			if(!processTaskStepSubtaskVo.getUserUuid().equals(oldProcessTaskStepSubtask.getUserUuid())) {//更新了处理人
				
				List<ProcessTaskStepUserVo> userList = new ArrayList<>();
				ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
				processTaskStepUserVo.setProcessTaskId(oldProcessTaskStepSubtask.getProcessTaskId());
				processTaskStepUserVo.setProcessTaskStepId(oldProcessTaskStepSubtask.getProcessTaskStepId());
				processTaskStepUserVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
				processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
				processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
				userList.add(processTaskStepUserVo);
				ProcessTaskStepUserVo oldUserVo = new ProcessTaskStepUserVo();
				oldUserVo.setProcessTaskId(oldProcessTaskStepSubtask.getProcessTaskId());
				oldUserVo.setProcessTaskStepId(oldProcessTaskStepSubtask.getProcessTaskStepId());
				oldUserVo.setUserUuid(oldProcessTaskStepSubtask.getUserUuid());
				oldUserVo.setUserName(oldProcessTaskStepSubtask.getUserName());
				oldUserVo.setUserType(ProcessUserType.MINOR.getValue());
				userList.add(oldUserVo);
				
				List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
				workerList.add(new ProcessTaskStepWorkerVo(oldProcessTaskStepSubtask.getProcessTaskId(), oldProcessTaskStepSubtask.getProcessTaskStepId(), GroupSearch.USER.getValue(), processTaskStepSubtaskVo.getUserUuid()));
				workerList.add(new ProcessTaskStepWorkerVo(oldProcessTaskStepSubtask.getProcessTaskId(), oldProcessTaskStepSubtask.getProcessTaskStepId(), GroupSearch.USER.getValue(), oldProcessTaskStepSubtask.getUserUuid()));
				handler.updateProcessTaskStepUserAndWorker(workerList, userList);
			}
				
			//记录活动
			ProcessTaskStepSubtaskVo subtaskVo = new ProcessTaskStepSubtaskVo();
			subtaskVo.setId(processTaskStepSubtaskVo.getId());
			subtaskVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
			subtaskVo.setUserName(processTaskStepSubtaskVo.getUserName());
			subtaskVo.setTargetTime(processTaskStepSubtaskVo.getTargetTime());
			subtaskVo.setContentHash(processTaskStepSubtaskVo.getContentHash());
			paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getParamName(), JSON.toJSONString(subtaskVo));
			
			ProcessTaskStepSubtaskVo oldSubtaskVo = new ProcessTaskStepSubtaskVo();
			oldSubtaskVo.setId(oldProcessTaskStepSubtask.getId());
			oldSubtaskVo.setUserUuid(oldProcessTaskStepSubtask.getUserUuid());
			oldSubtaskVo.setUserName(oldProcessTaskStepSubtask.getUserName());
			oldSubtaskVo.setTargetTime(oldProcessTaskStepSubtask.getTargetTime());
			oldSubtaskVo.setContentHash(oldProcessTaskStepSubtask.getContentHash());
			ProcessTaskContentVo oldSubtaskContentVo = new ProcessTaskContentVo(JSON.toJSONString(oldSubtaskVo));
			processTaskMapper.replaceProcessTaskContent(oldSubtaskContentVo);
			paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getOldDataParamName(), oldSubtaskContentVo.getHash());
			currentProcessTaskStepVo.setParamObj(paramObj);
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.EDITSUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.EDITSUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
	}

	@Override
	public void redoSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
		}else if(currentProcessTaskStepVo.getIsActive().intValue() != 1){
			throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
		}
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
		String content = paramObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
			processTaskMapper.insertProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskStepAction.REDOSUBTASK.getValue(), processTaskContentVo.getHash()));
		}
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			List<ProcessTaskStepUserVo> userList = new ArrayList<>();
			ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
			processTaskStepUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
			processTaskStepUserVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), GroupSearch.USER.getValue(), processTaskStepSubtaskVo.getUserUuid()));
			currentProcessTaskStepVo.setParamObj(processTaskStepSubtaskVo.getParamObj());
			handler.updateProcessTaskStepUserAndWorker(workerList, userList);	
			//记录活动
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.REDOSUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.REDOSUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
		
	}

	@Override
	public void completeSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
		}else if(currentProcessTaskStepVo.getIsActive().intValue() != 1){
			throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
		}
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
		String content = paramObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
			processTaskMapper.insertProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskStepAction.COMPLETESUBTASK.getValue(), processTaskContentVo.getHash()));
		}
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			List<ProcessTaskStepUserVo> userList = new ArrayList<>();
			ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
			processTaskStepUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
			processTaskStepUserVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), GroupSearch.USER.getValue(), processTaskStepSubtaskVo.getUserUuid()));
			currentProcessTaskStepVo.setParamObj(processTaskStepSubtaskVo.getParamObj());
			handler.updateProcessTaskStepUserAndWorker(workerList, userList);	
			//记录活动
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.COMPLETESUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.COMPLETESUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
		
	}

	@Override
	public void abortSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
		}else if(currentProcessTaskStepVo.getIsActive().intValue() != 1){
			throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
		}
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.ABORTED.getValue());
		processTaskStepSubtaskVo.setCancelUser(UserContext.get().getUserUuid(true));
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			List<ProcessTaskStepUserVo> userList = new ArrayList<>();
			ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
			processTaskStepUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
			processTaskStepUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
			processTaskStepUserVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), GroupSearch.USER.getValue(), processTaskStepSubtaskVo.getUserUuid()));
			currentProcessTaskStepVo.setParamObj(processTaskStepSubtaskVo.getParamObj());
			handler.updateProcessTaskStepUserAndWorker(workerList, userList);	
			//记录活动
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.ABORTSUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.ABORTSUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
	}

	@Override
	public List<ProcessTaskStepSubtaskContentVo> commentSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
		}else if(currentProcessTaskStepVo.getIsActive().intValue() != 1){
			throw new ProcessTaskRuntimeException("步骤未激活，不能回复子任务");
		}
		JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
		String content = paramObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
			processTaskMapper.insertProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), ProcessTaskStepAction.COMMENTSUBTASK.getValue(), processTaskContentVo.getHash()));
		}
		List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtaskVo.getId());
		Iterator<ProcessTaskStepSubtaskContentVo> iterator = processTaskStepSubtaskContentList.iterator();
		while(iterator.hasNext()) {
			ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = iterator.next();
			if(processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
				if(ProcessTaskStepAction.CREATESUBTASK.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
					processTaskStepSubtaskVo.setContent(processTaskStepSubtaskContentVo.getContent());
					iterator.remove();
				}
			}
		}
		return processTaskStepSubtaskContentList;
	}
	
	@Override
	public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo, Map<String, String> formAttributeActionMap, int mode) {
		Map<String, Object> formAttributeDataMap = processTaskVo.getFormAttributeDataMap();
		if(formAttributeDataMap == null) {
			formAttributeDataMap = new HashMap<>();
		}
		String formConfig = processTaskVo.getFormConfig();
		if(StringUtils.isNotBlank(formConfig)) {
			try {
				JSONObject formConfigObj = JSON.parseObject(formConfig);
				if(MapUtils.isNotEmpty(formConfigObj)) {
					JSONArray controllerList = formConfigObj.getJSONArray("controllerList");
					if(CollectionUtils.isNotEmpty(controllerList)) {
						List<String> currentUserProcessUserTypeList = new ArrayList<>();
						List<String> currentUserTeamList = new ArrayList<>();
						if(mode == 0) {					
							currentUserProcessUserTypeList.add(UserType.ALL.getValue());
							if(UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
								currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
							}
							if(UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
								currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
							}
							currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
						}else if(mode == 1){
							if(formAttributeActionMap == null) {
								formAttributeActionMap = new HashMap<>();
							}
						}
						
						for(int i = 0; i < controllerList.size(); i++) {
							JSONObject attributeObj = controllerList.getJSONObject(i);
							String action = FormAttributeAction.HIDE.getValue();
							JSONObject config = attributeObj.getJSONObject("config");
							if(mode == 0) {
								if(MapUtils.isNotEmpty(config)) {
									List<String> authorityList = JSON.parseArray(config.getString("authorityConfig"), String.class);
									if(CollectionUtils.isNotEmpty(authorityList)) {
										for(String authority : authorityList) {
											String[] split = authority.split("#");
											if(GroupSearch.COMMON.getValue().equals(split[0])) {
												if(currentUserProcessUserTypeList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
												if(currentUserProcessUserTypeList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(GroupSearch.USER.getValue().equals(split[0])) {
												if(UserContext.get().getUserUuid(true).equals(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
												if(currentUserTeamList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
												if(UserContext.get().getRoleUuidList().contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}
										}
									}
								}
							}else if(mode == 1){
								action = formAttributeActionMap.get(attributeObj.getString("uuid"));
							}
							if(FormAttributeAction.READ.getValue().equals(action)) {
								attributeObj.put("isReadonly", true);
							}else if(FormAttributeAction.HIDE.getValue().equals(action)) {
								attributeObj.put("isHide", true);
								formAttributeDataMap.remove(attributeObj.getString("uuid"));//对于隐藏属性，不返回值
								if(config != null) {
									config.remove("value");
									config.remove("defaultValueList");//对于隐藏属性，不返回默认值
								}
							}
						}
						processTaskVo.setFormConfig(formConfigObj.toJSONString());
					}
				}
			}catch(Exception ex) {
				logger.error("表单配置不是合法的JSON格式", ex);
			}
		}
		
	}

	@Override
	public void parseProcessTaskStepComment(ProcessTaskStepCommentVo processTaskStepComment) {
		if(StringUtils.isNotBlank(processTaskStepComment.getContentHash())) {
			ProcessTaskContentVo contentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepComment.getContentHash());
			if(contentVo != null) {
				processTaskStepComment.setContent(contentVo.getContent());
			}
		}
		if(StringUtils.isNotBlank(processTaskStepComment.getFileUuidListHash())) {
			ProcessTaskContentVo contentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepComment.getFileUuidListHash());
			if(contentVo != null) {
				List<String> fileUuidList = JSON.parseArray(contentVo.getContent(), String.class);
				if(CollectionUtils.isNotEmpty(fileUuidList)) {
					processTaskStepComment.setFileUuidList(fileUuidList);
					for(String fileUuid : fileUuidList) {
						FileVo fileVo = fileMapper.getFileByUuid(fileUuid);
						if(fileVo != null) {
							processTaskStepComment.getFileList().add(fileVo);
						}
					}
				}
			}
		}			
		if(StringUtils.isNotBlank(processTaskStepComment.getLcu())) {
			UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepComment.getLcu());
			if(user != null) {
				processTaskStepComment.setLcuName(user.getUserName());
			}
		}
		UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepComment.getFcu());
		if(user != null) {
			processTaskStepComment.setFcuName(user.getUserName());
		}
	}
	
	@Override
	public Boolean runRequest(AutomaticConfigVo automaticConfigVo,ProcessTaskStepVo currentProcessTaskStepVo) {
		
		Boolean isUnloadJob = false;
		ProcessTaskStepDataVo auditDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),currentProcessTaskStepVo.getId(),ProcessStepHandler.AUTOMATIC.getHandler()));
		JSONObject data = auditDataVo.getData();
		String integrationUuid = automaticConfigVo.getBaseIntegrationUuid();
		JSONObject successConfig = automaticConfigVo.getBaseSuccessConfig();
		String template = automaticConfigVo.getBaseResultTemplate();
		JSONObject failConfig = null;
		JSONObject audit = data.getJSONObject("requestAudit");
		String resultJson = null;
		if(!automaticConfigVo.getIsRequest()) {
			audit = data.getJSONObject("callbackAudit");
			template = automaticConfigVo.getCallbackResultTemplate();
			integrationUuid =automaticConfigVo.getCallbackIntegrationUuid();
			successConfig = automaticConfigVo.getCallbackSuccessConfig();
			failConfig = automaticConfigVo.getCallbackFailConfig();
		}
		audit.put("startTime", System.currentTimeMillis());
		JSONObject auditResult = new JSONObject();
		audit.put("result", auditResult);
		IProcessStepHandler processHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		try {
			IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
			audit.put("integrationName", integrationVo.getName());
			IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
			if (handler == null) {
				throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
			}
	    	integrationVo.getParamObj().putAll(getIntegrationParam(automaticConfigVo,currentProcessTaskStepVo));
			IntegrationResultVo resultVo = handler.sendRequest(integrationVo,ProcessRequestFrom.PROCESS);
			resultJson = resultVo.getTransformedResult();
			if(StringUtils.isBlank(resultVo.getTransformedResult())) {
				resultJson = resultVo.getRawResult();
			}
			audit.put("endTime", System.currentTimeMillis());
			auditResult.put("json", resultJson);
			auditResult.put("template", FreemarkerUtil.transform(JSONObject.parse(resultVo.getTransformedResult()), template));
			if(StringUtils.isNotBlank(resultVo.getError())) {
				logger.error(resultVo.getError());
	    		throw new MatrixExternalException("外部接口访问异常");
	    	}else if(StringUtils.isNotBlank(resultJson)) {
				if(predicate(successConfig,resultVo,true)) {//如果执行成功
					audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.SUCCEED.getValue()));
					if(automaticConfigVo.getIsRequest()&&!automaticConfigVo.getIsHasCallback()||!automaticConfigVo.getIsRequest()) {//第一次请求
						processHandler.complete(currentProcessTaskStepVo);
					}else {//回调请求
						if(CallbackType.WAIT.getValue().equals(automaticConfigVo.getCallbackType())) {
							//等待回调,挂起
							//processHandler.hang(currentProcessTaskStepVo);
						}
						if(CallbackType.INTERVAL.getValue().equals(automaticConfigVo.getCallbackType())) {
							automaticConfigVo.setIsRequest(false);
							automaticConfigVo.setResultJson(JSONObject.parseObject(resultJson));
							data = initProcessTaskStepData(currentProcessTaskStepVo,automaticConfigVo,data,"callback");
							initJob(automaticConfigVo,currentProcessTaskStepVo,data);
						}
					}
					isUnloadJob = true;
				}else if(automaticConfigVo.getIsRequest()||(!automaticConfigVo.getIsRequest()&&predicate(failConfig,resultVo,false))){//失败
					audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
					if(FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						List<ProcessTaskStepVo> backStepList = getbackStepList(currentProcessTaskStepVo.getId());
						if(backStepList.size() == 1) {
							ProcessTaskStepVo processTaskStepVo = backStepList.get(0);
							if (processHandler != null) {
								processHandler.back(processTaskStepVo);
							}
						}else {//如果存在多个回退线，则挂起
							//processHandler.hang(currentProcessTaskStepVo);
						}
					}else if(FailPolicy.KEEP_ON.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						processHandler.complete(currentProcessTaskStepVo);
					}else if(FailPolicy.CANCEL.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						processHandler.abort(currentProcessTaskStepVo);
					}else {//hang
						//processHandler.hang(currentProcessTaskStepVo);
					}
					isUnloadJob = true;
				}else{
					audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.RUNNING.getValue()));
					//continue
				}
	    	}
			
		}catch(Exception ex) {
			logger.error(ex.getMessage(),ex);
			audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
			//processHandler.hang(currentProcessTaskStepVo);
			isUnloadJob = true;
		}finally {
			auditDataVo.setData(data.toJSONString());
			auditDataVo.setFcu("system");
			processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
		}
		return isUnloadJob;
	}
	
	@Override
	public JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo,AutomaticConfigVo automaticConfigVo,JSONObject data,String type) {
		JSONObject failConfig = new JSONObject();
		JSONObject successConfig = new JSONObject();
		failConfig.put("default", "默认按状态码判断，4xx和5xx表示失败");
		successConfig.put("default", "默认按状态码判断，2xx和3xx表示成功");
		//init request
		if(type.equals("request")) {
			data = new JSONObject();
			JSONObject requestAudit = new JSONObject();
			data.put("requestAudit", requestAudit);
			requestAudit.put("integrationUuid", automaticConfigVo.getBaseIntegrationUuid());
			requestAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
			requestAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
			requestAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
			requestAudit.put("successConfig", automaticConfigVo.getBaseSuccessConfig());
			if(automaticConfigVo.getBaseSuccessConfig() == null) {
				requestAudit.put("successConfig",successConfig);
			}
			ProcessTaskStepDataVo auditDataVo = new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), ProcessStepHandler.AUTOMATIC.getHandler());
			auditDataVo.setData(data.toJSONString());
			auditDataVo.setFcu(UserContext.get().getUserUuid());
			auditDataVo.setFcu("system");
			processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
		}else {//init callback
			JSONObject callbackAudit = new JSONObject();
			callbackAudit.put("integrationUuid", automaticConfigVo.getCallbackIntegrationUuid());
			callbackAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
			callbackAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
			callbackAudit.put("type", automaticConfigVo.getCallbackType());
			callbackAudit.put("typeName", CallbackType.getText(automaticConfigVo.getCallbackType()));
			callbackAudit.put("interval", automaticConfigVo.getCallbackInterval());
			callbackAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
			callbackAudit.put("successConfig", automaticConfigVo.getCallbackSuccessConfig());
			if(automaticConfigVo.getCallbackFailConfig() == null) {
				callbackAudit.put("failConfig",failConfig);
			}
			if(automaticConfigVo.getCallbackSuccessConfig() == null) {
				callbackAudit.put("successConfig",successConfig);
			}
			data.put("callbackAudit", callbackAudit);
		}
		return data;
	}
	
	@Override
	public void initJob(AutomaticConfigVo automaticConfigVo,ProcessTaskStepVo currentProcessTaskStepVo,JSONObject data) {
		IJob jobHandler = SchedulerManager.getHandler(ProcessTaskAutomaticJob.class.getName());
		if (jobHandler != null) {
			JobObject.Builder jobObjectBuilder = new JobObject.Builder(
					currentProcessTaskStepVo.getProcessTaskId().toString()+"-"+currentProcessTaskStepVo.getId().toString(),
					jobHandler.getGroupName(), jobHandler.getClassName(), TenantContext.get().getTenantUuid()
					).addData("automaticConfigVo", automaticConfigVo)
					 .addData("data", data)
					 .addData("currentProcessTaskStepVo", currentProcessTaskStepVo);
			JobObject jobObject = jobObjectBuilder.build();
			jobHandler.reloadJob(jobObject);
		} else {
			throw new ScheduleHandlerNotFoundException(ProcessTaskAutomaticJob.class.getName());
		}
	}
	
	/**
	 * @Description: 判断条件是否成立
	 * @Param: 
	 * @return: boolean
	 */
	private Boolean predicate(JSONObject config,IntegrationResultVo resultVo,Boolean isSuccess) {
		Boolean result = false;
		if(config==null||config.isEmpty()||!config.containsKey("expression")) {
			String patternStr = "(2|3).*";
			if(!isSuccess) {
				patternStr = "(4|5).*";
			}
			Pattern pattern = Pattern.compile(patternStr);
			if(pattern.matcher(String.valueOf(resultVo.getStatusCode())).matches()) {
				result = true;
			}
		}else {
			String name = config.getString("name");
			if(StringUtils.isNotBlank(name)) {
				String resultValue = null;
				String transformedResult = resultVo.getTransformedResult();
				if(StringUtils.isNotBlank(transformedResult)) {
					JSONObject transformedResultObj = JSON.parseObject(transformedResult);
					if(MapUtils.isNotEmpty(transformedResultObj)) {
						resultValue = transformedResultObj.getString(name);
					}
				}
				if(resultValue == null) {
					String rawResult = resultVo.getRawResult();
					if(StringUtils.isNotEmpty(rawResult)) {
						JSONObject rawResultObj = JSON.parseObject(rawResult);
						if(MapUtils.isNotEmpty(rawResultObj)) {
							resultValue = rawResultObj.getString(name);
						}
					}
				}
				if(resultValue != null) {
					List<String> curentValueList = new ArrayList<>();
					curentValueList.add(resultValue);
					String value = config.getString("value");
					List<String> targetValueList = new ArrayList<>();
					targetValueList.add(value);
					String expression = config.getString("expression");
					result = ConditionUtil.predicate(curentValueList, expression, targetValueList);
				}
			}
		}
		return result;
	}
	
	/**
	 * 拼装入参数
	 * @param automaticConfigVo
	 * @return
	 */
	private JSONObject getIntegrationParam(AutomaticConfigVo automaticConfigVo,ProcessTaskStepVo currentProcessTaskStepVo) {
		ProcessTaskStepVo stepVo = getProcessTaskStepDetailInfoById(currentProcessTaskStepVo.getId());
		ProcessTaskVo processTaskVo = getProcessTaskDetailInfoById(currentProcessTaskStepVo.getProcessTaskId());
		processTaskVo.setCurrentProcessTaskStep(stepVo);
		JSONObject processTaskJson = ProcessTaskUtil.getProcessFieldData(processTaskVo,true);
		JSONObject resultJson = automaticConfigVo.getResultJson();
		JSONArray paramList =  automaticConfigVo.getBaseParamList();
		JSONObject integrationParam = new JSONObject();
		if(!automaticConfigVo.getIsRequest()) {
			paramList = automaticConfigVo.getCallbackParamList();
		}
		if(!CollectionUtils.isEmpty(paramList)) {
			for(Object paramObj : paramList) {
				JSONObject param = (JSONObject)paramObj;
				String type = param.getString("type");
				String value = param.getString("value");
				String name = param.getString("name");
				if(type.equals("common")||type.equals("form")) {
					integrationParam.put(name, processTaskJson.get(value));
				}else if(type.equals("integration")){
					integrationParam.put(name, resultJson.get(value));
				}else{//常量 
					integrationParam.put(name, value);
				}
			}
		}
		return integrationParam;
	}
		
	/**
	 * 获取工单回退步骤列表
	 * @param processTaskStepId
	 * @return
	 */
	private List<ProcessTaskStepVo> getbackStepList(Long processTaskStepId){
		List<ProcessTaskStepVo> resultList = new ArrayList<>();
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getToProcessTaskStepByFromId(processTaskStepId);
		for(ProcessTaskStepVo processTaskStep : processTaskStepList) {
			if(processTaskStep.getIsActive() != null) {
				if(ProcessFlowDirection.BACKWARD.getValue().equals(processTaskStep.getFlowDirection()) && processTaskStep.getIsActive().intValue() != 0){
					if(StringUtils.isNotBlank(processTaskStep.getAliasName())) {
						processTaskStep.setName(processTaskStep.getAliasName());
						processTaskStep.setFlowDirection("");
					}else {
						processTaskStep.setFlowDirection(ProcessFlowDirection.BACKWARD.getText());
					}
					resultList.add(processTaskStep);
				}
			}
		}
		return resultList;
	}
	
	@Override
	public ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId) {
		//获取步骤信息
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
		processTaskStepVo.setConfig(stepConfig);
		ProcessStepHandlerVo processStepHandlerVo = processStepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
		if(processStepHandlerVo != null) {
			processTaskStepVo.setGlobalConfig(processStepHandlerVo.getConfig());					
		}
		//处理人列表
		List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
		if(CollectionUtils.isNotEmpty(majorUserList)) {
			processTaskStepVo.setMajorUser(majorUserList.get(0));
		}
		List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
		processTaskStepVo.setMinorUserList(minorUserList);
		
		List<ProcessTaskStepUserVo> agentUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.AGENT.getValue());
		processTaskStepVo.setAgentUserList(agentUserList);
		
		List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepId);
		processTaskStepVo.setWorkerList(workerList);
		//回复框内容和附件暂存回显
//		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
//		processTaskStepAuditVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
//		processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
//		processTaskStepAuditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
//		processTaskStepAuditVo.setUserUuid(UserContext.get().getUserUuid(true));
//		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
//		if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
//			ProcessTaskStepAuditVo processTaskStepAudit = processTaskStepAuditList.get(processTaskStepAuditList.size() - 1);
//			processTaskStepVo.setComment(new ProcessTaskStepCommentVo(processTaskStepAudit));
//			for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAudit.getAuditDetailList()) {
//				if(ProcessTaskAuditDetailType.FORM.getValue().equals(processTaskStepAuditDetailVo.getType())) {
//					List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = JSON.parseArray(processTaskStepAuditDetailVo.getNewContent(), ProcessTaskFormAttributeDataVo.class);
//					if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
//						Map<String, Object> formAttributeDataMap = new HashMap<>();
//						for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
//							formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
//						}
//						processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
//					}
//				}
//			}
//		}
		
		//步骤评论列表
//		List<ProcessTaskStepCommentVo> processTaskStepCommentList = processTaskMapper.getProcessTaskStepCommentListByProcessTaskStepId(processTaskStepId);
//		for(ProcessTaskStepCommentVo processTaskStepComment : processTaskStepCommentList) {
//			processTaskService.parseProcessTaskStepComment(processTaskStepComment);
//		}
//		processTaskStepVo.setCommentList(processTaskStepCommentList);
		//获取当前用户有权限的所有子任务
		//子任务列表
//		if(processTaskStepVo.getIsActive().intValue() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
//			List<ProcessTaskStepSubtaskVo> subtaskList = new ArrayList<>();
//			ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
//			processTaskStepSubtaskVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
//			processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepId);
//			List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(processTaskStepSubtaskVo);
//			for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
//				String currentUser = UserContext.get().getUserUuid(true);
//				if((currentUser.equals(processTaskStepSubtask.getOwner()) && !ProcessTaskStatus.ABORTED.getValue().equals(processTaskStepSubtask.getStatus()))
//						|| (currentUser.equals(processTaskStepSubtask.getUserUuid()) && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus()))) {
//					List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtask.getId());
//					Iterator<ProcessTaskStepSubtaskContentVo> iterator = processTaskStepSubtaskContentList.iterator();
//					while(iterator.hasNext()) {
//						ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = iterator.next();
//						if(processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
//							if(ProcessTaskStepAction.CREATESUBTASK.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
//								processTaskStepSubtask.setContent(processTaskStepSubtaskContentVo.getContent());
//								iterator.remove();
//							}
//						}
//					}
//					processTaskStepSubtask.setContentList(processTaskStepSubtaskContentList);
//					subtaskList.add(processTaskStepSubtask);
//				}
//			}
//			processTaskStepVo.setProcessTaskStepSubtaskList(subtaskList);
//		}
		
		//获取可分配处理人的步骤列表				
//		ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
//		processTaskStepWorkerPolicyVo.setProcessTaskId(processTaskId);
//		List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
//		if(CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
//			List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
//			for(ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
//				if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
//					List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
//					for(String processStepUuid : processStepUuidList) {
//						if(processTaskStepVo.getProcessStepUuid().equals(processStepUuid)) {
//							List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
//							if(CollectionUtils.isEmpty(majorList)) {
//								ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
//								assignableWorkerStep.setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
//								assignableWorkerStepList.add(assignableWorkerStep);
//							}
//						}
//					}
//				}
//			}
//			processTaskStepVo.setAssignableWorkerStepList(assignableWorkerStepList);
//		}
		
		//时效列表
//		List<ProcessTaskSlaVo> processTaskSlaList = processTaskMapper.getProcessTaskSlaByProcessTaskStepId(processTaskStepId);
//		for(ProcessTaskSlaVo processTaskSlaVo : processTaskSlaList) {
//			ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaVo.getSlaTimeVo();
//			if(processTaskSlaTimeVo != null) {
//				processTaskSlaTimeVo.setName(processTaskSlaVo.getName());
//				if(processTaskSlaTimeVo.getExpireTime() != null) {
//					long timeLeft = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(), System.currentTimeMillis(), processTaskSlaTimeVo.getExpireTime().getTime());
//					processTaskSlaTimeVo.setTimeLeft(timeLeft);
//					processTaskSlaTimeVo.setTimeLeftDesc(conversionTimeUnit(timeLeft));
//				}
//				if(processTaskSlaTimeVo.getRealExpireTime() != null) {
//					long realTimeLeft = processTaskSlaTimeVo.getExpireTime().getTime() - System.currentTimeMillis();
//					processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
//					processTaskSlaTimeVo.setRealTimeLeftDesc(conversionTimeUnit(realTimeLeft));
//				}
//				processTaskStepVo.getSlaTimeList().add(processTaskSlaTimeVo);
//			}
//		}
		return processTaskStepVo;
	}
	
	@Override
	public ProcessTaskVo getProcessTaskDetailInfoById(Long processTaskId) {
		//获取工单基本信息(title、channel_uuid、config_hash、priority_uuid、status、start_time、end_time、expire_time、owner、ownerName、reporter、reporterName)
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		
		//获取工单流程图信息
		ProcessTaskConfigVo processTaskConfig = processTaskMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash());
		if(processTaskConfig == null) {
			throw new ProcessTaskRuntimeException("没有找到工单：'" + processTaskId + "'的流程图配置信息");
		}
		processTaskVo.setConfig(processTaskConfig.getConfig());
		//获取开始步骤id
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}

		ProcessTaskStepVo startProcessTaskStepVo = processTaskStepList.get(0);
		String startStepConfig = processTaskMapper.getProcessTaskStepConfigByHash(startProcessTaskStepVo.getConfigHash());
		startProcessTaskStepVo.setConfig(startStepConfig);
		ProcessStepHandlerVo processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerByHandler(startProcessTaskStepVo.getHandler());
		if(processStepHandlerConfig != null) {
			startProcessTaskStepVo.setGlobalConfig(processStepHandlerConfig.getConfig());					
		}
		Long startProcessTaskStepId = startProcessTaskStepVo.getId();
		ProcessTaskStepCommentVo comment = new ProcessTaskStepCommentVo();
		//获取上报描述内容
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepId);
		if(!processTaskStepContentList.isEmpty()) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo != null) {
				comment.setContent(processTaskContentVo.getContent());
			}
		}
		//附件
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(processTaskId);
		processTaskFileVo.setProcessTaskStepId(startProcessTaskStepId);
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		
		if(processTaskFileList.size() > 0) {
			List<String> fileUuidList = new ArrayList<>();
			List<FileVo> fileList = new ArrayList<>();
			for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
				fileUuidList.add(processTaskFile.getFileUuid());
				FileVo fileVo = fileMapper.getFileByUuid(processTaskFile.getFileUuid());
				fileList.add(fileVo);
			}
			comment.setFileList(fileList);
		}
		startProcessTaskStepVo.setComment(comment);
		processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);
		
		//优先级
		PriorityVo priorityVo = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
		if(priorityVo == null) {
			priorityVo = new PriorityVo();
			priorityVo.setUuid(processTaskVo.getPriorityUuid());
		}
		processTaskVo.setPriority(priorityVo);
		//上报服务路径
		ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
		if(channelVo != null) {
			StringBuilder channelPath = new StringBuilder();
			List<String> ancestorNameList = channelMapper.getAllAncestorNameListByParentUuid(channelVo.getParentUuid());
			for(String name : ancestorNameList) {
				channelPath.append(name);
				channelPath.append("/");
			}
			channelPath.append(channelVo.getName());
			processTaskVo.setChannelPath(channelPath.toString());
			processTaskVo.setChannelType(channelMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid()));
		}
		//耗时
		if(processTaskVo.getEndTime() != null) {
			long timeCost = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(), processTaskVo.getStartTime().getTime(), processTaskVo.getEndTime().getTime());
			processTaskVo.setTimeCost(timeCost);
		}
		
		//获取工单表单信息
		ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
		if(processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContent())) {
			processTaskVo.setFormConfig(processTaskFormVo.getFormContent());			
			List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
			if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
				Map<String, Object> formAttributeDataMap = new HashMap<>();
				for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
					formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
				}
				processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
			}
		}
		return processTaskVo;
	}
	
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("(5|4).*");
		System.out.println( pattern.matcher("300").matches());
	}
}
