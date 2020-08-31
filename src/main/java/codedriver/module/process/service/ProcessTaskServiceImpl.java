package codedriver.module.process.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.FormAttributeAction;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.constvalue.automatic.CallbackType;
import codedriver.framework.process.constvalue.automatic.FailPolicy;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskConfigVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskSlaVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFileVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.integration.handler.ProcessRequestFrom;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
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
    
    @Autowired
    private CatalogMapper catalogMapper;

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;

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
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());
			//记录活动
			ProcessTaskStepSubtaskVo subtaskVo = new ProcessTaskStepSubtaskVo();
			subtaskVo.setId(processTaskStepSubtaskVo.getId());
			subtaskVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
			subtaskVo.setUserName(processTaskStepSubtaskVo.getUserName());
			subtaskVo.setTargetTime(processTaskStepSubtaskVo.getTargetTime());
			subtaskVo.setContentHash(processTaskContentVo.getHash());
			paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getParamName(), JSON.toJSONString(subtaskVo));
			currentProcessTaskStepVo.setParamObj(paramObj);
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskAuditType.CREATESUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.CREATESUBTASK);
		}else {
			throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
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
		
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			if(!processTaskStepSubtaskVo.getUserUuid().equals(oldProcessTaskStepSubtask.getUserUuid())) {//更新了处理人
				handler.updateProcessTaskStepUserAndWorker(oldProcessTaskStepSubtask.getProcessTaskId(), oldProcessTaskStepSubtask.getProcessTaskStepId());
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
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskAuditType.EDITSUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.EDITSUBTASK);
		}else {
			throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
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
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());
			//记录活动
			currentProcessTaskStepVo.setParamObj(processTaskStepSubtaskVo.getParamObj());
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskAuditType.REDOSUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.REDOSUBTASK);
		}else {
			throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
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
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());
			//记录活动
			currentProcessTaskStepVo.setParamObj(processTaskStepSubtaskVo.getParamObj());
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskAuditType.COMPLETESUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.COMPLETESUBTASK);
		}else {
			throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
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
		
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {	
			handler.updateProcessTaskStepUserAndWorker(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId());	
			//记录活动
			currentProcessTaskStepVo.setParamObj(processTaskStepSubtaskVo.getParamObj());
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskAuditType.ABORTSUBTASK);
			currentProcessTaskStepVo.setCurrentSubtaskId(processTaskStepSubtaskVo.getId());
			handler.notify(currentProcessTaskStepVo, NotifyTriggerType.ABORTSUBTASK);
		}else {
			throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
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
	public void parseProcessTaskStepReply(ProcessTaskStepReplyVo processTaskStepReplyVo) {
		if(StringUtils.isBlank(processTaskStepReplyVo.getContent()) && StringUtils.isNotBlank(processTaskStepReplyVo.getContentHash())) {
		    processTaskStepReplyVo.setContent(processTaskMapper.getProcessTaskContentStringByHash(processTaskStepReplyVo.getContentHash()));
		}
		List<Long> fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepReplyVo.getId());
		if(CollectionUtils.isNotEmpty(fileIdList)) {
            processTaskStepReplyVo.setFileIdList(fileIdList);
            processTaskStepReplyVo.setFileList(fileMapper.getFileListByIdList(fileIdList));
        }			
		if(StringUtils.isNotBlank(processTaskStepReplyVo.getLcu())) {
			UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepReplyVo.getLcu());
			if(user != null) {
			    processTaskStepReplyVo.setLcuName(user.getUserName());
			}
		}
		UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepReplyVo.getFcu());
		if(user != null) {
		    processTaskStepReplyVo.setFcuName(user.getUserName());
		}
	}
	
	@Override
	public Boolean runRequest(AutomaticConfigVo automaticConfigVo,ProcessTaskStepVo currentProcessTaskStepVo) {
		IntegrationResultVo resultVo = null;
		Boolean isUnloadJob = false;
		ProcessTaskStepDataVo auditDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),currentProcessTaskStepVo.getId(),ProcessTaskStepDataType.AUTOMATIC.getValue()));
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
			resultVo = handler.sendRequest(integrationVo,ProcessRequestFrom.PROCESS);
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
					audit.put("failedReason","");
					if(FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						List<ProcessTaskStepVo> backStepList = getbackStepList(currentProcessTaskStepVo.getId());
						if(backStepList.size() == 1) {
							ProcessTaskStepVo nextProcessTaskStepVo = backStepList.get(0);
							if (processHandler != null) {
								JSONObject jsonParam = new JSONObject();
								jsonParam.put("action", ProcessTaskStepAction.BACK.getValue());
								jsonParam.put("nextStepId", nextProcessTaskStepVo.getId());
								currentProcessTaskStepVo.setParamObj(jsonParam);
								processHandler.complete(currentProcessTaskStepVo);
							}
						}else {//如果存在多个回退线，则挂起
							//processHandler.hang(currentProcessTaskStepVo);
						}
					}else if(FailPolicy.KEEP_ON.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						processHandler.complete(currentProcessTaskStepVo);
					}else if(FailPolicy.CANCEL.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						processHandler.abortProcessTask(new ProcessTaskVo(currentProcessTaskStepVo.getProcessTaskId()));
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
			if(resultVo != null && StringUtils.isNotEmpty(resultVo.getError())) {
				audit.put("failedReason",resultVo.getError());
			}else {
				StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        ex.printStackTrace(pw);
				audit.put("failedReason",sw.toString());
			}
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
			ProcessTaskStepDataVo auditDataVo = new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), ProcessTaskStepDataType.AUTOMATIC.getValue());
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
		ProcessTaskVo processTaskVo = getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
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
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId,null);
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

		return processTaskStepVo;
	}
	
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("(5|4).*");
		System.out.println( pattern.matcher("300").matches());
	}

//	@Override
//	public Map<String, String> getCustomButtonTextMap(Long processTaskStepId) {
//		Map<String, String> customButtonMap = new HashMap<>();
//		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
//		if(processTaskStepVo == null) {
//			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
//		}
//		/** 节点管理按钮映射 **/
//		ProcessStepHandlerVo processStepHandlerVo = processStepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
//		if(processStepHandlerVo != null) {
//			JSONObject globalConfig = processStepHandlerVo.getConfig();
//			if(MapUtils.isNotEmpty(globalConfig)) {
//				JSONArray customButtonList = globalConfig.getJSONArray("customButtonList");
//				if(CollectionUtils.isNotEmpty(customButtonList)) {
//					for(int i = 0; i < customButtonList.size(); i++) {
//						JSONObject customButton = customButtonList.getJSONObject(i);
//						String value = customButton.getString("value");
//						if(StringUtils.isNotBlank(value)) {
//							customButtonMap.put(customButton.getString("name"), value);
//						}
//					}
//				}
//			}
//		}
//		/** 节点设置按钮映射 **/
//		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
//		JSONObject stepConfigObj = JSON.parseObject(stepConfig);
//		if(MapUtils.isNotEmpty(stepConfigObj)) {
//			JSONArray customButtonList = stepConfigObj.getJSONArray("customButtonList");
//			if(CollectionUtils.isNotEmpty(customButtonList)) {
//				for(int i = 0; i < customButtonList.size(); i++) {
//					JSONObject customButton = customButtonList.getJSONObject(i);
//					String value = customButton.getString("value");
//					if(StringUtils.isNotBlank(value)) {
//						customButtonMap.put(customButton.getString("name"), value);
//					}
//				}
//			}
//		}
//		return customButtonMap;
//	}

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if(processTaskVo == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        if(processTaskStepId != null) {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if(processTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
            }
            IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if(processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
            }
            processTaskVo.setCurrentProcessTaskStep(processTaskStepVo);
        }
        if(nextStepId != null) {
            ProcessTaskStepVo nextProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(nextStepId);
            if(nextProcessTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(nextStepId.toString());
            }
            IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(nextProcessTaskStepVo.getHandler());
            if(processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(nextProcessTaskStepVo.getHandler());
            }
            if(!processTaskId.equals(nextProcessTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskRuntimeException("步骤：'" + nextStepId + "'不是工单：'" + processTaskId + "'的步骤");
            }
        }
        return processTaskVo;
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId) {
        return checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId, null);
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId) {
        return checkProcessTaskParamsIsLegal(processTaskId, null, null);
    }

    @Override
    public ProcessTaskVo getProcessTaskDetailById(Long processTaskId) {
      //获取工单基本信息(title、channel_uuid、config_hash、priority_uuid、status、start_time、end_time、expire_time、owner、ownerName、reporter、reporterName)
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        //获取工单流程图信息
        ProcessTaskConfigVo processTaskConfig = processTaskMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash());
        if(processTaskConfig == null) {
            throw new ProcessTaskRuntimeException("没有找到工单：'" + processTaskId + "'的流程图配置信息");
        }
        processTaskVo.setConfig(processTaskConfig.getConfig());
        
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
            CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelVo.getParentUuid());
            if(catalogVo != null) {
                List<CatalogVo> catalogList = catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
                List<String> nameList = catalogList.stream().map(CatalogVo::getName).collect(Collectors.toList());
                nameList.add(channelVo.getName());
                processTaskVo.setChannelPath(String.join("/", nameList));
            }
            ChannelTypeVo channelTypeVo =  channelMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
            if(channelTypeVo == null) {
                channelTypeVo = new ChannelTypeVo();
                channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
            }
            processTaskVo.setChannelType(channelTypeVo);
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
            for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                processTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
            }
        }
        /** 上报人公司列表 **/
        List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(processTaskVo.getOwner());
        if(CollectionUtils.isNotEmpty(teamUuidList)) {
            List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
            for(TeamVo teamVo : teamList) {
                List<TeamVo> companyList = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht(), TeamLevel.COMPANY.getValue());
                if(CollectionUtils.isNotEmpty(companyList)) {
                    processTaskVo.getOwnerCompanyList().addAll(companyList);
                }
            }
        }
        return processTaskVo;
    }

    @Override
    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepReplyVo> processTaskStepReplyList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
            if(ProcessTaskStepAction.COMMENT.getValue().equals(processTaskStepContentVo.getType())) {
                ProcessTaskStepReplyVo processTaskStepReplyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
                parseProcessTaskStepReply(processTaskStepReplyVo);
                processTaskStepReplyList.add(processTaskStepReplyVo);
            }
        }
        processTaskStepReplyList.sort((e1, e2) -> e1.getId().compareTo(e2.getId()));
        return processTaskStepReplyList;
    }

    @Override
    public List<ProcessTaskStepSubtaskVo> getProcessTaskStepSubtaskListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
            List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtask.getId());
            Iterator<ProcessTaskStepSubtaskContentVo> iterator = processTaskStepSubtaskContentList.iterator();
            while(iterator.hasNext()) {
                ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = iterator.next();
                if(processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
                    if(ProcessTaskStepAction.CREATESUBTASK.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
                        processTaskStepSubtask.setContent(processTaskStepSubtaskContentVo.getContent());
                        iterator.remove();
                    }
                }
            }
            processTaskStepSubtask.setContentList(processTaskStepSubtaskContentList);
        }
        return processTaskStepSubtaskList;
    }

    @Override
    public List<ProcessTaskStepVo> getAssignableWorkerStepListByProcessTaskIdAndProcessStepUuid(Long processTaskId,
        String processStepUuid) {
        ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        processTaskStepWorkerPolicyVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
        if(CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
            List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
            for(ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
                if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
                    for(String stepUuid : processStepUuidList) {
                        if(processStepUuid.equals(stepUuid)) {
                            List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                            if(CollectionUtils.isEmpty(majorList)) {
                                ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                                assignableWorkerStep.setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
                                assignableWorkerStepList.add(assignableWorkerStep);
                            }
                        }
                    }
                }
            }
            return assignableWorkerStepList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(Long processTaskStepId,
        String worktimeUuid) {
        List<ProcessTaskSlaTimeVo> slaTimeList = new ArrayList<>();
        List<ProcessTaskSlaVo> processTaskSlaList = processTaskMapper.getProcessTaskSlaByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskSlaVo processTaskSlaVo : processTaskSlaList) {
            ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaVo.getSlaTimeVo();
            if(processTaskSlaTimeVo != null) {
                processTaskSlaTimeVo.setName(processTaskSlaVo.getName());
                if(processTaskSlaTimeVo.getExpireTime() != null) {
                    long timeLeft = 0L;
                    long nowTime = System.currentTimeMillis();
                    long expireTime = processTaskSlaTimeVo.getExpireTime().getTime();
                    if(nowTime < expireTime) {
                        timeLeft = worktimeMapper.calculateCostTime(worktimeUuid, nowTime, expireTime);
                    }else if(nowTime > expireTime) {
                        timeLeft = -worktimeMapper.calculateCostTime(worktimeUuid, expireTime, nowTime);
                    }                   
                    processTaskSlaTimeVo.setTimeLeft(timeLeft);
                }
                if(processTaskSlaTimeVo.getRealExpireTime() != null) {
                    long realTimeLeft = processTaskSlaTimeVo.getRealExpireTime().getTime() - System.currentTimeMillis();
                    processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
                }
                slaTimeList.add(processTaskSlaTimeVo);
            }
        }
        return slaTimeList;
    }

    @Override
    public void setNextStepList(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepVo.getId(), null);
        for(ProcessTaskStepVo processTaskStep : nextStepList) {
            if(processTaskStep.getIsActive() != null) {
                if(ProcessFlowDirection.FORWARD.getValue().equals(processTaskStep.getFlowDirection())) {
                    if(StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                        processTaskStep.setName(processTaskStep.getAliasName());
                        processTaskStep.setFlowDirection("");
                    }else {
                        processTaskStep.setFlowDirection(ProcessFlowDirection.FORWARD.getText());
                    }
                    processTaskStepVo.getForwardNextStepList().add(processTaskStep);
                }else if(ProcessFlowDirection.BACKWARD.getValue().equals(processTaskStep.getFlowDirection()) && processTaskStep.getIsActive().intValue() != 0){
                    if(StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                        processTaskStep.setName(processTaskStep.getAliasName());
                        processTaskStep.setFlowDirection("");
                    }else {
                        processTaskStep.setFlowDirection(ProcessFlowDirection.BACKWARD.getText());
                    }
                    processTaskStepVo.getBackwardNextStepList().add(processTaskStep);
                }
            }
        }
        
    }

    @Override
    public void setProcessTaskStepUser(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
        if(CollectionUtils.isNotEmpty(majorUserList)) {
            processTaskStepVo.setMajorUser(majorUserList.get(0));
        }else {
            processTaskStepVo.setWorkerList(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
        }
        processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MINOR.getValue()));
        processTaskStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.AGENT.getValue()));
    }

    @Override
    public void setProcessTaskStepConfig(ProcessTaskStepVo processTaskStepVo) {
        String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        processTaskStepVo.setConfig(stepConfig);
        ProcessStepHandlerVo processStepHandlerConfig = stepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
        if(processStepHandlerConfig != null) {
            processTaskStepVo.setGlobalConfig(processStepHandlerConfig.getConfig());                    
        }
    }

    @Override
    public ProcessTaskStepReplyVo getProcessTaskStepContentAndFileByProcessTaskStepIdId(Long processTaskStepId) {
        ProcessTaskStepReplyVo comment = new ProcessTaskStepReplyVo();
        //获取上报描述内容
        List<Long> fileIdList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
            if (ProcessTaskStepAction.STARTPROCESS.getValue().equals(processTaskStepContent.getType())) {
                fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepContent.getId());
                comment.setContent(processTaskMapper.getProcessTaskContentStringByHash(processTaskStepContent.getContentHash()));
                break;
            }
        }
        //附件
        if(CollectionUtils.isNotEmpty(fileIdList)) {
            comment.setFileList(fileMapper.getFileListByIdList(fileIdList));
        }
        return comment;
    }

    @Override
    public boolean saveProcessTaskStepReply(JSONObject jsonObj, ProcessTaskStepReplyVo oldReplyVo) {
        Long processTaskId = oldReplyVo.getProcessTaskId();
        Long processTaskStepId = oldReplyVo.getProcessTaskStepId();
        boolean isUpdate = false;
      //获取上传附件uuid
        List<Long> oldFileIdList = new ArrayList<>();
        //获取上报描述内容hash
        String oldContentHash = null;
        Long oldContentId = null;
        if(oldReplyVo.getId() != null) {
            parseProcessTaskStepReply(oldReplyVo);
            oldContentId = oldReplyVo.getId();
            oldContentHash = oldReplyVo.getContentHash();
            oldFileIdList = oldReplyVo.getFileIdList();
        }
        /** 保存新附件uuid **/
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        if(fileIdList == null) {
            fileIdList = new ArrayList<>();
        }
        String content = jsonObj.getString("content");
        if(StringUtils.isNotBlank(content)) {
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
            if(Objects.equals(oldContentHash, contentVo.getHash())) {
                jsonObj.remove("content");
            }else {
                isUpdate = true;
                jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
                processTaskMapper.replaceProcessTaskContent(contentVo);
                if(oldContentId == null) {
                    processTaskMapper.insertProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskStepAction.STARTPROCESS.getValue()));
                }else {
                    processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, contentVo.getHash()));
                }
            }
        }else if(oldContentHash != null){
            isUpdate = true;
            jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
            if(CollectionUtils.isEmpty(fileIdList)) {
                processTaskMapper.deleteProcessTaskStepContentById(oldContentId);
            }else {
                processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, null));
            }
        }else {
            jsonObj.remove("content");
        }
        
        if(Objects.equals(oldFileIdList, fileIdList)) {
            jsonObj.remove("fileIdList");
        }else {
            isUpdate = true;
            processTaskMapper.deleteProcessTaskStepFileByContentId(oldContentId);
            ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(JSON.toJSONString(oldFileIdList));
            processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
            jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), processTaskContentVo.getHash());
            /** 保存附件uuid **/
            if(CollectionUtils.isNotEmpty(fileIdList)) {
                ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
                processTaskStepFileVo.setProcessTaskId(processTaskId);
                processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
                processTaskStepFileVo.setContentId(oldContentId);
                for (Long fileId : fileIdList) {
                    if(fileMapper.getFileById(fileId) == null) {
                        throw new ProcessTaskRuntimeException("上传附件id:'" + fileId + "'不存在");
                    }
                    processTaskStepFileVo.setFileId(fileId);
                    processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
                }
            }
        }
        return isUpdate;
    }
    
    /**
     * 
     * @Time:2020年4月2日
     * @Description: 检查当前用户是否配置该权限
     * @param processTaskStepVo
     * @param operationType 
     * @return boolean
     */
    @Override
    public boolean checkOperationAuthIsConfigured(ProcessTaskStepVo processTaskStepVo, ProcessTaskOperationType operationType) {
//        String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
//        JSONObject stepConfigObj = JSON.parseObject(stepConfig);
//        JSONArray authorityList = stepConfigObj.getJSONArray("authorityList");
//        // 如果步骤自定义权限设置为空，则用组件的全局权限设置
//        if (CollectionUtils.isEmpty(authorityList)) {
//            ProcessStepHandlerVo processStepHandlerVo = processStepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
//            if(processStepHandlerVo != null) {
//                JSONObject handlerConfigObj = processStepHandlerVo.getConfig();
//                if(MapUtils.isNotEmpty(handlerConfigObj)) {
//                    authorityList = handlerConfigObj.getJSONArray("authorityList");
//                }
//            }
//        }
        JSONObject configObj = processTaskStepVo.getConfigObj();
        JSONArray authorityList = configObj.getJSONArray("authorityList");
        if (CollectionUtils.isEmpty(authorityList)) {
            JSONObject globalConfig = processTaskStepVo.getGlobalConfig();
            authorityList = globalConfig.getJSONArray("authorityList");
        }
        // 如果步骤自定义权限设置为空，则用组件的全局权限设置
        if (CollectionUtils.isNotEmpty(authorityList)) {
            for (int i = 0; i < authorityList.size(); i++) {
                JSONObject authorityObj = authorityList.getJSONObject(i);
                String action = authorityObj.getString("action");
                if(operationType.getValue().equals(action)) {
                    JSONArray acceptList = authorityObj.getJSONArray("acceptList");
                    if (CollectionUtils.isNotEmpty(acceptList)) {
                        List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
                        ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                        processTaskStepUserVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
                        processTaskStepUserVo.setProcessTaskStepId(processTaskStepVo.getId());
                        processTaskStepUserVo.setUserUuid(UserContext.get().getUserUuid(true));
                        for (int j = 0; j < acceptList.size(); j++) {
                            String accept = acceptList.getString(j);
                            String[] split = accept.split("#");
                            if (GroupSearch.COMMON.getValue().equals(split[0])) {
                                if (UserType.ALL.getValue().equals(split[1])) {
                                    return true;
                                }
                            } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
//                                if(ProcessUserType.OWNER.getValue().equals(split[1])) {
//                                    if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
//                                        return true;
//                                    }
//                                }else if(ProcessUserType.REPORTER.getValue().equals(split[1])) {
//                                    if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
//                                        return true;
//                                    }
//                                }else if(ProcessUserType.MAJOR.getValue().equals(split[1])) {
//                                    processTaskStepUserVo.setUserType(ProcessUserType.MAJOR.getValue());
//                                    if(processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
//                                        return true;
//                                    }
//                                }else if(ProcessUserType.MINOR.getValue().equals(split[1])) {
//                                    processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
//                                    if(processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
//                                        return true;
//                                    }
//                                }else if(ProcessUserType.AGENT.getValue().equals(split[1])) {
//                                    processTaskStepUserVo.setUserType(ProcessUserType.AGENT.getValue());
//                                    if(processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
//                                        return true;
//                                    }
//                                }
                                if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.USER.getValue().equals(split[0])) {
                                if (UserContext.get().getUserUuid(true).equals(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                if (currentUserTeamList.contains(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                if (UserContext.get().getRoleUuidList().contains(split[1])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 
     * @Time:2020年4月3日
     * @Description: 获取工单中当前用户能撤回的步骤列表
     * @param processTaskId
     * @return Set<ProcessTaskStepVo>
     */
    @Override
    public Set<ProcessTaskStepVo> getRetractableStepListByProcessTaskId(Long processTaskId) {
        Set<ProcessTaskStepVo> resultSet = new HashSet<>();
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
        for (ProcessTaskStepVo stepVo : processTaskStepList) {
            /** 找到所有已激活步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                resultSet.addAll(getRetractableStepListByProcessTaskStepId(stepVo.getId()));
            }
        }
        return resultSet;
    }
    
    /**
     * 
     * @Author: 14378
     * @Time:2020年4月3日
     * @Description: 获取当前步骤的前置步骤列表中处理人是当前用户的步骤列表
     * @param processTaskStepId 已激活的步骤id
     * @return List<ProcessTaskStepVo>
     */
    private List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        /** 所有前置步骤 **/
        List<ProcessTaskStepVo> fromStepList = processTaskMapper.getFromProcessTaskStepByToId(processTaskStepId);
        /** 找到所有已完成步骤 **/
        for (ProcessTaskStepVo fromStep : fromStepList) {
            IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(fromStep.getHandler());
            if (handler != null) {
                if (ProcessStepMode.MT == handler.getMode()) {// 手动处理节点
                    // 获取步骤处理人
                    List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(fromStep.getId(), ProcessUserType.MAJOR.getValue());
                    List<String> majorUserUuidList = majorUserList.stream().map(ProcessTaskStepUserVo::getUserUuid).collect(Collectors.toList());
                    if (majorUserUuidList.contains(UserContext.get().getUserUuid(true))) {
                        resultList.add(fromStep);
                    }
                } else {// 自动处理节点，继续找前置节点
                    resultList.addAll(getRetractableStepListByProcessTaskStepId(fromStep.getId()));
                }
            } else {
                throw new ProcessStepHandlerNotFoundException(fromStep.getHandler());
            }
        }
        return resultList;
    }
    /**
     * 
     * @Time:2020年4月3日
     * @Description: 获取工单中当前用户能处理的步骤列表
     * @param processTaskId
     * @return List<ProcessTaskStepVo>
     */
    @Override
    public List<ProcessTaskStepVo> getProcessableStepList(Long processTaskId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
        for (ProcessTaskStepVo stepVo : processTaskStepList) {
            /** 找到所有已激活未处理的步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(stepVo.getId());
                for (ProcessTaskStepWorkerVo processTaskStepWorkerVo : processTaskStepWorkerList) {
                    if (GroupSearch.USER.getValue().equals(processTaskStepWorkerVo.getType()) && UserContext.get().getUserUuid(true).equals(processTaskStepWorkerVo.getUuid())) {
                        resultList.add(stepVo);
                        break;
                    } else if (GroupSearch.TEAM.getValue().equals(processTaskStepWorkerVo.getType()) && currentUserTeamList.contains(processTaskStepWorkerVo.getUuid())) {
                        resultList.add(stepVo);
                        break;
                    } else if (GroupSearch.ROLE.getValue().equals(processTaskStepWorkerVo.getType()) && UserContext.get().getRoleUuidList().contains(processTaskStepWorkerVo.getUuid())) {
                        resultList.add(stepVo);
                        break;
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 
     * @Time:2020年4月18日
     * @Description: 获取工单中当前用户能催办的步骤列表
     * @param processTaskId
     * @return List<ProcessTaskStepVo>
     */
    @Override
    public List<ProcessTaskStepVo> getUrgeableStepList(Long processTaskId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> startProcessTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
        processTaskStepList.addAll(startProcessTaskStepList);
        for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
            if (processTaskStep.getIsActive().intValue() == 1) {
//                List<String> currentUserProcessUserTypeList = getCurrentUserProcessUserTypeList(processTaskVo, processTaskStep.getId());
//                List<String> actionList = new ArrayList<>();
//                actionList.add(ProcessTaskStepAction.URGE.getValue());
//                List<String> configActionList = getProcessTaskStepConfigActionList(processTaskVo, processTaskStep, actionList, currentUserProcessUserTypeList);
//                if (configActionList.contains(ProcessTaskStepAction.URGE.getValue())) {
//                    resultList.add(processTaskStep);
//                }
                if(checkOperationAuthIsConfigured(processTaskStep, ProcessTaskOperationType.URGE)) {
                    resultList.add(processTaskStep);
                }
            }
        }
        return resultList;
    }
}
