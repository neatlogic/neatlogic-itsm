package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.FormAttributeAction;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;

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
		//插入子任务
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		processTaskMapper.insertProcessTaskStepSubtask(processTaskStepSubtaskVo);
		JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
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
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
		
	}

	@Override
	public void editSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
		}else if(currentProcessTaskStepVo.getIsActive().intValue() != 1){
			throw new ProcessTaskRuntimeException("步骤未激活，不能处理子任务");
		}
		
		JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
		String content = paramObj.getString("content");
		ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
		processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
		processTaskStepSubtaskVo.setContentHash(processTaskContentVo.getHash());
		
		//TODO linbq查出旧数据
		ProcessTaskStepSubtaskVo oldProcessTaskStepSubtask = processTaskMapper.getProcessTaskStepSubtaskById(processTaskStepSubtaskVo.getId());
		List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtaskVo.getId());
		for(ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo : processTaskStepSubtaskContentList) {
			if(processTaskStepSubtaskContentVo != null 
					&& processTaskStepSubtaskContentVo.getContentHash() != null 
					&& ProcessTaskStepAction.CREATESUBTASK.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
				oldProcessTaskStepSubtask.setContentHash(processTaskStepSubtaskContentVo.getContentHash());
				processTaskMapper.updateProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskContentVo.getId(), processTaskContentVo.getHash()));
			}
		}
		
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		
		if(processTaskStepSubtaskVo.equals(oldProcessTaskStepSubtask)) {//如果子任务信息没有被修改，则不进行下面操作
			return;
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			String oldUserUuid = paramObj.getString("oldUserUuid");
			if(!processTaskStepSubtaskVo.getUserUuid().equals(oldUserUuid)) {//更新了处理人
				
				List<ProcessTaskStepUserVo> userList = new ArrayList<>();
				ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
				processTaskStepUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
				processTaskStepUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
				processTaskStepUserVo.setUserUuid(processTaskStepSubtaskVo.getUserUuid());
				processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
				processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
				userList.add(processTaskStepUserVo);
				ProcessTaskStepUserVo oldUserVo = new ProcessTaskStepUserVo();
				oldUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
				oldUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
				oldUserVo.setUserUuid(oldUserUuid);
				oldUserVo.setUserName(paramObj.getString("oldUserName"));
				oldUserVo.setUserType(ProcessUserType.MINOR.getValue());
				userList.add(oldUserVo);
				
				List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
				workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), GroupSearch.USER.getValue(), processTaskStepSubtaskVo.getUserUuid()));
				workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), GroupSearch.USER.getValue(), oldUserUuid));
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
			paramObj.put(ProcessTaskAuditDetailType.SUBTASK.getOldDataParamName(), JSON.toJSONString(oldSubtaskVo));
			currentProcessTaskStepVo.setParamObj(paramObj);
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.EDITSUBTASK);
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
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
	}

	@Override
	public void commentSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
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
}
