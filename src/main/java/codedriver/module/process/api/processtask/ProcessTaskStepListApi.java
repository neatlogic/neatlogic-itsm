package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Arrays;
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
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.module.process.service.ProcessTaskStepSubtaskService;
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
    
    @Autowired
    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;
	
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
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		ProcessStepUtilHandlerFactory.getHandler().verifyOperationAuthoriy(processTaskVo, ProcessTaskOperationType.POCESSTASKVIEW, true);
		
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
			    IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
			    if(handler == null) {
			        throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
			    }
		        if(handler.verifyOperationAuthoriy(processTaskVo, processTaskStepVo, ProcessTaskOperationType.VIEW, false)){
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
        processTaskService.setProcessTaskStepUser(startProcessTaskStepVo);
        
        //步骤评论列表
        startProcessTaskStepVo.setCommentList(processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(startProcessTaskStepVo.getId(), Arrays.asList(ProcessTaskOperationType.COMMENT.getValue())));
        //子任务列表
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(startProcessTaskStepVo.getId());
        for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
            processTaskStepSubtask.setIsAbortable(0);
            processTaskStepSubtask.setIsCompletable(0);
            processTaskStepSubtask.setIsEditable(0);
            processTaskStepSubtask.setIsRedoable(0);
        }
        startProcessTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);
        
        startProcessTaskStepVo.setComment(processTaskService.getProcessTaskStepContentAndFileByProcessTaskStepId(startProcessTaskStepVo.getId()));
        startProcessTaskStepVo.setIsView(1);
        /** 当前步骤特有步骤信息 **/
        IProcessStepUtilHandler startProcessStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if(startProcessStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }
        startProcessStepUtilHandler.setProcessTaskStepConfig(startProcessTaskStepVo);
        startProcessTaskStepVo.setHandlerStepInfo(startProcessStepUtilHandler.getHandlerStepInitInfo(startProcessTaskStepVo));
        return startProcessTaskStepVo;
	}

	private void getProcessTaskStepDetail(ProcessTaskStepVo processTaskStepVo) {
        //处理人列表
        processTaskService.setProcessTaskStepUser(processTaskStepVo);

        /** 当前步骤特有步骤信息 **/
        IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if(processStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        //获取步骤配置信息
        processStepUtilHandler.setProcessTaskStepConfig(processTaskStepVo);
        processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getHandlerStepInitInfo(processTaskStepVo));
        //步骤评论列表
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.BACK.getValue());
        typeList.add(ProcessTaskOperationType.RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.TRANSFER.getValue());
        processTaskStepVo.setCommentList(processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepVo.getId(), typeList));
        //子任务列表
        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepVo.getId());
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
        //automatic processtaskStepData
        ProcessTaskStepDataVo  stepDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),processTaskStepVo.getHandler(),SystemUser.SYSTEM.getUserId()));
        if(stepDataVo != null) {
            JSONObject stepDataJson = stepDataVo.getData();
            stepDataJson.put("isStepUser", processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid()))>0?1:0);
            processTaskStepVo.setProcessTaskStepData(stepDataJson);
        }
	}
}
