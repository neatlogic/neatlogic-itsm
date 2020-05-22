package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskStepListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/step/list";
	}

	@Override
	public String getName() {
		return "工单步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")
	})
	@Description(desc = "工单步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessStepHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		
		
		//开始步骤
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		ProcessTaskStepVo startStepVo = processTaskStepList.get(0);
		List<ProcessTaskStepUserVo> startStepMajorUserList = processTaskMapper.getProcessTaskStepUserByStepId(startStepVo.getId(), ProcessUserType.MAJOR.getValue());
		if(CollectionUtils.isNotEmpty(startStepMajorUserList)) {
			startStepVo.setMajorUser(startStepMajorUserList.get(0));
		}
		startStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(startStepVo.getId(), ProcessUserType.AGENT.getValue()));
		//上报描述内容和附件
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setProcessTaskId(processTaskId);
		processTaskStepAuditVo.setProcessTaskStepId(startStepVo.getId());
		processTaskStepAuditVo.setAction(ProcessTaskStepAction.STARTPROCESS.getValue());
		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
		if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
			ProcessTaskStepCommentVo comment = new ProcessTaskStepCommentVo(processTaskStepAuditList.get(0));
			startStepVo.setComment(comment);
		}
		startStepVo.setIsView(1);

		Map<Long, ProcessTaskStepVo> processTaskStepMap = new HashMap<>();
		processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				if(Objects.equals(processTaskStepVo.getIsActive(), 0) && ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {
					continue;
				}
				processTaskStepMap.put(processTaskStepVo.getId(), processTaskStepVo);
			}
		}
		
		Map<Long, List<Long>> fromStepIdMap = new HashMap<>();
		List<ProcessTaskStepRelVo> prcessTaskStepRelList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
		for(ProcessTaskStepRelVo processTaskStepRelVo : prcessTaskStepRelList) {
			if(ProcessFlowDirection.FORWARD.getValue().equals(processTaskStepRelVo.getType())) {
				Long fromStepId = processTaskStepRelVo.getFromProcessTaskStepId();
				Long toStepId = processTaskStepRelVo.getToProcessTaskStepId();
					List<Long> toStepIdList = fromStepIdMap.get(fromStepId);
					if(toStepIdList == null) {
						toStepIdList = new ArrayList<>();
						fromStepIdMap.put(fromStepId, toStepIdList);
					}
					toStepIdList.add(toStepId);
			}
			
		}
		List<ProcessTaskStepVo> resultList = new ArrayList<>();
		Set<Long> fromStepIdList = new HashSet<>();
		fromStepIdList.add(startStepVo.getId());
		while(!processTaskStepMap.isEmpty()) {
			Set<Long> newFromStepIdList = new HashSet<>();
			for(Long fromStepId : fromStepIdList) {
				List<Long> toStepIdList = fromStepIdMap.get(fromStepId);
				List<ProcessTaskStepVo> toStepList = new ArrayList<>(toStepIdList.size());
				for(Long toStepId : toStepIdList) {
					ProcessTaskStepVo toStep = processTaskStepMap.remove(toStepId);
					if(toStep != null) {
						toStepList.add(toStep);
					}
					if(fromStepIdMap.containsKey(toStepId)) {
						newFromStepIdList.add(toStepId);
					}
				}
				if(toStepList.size() > 1) {
					//按开始时间正序排序
					toStepList.sort((step1, step2) -> (int)(step1.getStartTime().getTime() - step2.getStartTime().getTime()));
				}
				resultList.addAll(toStepList);
			}
			fromStepIdList.clear(); 
			fromStepIdList.addAll(newFromStepIdList);
		}
		
		//其他处理步骤
		if(CollectionUtils.isNotEmpty(resultList)) {
			for(ProcessTaskStepVo processTaskStepVo : resultList) {
				//判断当前用户是否有权限查看该节点信息
				List<String> verifyActionList = new ArrayList<>();
				verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
				List<String> actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), verifyActionList);
				if(actionList.contains(ProcessTaskStepAction.VIEW.getValue())){
					processTaskStepVo.setIsView(1);
					List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
					if(CollectionUtils.isNotEmpty(majorUserList)) {
						processTaskStepVo.setMajorUser(majorUserList.get(0));
					}
					processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MINOR.getValue()));
					processTaskStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.AGENT.getValue()));
					processTaskStepVo.setWorkerList(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
					//步骤评论列表
					List<ProcessTaskStepCommentVo> processTaskStepCommentList = processTaskMapper.getProcessTaskStepCommentListByProcessTaskStepId(processTaskStepVo.getId());
					for(ProcessTaskStepCommentVo processTaskStepComment : processTaskStepCommentList) {
						processTaskService.parseProcessTaskStepComment(processTaskStepComment);
					}
					//子任务列表
					ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
					processTaskStepSubtaskVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
					processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepVo.getId());
					List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(processTaskStepSubtaskVo);
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
						processTaskStepSubtask.setIsAbortable(0);
						processTaskStepSubtask.setIsCompletable(0);
						processTaskStepSubtask.setIsEditable(0);
						processTaskStepSubtask.setIsRedoable(0);
						
					}
					processTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);
				}else {
					processTaskStepVo.setIsView(0);
				}
				String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
				processTaskStepVo.setConfig(stepConfig);
			}
		}
		resultList.add(0, startStepVo);
		return resultList;
	}

}
