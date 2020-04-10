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
import codedriver.framework.process.constvalue.FormAttributeAction;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
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
	private TeamMapper teamMapper;
	
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
		String content = paramObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
			processTaskMapper.replaceProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), processTaskContentVo.getHash()));
			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), processTaskContentVo.getHash());
		}
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
			processTaskStepUserVo.setUserId(processTaskStepSubtaskVo.getUserId());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), processTaskStepSubtaskVo.getUserId()));
			currentProcessTaskStepVo.setParamObj(paramObj);
			handler.updateProcessTaskStepUserAndWorker(workerList, userList);	
			//记录活动
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.CREATESUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
		
	}

	@Override
	public void editSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		JSONObject paramObj = processTaskStepSubtaskVo.getParamObj();
		//TODO linbq查出旧数据
		ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = processTaskMapper.getProcessTaskStepSubtaskContentById(processTaskStepSubtaskVo.getId());
		if(processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), processTaskStepSubtaskContentVo.getContentHash());
		}
		String content = paramObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
			processTaskMapper.replaceProcessTaskStepSubtaskContent(new ProcessTaskStepSubtaskContentVo(processTaskStepSubtaskVo.getId(), processTaskContentVo.getHash()));
			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), processTaskContentVo.getHash());
		}
		
		ProcessTaskStepVo currentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepSubtaskVo.getProcessTaskStepId());
		if(currentProcessTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepSubtaskVo.getProcessTaskStepId().toString());
		}
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		if(handler != null) {
			currentProcessTaskStepVo.setParamObj(paramObj);
			String oldUserId = paramObj.getString("oldUserId");
			if(!processTaskStepSubtaskVo.getUserId().equals(oldUserId)) {//更新了处理人
				
				List<ProcessTaskStepUserVo> userList = new ArrayList<>();
				ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
				processTaskStepUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
				processTaskStepUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
				processTaskStepUserVo.setUserId(processTaskStepSubtaskVo.getUserId());
				processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
				processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
				userList.add(processTaskStepUserVo);
				ProcessTaskStepUserVo oldUserVo = new ProcessTaskStepUserVo();
				oldUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
				oldUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
				oldUserVo.setUserId(oldUserId);
				oldUserVo.setUserName(paramObj.getString("oldUserName"));
				oldUserVo.setUserType(ProcessUserType.MINOR.getValue());
				userList.add(oldUserVo);
				
				List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
				workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), processTaskStepSubtaskVo.getUserId()));
				workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), oldUserId));
				handler.updateProcessTaskStepUserAndWorker(workerList, userList);
			}
				
			//记录活动
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.EDITSUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
	}

	@Override
	public void redoSubtask(ProcessTaskStepSubtaskVo processTaskStepSubtaskVo) {
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		
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
			processTaskStepUserVo.setUserId(processTaskStepSubtaskVo.getUserId());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), processTaskStepSubtaskVo.getUserId()));
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
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		
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
			processTaskStepUserVo.setUserId(processTaskStepSubtaskVo.getUserId());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), processTaskStepSubtaskVo.getUserId()));
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
		processTaskStepSubtaskVo.setStatus(ProcessTaskStatus.ABORTED.getValue());
		processTaskStepSubtaskVo.setCancelUser(UserContext.get().getUserId(true));
		processTaskMapper.updateProcessTaskStepSubtaskStatus(processTaskStepSubtaskVo);
		
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
			processTaskStepUserVo.setUserId(processTaskStepSubtaskVo.getUserId());
			processTaskStepUserVo.setUserName(processTaskStepSubtaskVo.getUserName());
			processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
			userList.add(processTaskStepUserVo);
			List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
			workerList.add(new ProcessTaskStepWorkerVo(processTaskStepSubtaskVo.getProcessTaskId(), processTaskStepSubtaskVo.getProcessTaskStepId(), processTaskStepSubtaskVo.getUserId()));
			currentProcessTaskStepVo.setParamObj(processTaskStepSubtaskVo.getParamObj());
			handler.updateProcessTaskStepUserAndWorker(workerList, userList);	
			//记录活动
			handler.activityAudit(currentProcessTaskStepVo, ProcessTaskStepAction.ABORTSUBTASK);
		}else {
			throw new ProcessStepHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
		}
	}
	
	@Override
	public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo, Map<String, String> formAttributeActionMap, int mode) {
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
							if(UserContext.get().getUserId(true).equals(processTaskVo.getOwner())) {
								currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
							}
							if(UserContext.get().getUserId(true).equals(processTaskVo.getReporter())) {
								currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
							}
							currentUserTeamList = teamMapper.getTeamUuidListByUserId(UserContext.get().getUserId(true));
						}else if(mode == 1){
							if(formAttributeActionMap == null) {
								formAttributeActionMap = new HashMap<>();
							}
						}
						
						for(int i = 0; i < controllerList.size(); i++) {
							JSONObject attributeObj = controllerList.getJSONObject(i);
							String action = null;
							if(mode == 0) {
								JSONObject config = attributeObj.getJSONObject("config");
								if(MapUtils.isNotEmpty(config)) {
									List<String> authorityList = JSON.parseArray(config.getString("authorityConfig"), String.class);
									if(CollectionUtils.isNotEmpty(authorityList)) {
										action = FormAttributeAction.HIDE.getValue();
										for(String authority : authorityList) {
											String[] split = authority.split("#");
											if(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
												if(currentUserProcessUserTypeList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}
											if(GroupSearch.USER.getValue().equals(split[0])) {
												if(UserContext.get().getUserId(true).equals(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}
											if(GroupSearch.TEAM.getValue().equals(split[0])) {
												if(currentUserTeamList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}
											if(GroupSearch.ROLE.getValue().equals(split[0])) {
												if(UserContext.get().getRoleNameList().contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}
										}
									}else {
										action = FormAttributeAction.READ.getValue();
									}
								}
							}else if(mode == 1){
								action = formAttributeActionMap.get(attributeObj.getString("uuid"));
							}
							if(FormAttributeAction.READ.getValue().equals(action)) {
								attributeObj.put("isReadonly", true);
							}else if(FormAttributeAction.HIDE.getValue().equals(action)) {
								attributeObj.put("isHide", true);
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
}
