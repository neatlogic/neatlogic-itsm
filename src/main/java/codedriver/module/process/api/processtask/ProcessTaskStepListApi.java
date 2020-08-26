package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepListApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
    
    @Autowired
	ProcessTaskStepDataMapper processTaskStepDataMapper;
	
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
		ProcessStepUtilHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		ProcessTaskStepVo startProcessTaskStepVo = getStartProcessTaskStepByProcessTaskId(processTaskId);
				
		Map<Long, ProcessTaskStepVo> processTaskStepMap = new HashMap<>();
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				if(processTaskStepVo.getStartTime() != null) {
					processTaskStepMap.put(processTaskStepVo.getId(), processTaskStepVo);
				}
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
					toStepList.sort((step1, step2) -> step1.getStartTime().compareTo(step2.getStartTime()));
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
		        List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), verifyActionList);
		        if(actionList.contains(ProcessTaskStepAction.VIEW.getValue())){
		            processTaskStepVo.setIsView(1);
		            getProcessTaskStepDetail(processTaskStepVo);
		        }else {
		            processTaskStepVo.setIsView(0);
		        }
			}
		}
		resultList.add(0, startProcessTaskStepVo);
		return resultList;
	}

	private ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId) {
	  //开始步骤
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
        if(processTaskStepList.size() != 1) {
            throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
        }

        ProcessTaskStepVo startProcessTaskStepVo = processTaskStepList.get(0);
        processTaskService.setProcessTaskStepConfig(startProcessTaskStepVo);
        processTaskService.setProcessTaskStepUser(startProcessTaskStepVo);
        
        //步骤评论列表
        startProcessTaskStepVo.setCommentList(processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(startProcessTaskStepVo.getId()));
        //子任务列表
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(startProcessTaskStepVo.getId());
        for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
            processTaskStepSubtask.setIsAbortable(0);
            processTaskStepSubtask.setIsCompletable(0);
            processTaskStepSubtask.setIsEditable(0);
            processTaskStepSubtask.setIsRedoable(0);
        }
        startProcessTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);
        
        startProcessTaskStepVo.setComment(processTaskService.getProcessTaskStepContentAndFileByProcessTaskStepIdId(startProcessTaskStepVo.getId()));
        startProcessTaskStepVo.setIsView(1);
        /** 当前步骤特有步骤信息 **/
        IProcessStepUtilHandler startProcessStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if(startProcessStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }
        startProcessTaskStepVo.setHandlerStepInfo(startProcessStepUtilHandler.getHandlerStepInitInfo(startProcessTaskStepVo));
        return startProcessTaskStepVo;
	}

	private void getProcessTaskStepDetail(ProcessTaskStepVo processTaskStepVo) {
	    //获取步骤配置信息
        processTaskService.setProcessTaskStepConfig(processTaskStepVo);
        //处理人列表
        processTaskService.setProcessTaskStepUser(processTaskStepVo);

        /** 当前步骤特有步骤信息 **/
        IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if(processStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getHandlerStepInitInfo(processTaskStepVo));
        //步骤评论列表
        processTaskStepVo.setCommentList(processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepVo.getId()));
        //子任务列表
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepVo.getId());
        for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
            processTaskStepSubtask.setIsAbortable(0);
            processTaskStepSubtask.setIsCompletable(0);
            processTaskStepSubtask.setIsEditable(0);
            processTaskStepSubtask.setIsRedoable(0);
        }
        processTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);
        //时效列表
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskStepVo.getProcessTaskId());
        processTaskStepVo.setSlaTimeList(processTaskService.getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(processTaskStepVo.getId(), processTaskVo.getWorktimeUuid()));
        //processtaskStepData
        ProcessTaskStepDataVo  stepDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),processTaskStepVo.getHandler()));
        if(stepDataVo != null) {
            JSONObject stepDataJson = stepDataVo.getData();
            stepDataJson.put("isStepUser", processTaskMapper.checkIsProcessTaskStepUser(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid())>0?1:0);
            processTaskStepVo.setProcessTaskStepData(stepDataJson);
        }
	}
}
