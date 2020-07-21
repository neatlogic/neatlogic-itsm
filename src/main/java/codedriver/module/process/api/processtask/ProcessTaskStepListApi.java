package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;
    
    @Autowired
	ProcessTaskStepDataMapper processTaskStepDataMapper;
	
	@Autowired
	private FileMapper fileMapper;
	
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
		Map<String, ProcessStepHandlerVo> handlerConfigMap = new HashMap<>();
        List<ProcessStepHandlerVo> handlerConfigList = stepHandlerMapper.getProcessStepHandlerConfig();
        for(ProcessStepHandlerVo handlerConfig : handlerConfigList) {
        	handlerConfigMap.put(handlerConfig.getHandler(), handlerConfig);
        }
		ProcessTaskStepVo startProcessTaskStepVo = processTaskStepList.get(0);
		String startStepConfig = processTaskMapper.getProcessTaskStepConfigByHash(startProcessTaskStepVo.getConfigHash());
		startProcessTaskStepVo.setConfig(startStepConfig);
		ProcessStepHandlerVo processStepHandlerConfig = handlerConfigMap.get(startProcessTaskStepVo.getHandler());
		if(processStepHandlerConfig != null) {
			startProcessTaskStepVo.setGlobalConfig(processStepHandlerConfig.getConfig());					
		}
		List<ProcessTaskStepUserVo> startStepMajorUserList = processTaskMapper.getProcessTaskStepUserByStepId(startProcessTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
		if(CollectionUtils.isNotEmpty(startStepMajorUserList)) {
			startProcessTaskStepVo.setMajorUser(startStepMajorUserList.get(0));
		}
		startProcessTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(startProcessTaskStepVo.getId(), ProcessUserType.MINOR.getValue()));
		startProcessTaskStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(startProcessTaskStepVo.getId(), ProcessUserType.AGENT.getValue()));
		startProcessTaskStepVo.setWorkerList(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(startProcessTaskStepVo.getId()));
		
		//步骤评论列表
		List<ProcessTaskStepCommentVo> processTaskStepCommentList = processTaskMapper.getProcessTaskStepCommentListByProcessTaskStepId(startProcessTaskStepVo.getId());
		for(ProcessTaskStepCommentVo processTaskStepComment : processTaskStepCommentList) {
			processTaskService.parseProcessTaskStepComment(processTaskStepComment);
		}
		startProcessTaskStepVo.setCommentList(processTaskStepCommentList);
		//子任务列表
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
		processTaskStepSubtaskVo.setProcessTaskId(startProcessTaskStepVo.getProcessTaskId());
		processTaskStepSubtaskVo.setProcessTaskStepId(startProcessTaskStepVo.getId());
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
		startProcessTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);

		ProcessTaskStepCommentVo comment = new ProcessTaskStepCommentVo();
		//获取上报描述内容
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepVo.getId());
		if(!processTaskStepContentList.isEmpty()) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo != null) {
				comment.setContent(processTaskContentVo.getContent());
			}
		}
		//附件
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(processTaskId);
		processTaskFileVo.setProcessTaskStepId(startProcessTaskStepVo.getId());
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		
		if(processTaskFileList.size() > 0) {
			List<FileVo> fileList = new ArrayList<>();
			for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
				FileVo fileVo = fileMapper.getFileById(processTaskFile.getFileId());
				fileList.add(fileVo);
			}
			comment.setFileList(fileList);
		}
		startProcessTaskStepVo.setComment(comment);
		startProcessTaskStepVo.setIsView(1);

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
		fromStepIdList.add(startProcessTaskStepVo.getId());
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
					processTaskStepCommentList = processTaskMapper.getProcessTaskStepCommentListByProcessTaskStepId(processTaskStepVo.getId());
					for(ProcessTaskStepCommentVo processTaskStepComment : processTaskStepCommentList) {
						processTaskService.parseProcessTaskStepComment(processTaskStepComment);
					}
					processTaskStepVo.setCommentList(processTaskStepCommentList);
					//子任务列表
					processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
					processTaskStepSubtaskVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
					processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepVo.getId());
					processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(processTaskStepSubtaskVo);
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
				processStepHandlerConfig = handlerConfigMap.get(processTaskStepVo.getHandler());
				if(processStepHandlerConfig != null) {
					processTaskStepVo.setGlobalConfig(processStepHandlerConfig.getConfig());					
				}
				//processtaskStepData
				ProcessTaskStepDataVo  stepDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),processTaskStepVo.getHandler()));
				if(stepDataVo != null) {
					JSONObject stepDataJson = stepDataVo.getData();
					stepDataJson.put("isStepUser", processTaskMapper.checkIsProcessTaskStepUser(processTaskId, processTaskStepVo.getId(), UserContext.get().getUserUuid())>0?1:0);
					processTaskStepVo.setProcessTaskStepData(stepDataJson);
				}
			}
		}
		resultList.add(0, startProcessTaskStepVo);
		return resultList;
	}

}
