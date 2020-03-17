package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.module.process.constvalue.ProcessTaskAuthorizationObjectType;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.ProcessTaskStepWorkerAction;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

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
	public List<String> getProcessTaskStepActionList(Long processTaskId, Long processTaskStepId) {
		List<String> actionList = new ArrayList<>();
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		String processTaskStatus = processTaskVo.getStatus();
		int processTaskStepIsActive = -1;
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
		}
		//如果步骤自定义权限设置为空，则用组件的全局权限设置
		if(CollectionUtils.isEmpty(authorityList)) {
			//TODO linbq获取组件全局权限设置
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
						String accept = acceptList.getString(i);
						String[] split = accept.split("#");
						if(ProcessTaskAuthorizationObjectType.PROCESSUSERTYPE.getValue().equals(split[0])) {
							processUserTypeList.add(split[1]);
						}else if(ProcessTaskAuthorizationObjectType.USER.getValue().equals(split[0])) {
							userList.add(split[1]);
						}else if(ProcessTaskAuthorizationObjectType.TEAM.getValue().equals(split[0])) {
							teamList.add(split[1]);
						}else if(ProcessTaskAuthorizationObjectType.ROLE.getValue().equals(split[0])) {
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
				//步骤状态为未处理的才能转交
				if(processTaskStepIsActive != 0) {
					iterator.remove();
				}
			}else if(ProcessTaskStepAction.UPDATE.getValue().equals(action)) {
				//步骤状态为正在处理的才能修改上报内容
				if(processTaskStepIsActive != 1) {
					iterator.remove();
				}
			}
		}
		
		//以上是权限设置中可自定义的四个权限，接下来判断当前用户是否有开始start、完成complete、暂存save、评论comment的权限
		if(processTaskStepIsActive == 0) {
			//接受开始start
			List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepId);
			for(ProcessTaskStepWorkerVo processTaskStepWorkerVo : processTaskStepWorkerList) {
				if(ProcessTaskStepWorkerAction.UPDATE.getValue().equals(processTaskStepWorkerVo.getAction())) {
					continue;
				}
				if(UserContext.get().getUserId(true).equals(processTaskStepWorkerVo.getUserId())) {
					actionList.add(ProcessTaskStepAction.START.getValue());
					break;
				}
				if(currentUserTeamList.contains(processTaskStepWorkerVo.getTeamUuid())) {
					actionList.add(ProcessTaskStepAction.START.getValue());
					break;
				}
				if(UserContext.get().getRoleNameList().contains(processTaskStepWorkerVo.getRoleName())) {
					actionList.add(ProcessTaskStepAction.START.getValue());
					break;
				}
			}
			
		}
		//完成complete
		//暂存save
		//评论comment
		if(processTaskStepIsActive == 1) {
			if(currentUserProcessUserTypeList.contains(UserType.MAJOR.getValue()) || currentUserProcessUserTypeList.contains(UserType.AGENT.getValue())) {
				actionList.add(ProcessTaskStepAction.COMPLETE.getValue());
				actionList.add(ProcessTaskStepAction.SAVE.getValue());
				actionList.add(ProcessTaskStepAction.COMMENT.getValue());
			}
		}
		return actionList;
	}

	@Override
	public boolean verifyActionAuthoriy(Long processTaskId, Long processTaskStepId, ProcessTaskStepAction action) {
		List<String> actionList = getProcessTaskStepActionList(processTaskId, processTaskStepId);
		return actionList.contains(action.getValue());
	}
	
}
