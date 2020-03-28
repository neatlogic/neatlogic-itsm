package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepWorkerAction;
import codedriver.framework.process.constvalue.UserType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessStepHandlerMapper processStepHandlerMapper;
	
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
	public List<String> getProcessTaskStepActionList(Long processTaskId, Long processTaskStepId) {
		List<String> actionList = new ArrayList<>();
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		String processTaskStatus = processTaskVo.getStatus();
		int processTaskStepIsActive = -1;
		String processTaskStepStatus = ProcessTaskStatus.PENDING.getValue();
		List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserId(UserContext.get().getUserId(true));
		List<String> currentUserProcessUserTypeList = new ArrayList<>();
		currentUserProcessUserTypeList.add(UserType.ALL.getValue());
		if(UserContext.get().getUserId(true).equals(processTaskVo.getOwner())) {
			currentUserProcessUserTypeList.add(UserType.OWNER.getValue());
			if(ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
				actionList.add(ProcessTaskStepAction.STARTPROCESS.getValue());
			}
		}
		if(UserContext.get().getUserId(true).equals(processTaskVo.getReporter())) {
			currentUserProcessUserTypeList.add(UserType.REPORTER.getValue());
			if(ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
				actionList.add(ProcessTaskStepAction.STARTPROCESS.getValue());
			}
		}
		
		JSONArray authorityList = null;
		if(processTaskStepId != null) {
			//获取步骤信息
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			if(processTaskStepVo == null) {
				throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
			}
			if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
				throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
			}
			processTaskStepIsActive = processTaskStepVo.getIsActive();
			processTaskStepStatus = processTaskStepVo.getStatus();
			List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.MAJOR.getValue());
			List<String> majorUserIdList = majorUserList.stream().map(ProcessTaskStepUserVo::getUserId).collect(Collectors.toList());
			if(majorUserIdList.contains(UserContext.get().getUserId(true))) {
				currentUserProcessUserTypeList.add(UserType.MAJOR.getValue());
			}
			List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.MINOR.getValue());
			List<String> minorUserIdList = minorUserList.stream().map(ProcessTaskStepUserVo::getUserId).collect(Collectors.toList());
			if(minorUserIdList.contains(UserContext.get().getUserId(true))) {
				currentUserProcessUserTypeList.add(UserType.MINOR.getValue());
			}
			List<ProcessTaskStepUserVo> agentUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.AGENT.getValue());
			List<String> agentUserIdList = agentUserList.stream().map(ProcessTaskStepUserVo::getUserId).collect(Collectors.toList());
			if(agentUserIdList.contains(UserContext.get().getUserId(true))) {
				currentUserProcessUserTypeList.add(UserType.AGENT.getValue());
			}
			String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
			JSONObject stepConfigObj = JSON.parseObject(stepConfig);
			authorityList = stepConfigObj.getJSONArray("authorityList");
			//如果步骤自定义权限设置为空，则用组件的全局权限设置
			if(CollectionUtils.isEmpty(authorityList)) {
				ProcessStepHandlerVo processStepHandlerVo = processStepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
				if(processStepHandlerVo != null) {
					JSONObject handlerConfigObj = JSON.parseObject(processStepHandlerVo.getConfig());
					authorityList = handlerConfigObj.getJSONArray("authorityList");
				}
			}
		}
		
		//目前只有四种权限可以设置：查看节点信息view、终止/恢复流程abort、转交transfer、修改上报内容update
				
		if(CollectionUtils.isNotEmpty(authorityList)) {
			for(int i = 0; i < authorityList.size(); i++) {
				JSONObject authorityObj = authorityList.getJSONObject(i);
				JSONArray acceptList = authorityObj.getJSONArray("acceptList");
				if(CollectionUtils.isNotEmpty(acceptList)) {
					List<String> processUserTypeList = new ArrayList<>();
					List<String> userList = new ArrayList<>();
					List<String> teamList = new ArrayList<>();
					List<String> roleList = new ArrayList<>();
					for(int j = 0; j < acceptList.size(); j++) {
						String accept = acceptList.getString(j);
						String[] split = accept.split("#");
						if(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
							processUserTypeList.add(split[1]);
						}else if(GroupSearch.USER.getValue().equals(split[0])) {
							userList.add(split[1]);
						}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
							teamList.add(split[1]);
						}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
							roleList.add(split[1]);
						}
					}
					if(processUserTypeList.removeAll(currentUserProcessUserTypeList)) {
						actionList.add(authorityObj.getString("action"));
					}else if(userList.contains(UserContext.get().getUserId(true))){
						actionList.add(authorityObj.getString("action"));
					}else if(teamList.removeAll(currentUserTeamList)){
						actionList.add(authorityObj.getString("action"));
					}else if(roleList.removeAll(UserContext.get().getRoleNameList())){
						actionList.add(authorityObj.getString("action"));
					}
				}else {
					actionList.add(authorityObj.getString("action"));
				}
			}
		}else {//不设置，默认都有
			actionList.add(ProcessTaskStepAction.VIEW.getValue());
			actionList.add(ProcessTaskStepAction.ABORT.getValue());
			actionList.add(ProcessTaskStepAction.TRANSFER.getValue());
			actionList.add(ProcessTaskStepAction.UPDATE.getValue());
		}
		//有终止权限就加上恢复权限，再根据工单的状态判断具体留下哪个权限
		if(actionList.contains(ProcessTaskStepAction.ABORT.getValue())) {
			actionList.add(ProcessTaskStepAction.RECOVER.getValue());
		}
		//TODO linbq根据流程设置和步骤状态判断当前用户权限
		Iterator<String> iterator = actionList.iterator();
		while(iterator.hasNext()) {
			String action = iterator.next();
			if(ProcessTaskStepAction.ABORT.getValue().equals(action)) {
				//工单状态为进行中的才能终止
				if(!ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)) {
					iterator.remove();
				}
			}else if(ProcessTaskStepAction.RECOVER.getValue().equals(action)) {
				//工单状态为已终止的才能恢复
				if(!ProcessTaskStatus.ABORTED.getValue().equals(processTaskStatus)) {
					iterator.remove();
				}
			}else if(ProcessTaskStepAction.TRANSFER.getValue().equals(action)) {
				//步骤状态为正在处理的才能转交
				if(processTaskStepIsActive != 1 || !ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepStatus)) {
					iterator.remove();
				}
			}else if(ProcessTaskStepAction.UPDATE.getValue().equals(action)) {
				//步骤状态为正在处理的才能修改上报内容
				if(processTaskStepIsActive != 1 || !ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepStatus)) {
					iterator.remove();
				}
			}
		}
		
		//以上是权限设置中可自定义的四个权限，接下来判断当前用户是否有开始start、完成complete、暂存save、评论comment的权限

		//获取当前用户可处理步骤列表
		List<ProcessTaskStepVo> processableStepList = getProcessableStepList(processTaskId);
		if(CollectionUtils.isNotEmpty(processableStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processableStepList) {
				if(ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {//已激活未处理
					if(processTaskStepId != null) {
						if(processTaskStepId.equals(processTaskStepVo.getId())) {
							actionList.add(ProcessTaskStepAction.START.getValue());
							break;
						}
					}else {
						actionList.add(ProcessTaskStepAction.START.getValue());
					}
				}
				
			}
		}

		if(processTaskStepIsActive == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepStatus)) {
			//完成complete 暂存save 评论comment
			if(currentUserProcessUserTypeList.contains(UserType.MAJOR.getValue()) || currentUserProcessUserTypeList.contains(UserType.AGENT.getValue())) {
				actionList.add(ProcessTaskStepAction.COMPLETE.getValue());
				actionList.add(ProcessTaskStepAction.SAVE.getValue());
				actionList.add(ProcessTaskStepAction.COMMENT.getValue());
				actionList.add(ProcessTaskStepAction.CREATESUBTASK.getValue());
			}
		}
		
		//撤销权限retreat
		Set<ProcessTaskStepVo> retractableStepSet = getRetractableStepListByProcessTaskId(processTaskId);
		if(CollectionUtils.isNotEmpty(retractableStepSet)) {
			actionList.add(ProcessTaskStepAction.RETREAT.getValue());
		}
		return actionList;
	}

	@Override
	public boolean verifyActionAuthoriy(Long processTaskId, Long processTaskStepId, ProcessTaskStepAction action) {
		List<String> actionList = getProcessTaskStepActionList(processTaskId, processTaskStepId);
		return actionList.contains(action.getValue());
	}

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

	private List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(Long processTaskStepId) {
		List<ProcessTaskStepVo> resultList = new ArrayList<>();
		/**所有前置步骤**/
		List<ProcessTaskStepVo> fromStepList = processTaskMapper.getFromProcessTaskStepByToId(processTaskStepId);
		/** 找到所有已完成步骤 **/
		for (ProcessTaskStepVo fromStep : fromStepList) {
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(fromStep.getHandler());
			if(handler != null) {
				if (ProcessStepMode.MT == handler.getMode()) {//手动处理节点
					//获取步骤处理人
					List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(fromStep.getId(), UserType.MAJOR.getValue());
					List<String> majorUserIdList = majorUserList.stream().map(ProcessTaskStepUserVo::getUserId).collect(Collectors.toList());
					if(majorUserIdList.contains(UserContext.get().getUserId())) {
						resultList.add(fromStep);
					}
				}else {//自动处理节点，继续找前置节点
					resultList.addAll(getRetractableStepListByProcessTaskStepId(fromStep.getId()));
				}
			}else {
				throw new ProcessStepHandlerNotFoundException(fromStep.getHandler());
			}
		}
		return resultList;
	}
	
	@Override
	public List<ProcessTaskStepVo> getProcessableStepList(Long processTaskId) {
		List<ProcessTaskStepVo> resultList = new ArrayList<>();
		List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserId(UserContext.get().getUserId(true));
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
		for (ProcessTaskStepVo stepVo : processTaskStepList) {
			/** 找到所有已激活未处理的步骤 **/
			if (stepVo.getIsActive().equals(1)) {
				List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(stepVo.getId());
				for(ProcessTaskStepWorkerVo processTaskStepWorkerVo : processTaskStepWorkerList) {
					if(ProcessTaskStepWorkerAction.UPDATE.getValue().equals(processTaskStepWorkerVo.getAction())) {
						continue;
					}
					if(UserContext.get().getUserId(true).equals(processTaskStepWorkerVo.getUserId())) {
						resultList.add(stepVo);
						break;
					}
					if(currentUserTeamList.contains(processTaskStepWorkerVo.getTeamUuid())) {
						resultList.add(stepVo);
						break;
					}
					if(UserContext.get().getRoleNameList().contains(processTaskStepWorkerVo.getRoleName())) {
						resultList.add(stepVo);
						break;
					}
				}				
			}
		}
		return resultList;
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
			processTaskStepUserVo.setUserType(UserType.MINOR.getValue());
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
				processTaskStepUserVo.setUserType(UserType.MINOR.getValue());
				userList.add(processTaskStepUserVo);
				ProcessTaskStepUserVo oldUserVo = new ProcessTaskStepUserVo();
				oldUserVo.setProcessTaskId(processTaskStepSubtaskVo.getProcessTaskId());
				oldUserVo.setProcessTaskStepId(processTaskStepSubtaskVo.getProcessTaskStepId());
				oldUserVo.setUserId(oldUserId);
				oldUserVo.setUserName(paramObj.getString("oldUserName"));
				oldUserVo.setUserType(UserType.MINOR.getValue());
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
			processTaskStepUserVo.setUserType(UserType.MINOR.getValue());
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
			processTaskStepUserVo.setUserType(UserType.MINOR.getValue());
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
			processTaskStepUserVo.setUserType(UserType.MINOR.getValue());
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
}
