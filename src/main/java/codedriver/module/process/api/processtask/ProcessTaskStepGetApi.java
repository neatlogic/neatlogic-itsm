package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepGetApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
    
    @Autowired
    ProcessTaskStepDataMapper processTaskStepDataMapper;
    
    @Autowired
    private FileMapper fileMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/get";
	}

	@Override
	public String getName() {
		return "工单步骤基本信息获取接口";
	}
	
	
	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")
	})
	@Output({
		@Param(name = "processTask", explode = ProcessTaskVo.class, desc = "工单信息")
	})
	@Description(desc = "工单步骤基本信息获取接口，当前步骤名称、激活时间、状态、处理人、协助处理人、处理时效、表单属性显示控制等")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        
		ProcessStepUtilHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		
		ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
        
        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));

        Map<String, String> formAttributeActionMap = new HashMap<>();
		if(processTaskStepId != null) {
			List<String> verifyActionList = new ArrayList<>();
			verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
			List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
			if(actionList.contains(ProcessTaskStepAction.VIEW.getValue())){	
			    ProcessTaskStepVo currentProcessTaskStepVo = getCurrentProcessTaskStepById(processTaskStepId);
				processTaskVo.setCurrentProcessTaskStep(currentProcessTaskStepVo);
				if(MapUtils.isNotEmpty(currentProcessTaskStepVo.getFormAttributeDataMap())) {
				    processTaskVo.setFormAttributeDataMap(currentProcessTaskStepVo.getFormAttributeDataMap());
				}
				if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
	                //表单属性显示控制
	                List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
	                for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
	                    formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
	                }
				}
			}			
		}
		if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
		    List<String> verifyActionList = new ArrayList<>();
	        verifyActionList.add(ProcessTaskStepAction.WORK.getValue());
	        List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
	        processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, actionList.removeAll(verifyActionList) ? 1 : 0);
		}
		
		//TODO 兼容老工单表单（判断是否存在旧表单）
        Map<String,String> oldFormPropMap = processTaskMapper.getProcessTaskOldFormAndPropByTaskId(processTaskId);
        if(oldFormPropMap != null&&oldFormPropMap.size()>0) {
            processTaskVo.setIsHasOldFormProp(1);
        }
		
		JSONObject resultObj = new JSONObject();
        resultObj.put("processTask", processTaskVo);
		return resultObj;
	}
	
	private ProcessTaskStepVo getCurrentProcessTaskStepById(Long processTaskStepId) {
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        Long processTaskId = processTaskStepVo.getProcessTaskId();
        //获取步骤信息
        processTaskService.setProcessTaskStepConfig(processTaskStepVo);
        //处理人列表
        processTaskService.setProcessTaskStepUser(processTaskStepVo);

        /** 当前步骤特有步骤信息 **/
        IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if(processStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getHandlerStepInitInfo(processTaskStepId));
        //回复框内容和附件暂存回显              
        setTemporaryData(processTaskStepVo);
        
        //步骤评论列表        
        processTaskStepVo.setCommentList(processTaskService.getProcessTaskStepCommentListByProcessTaskStepId(processTaskStepId));
        
        //获取当前用户有权限的所有子任务
        //子任务列表
        if(processTaskStepVo.getIsActive().intValue() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {          
            processTaskStepVo.setProcessTaskStepSubtaskList(processTaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId));
        }
        
        //获取可分配处理人的步骤列表             
        processTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepListByProcessTaskIdAndProcessStepUuid(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getProcessStepUuid()));
        
        //时效列表
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        processTaskStepVo.setSlaTimeList(processTaskService.getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(processTaskStepId, processTaskVo.getWorktimeUuid()));
        
      //processtaskStepData
        ProcessTaskStepDataVo  stepDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),processTaskStepVo.getHandler()));
        if(stepDataVo != null) {
            JSONObject stepDataJson = stepDataVo.getData();
            processTaskStepVo.setProcessTaskStepData(stepDataJson);
            List<String> verifyActionList = new ArrayList<>();
            verifyActionList.add(ProcessTaskStepAction.WORK.getValue());
            List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
            if(actionList.removeAll(verifyActionList)) {//有处理权限
                stepDataJson.put("isStepUser", 1);
                if(processTaskStepVo.getHandler().equals(ProcessStepHandler.AUTOMATIC.getHandler())){
                    JSONObject requestAuditJson = stepDataJson.getJSONObject("requestAudit");
                    if(requestAuditJson.containsKey("status")
                            &&requestAuditJson.getJSONObject("status").getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                        requestAuditJson.put("isRetry", 1);
                    }else {
                        requestAuditJson.put("isRetry", 0);
                    }
                    JSONObject callbackAuditJson = stepDataJson.getJSONObject("callbackAudit");
                    if(callbackAuditJson!=null) {
                            if(callbackAuditJson.containsKey("status")
                            &&callbackAuditJson.getJSONObject("status").getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                                    callbackAuditJson.put("isRetry", 1);
                            }else {
                                callbackAuditJson.put("isRetry", 0);
                            }
                    }
                }
            }
        }
        /** 下一步骤列表 **/
        processTaskService.setNextStepList(processTaskStepVo);
        return processTaskStepVo;
    }
	
	private void setTemporaryData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepVo.getId());
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if(stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if(MapUtils.isNotEmpty(dataObj)) {
                JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                if(CollectionUtils.isNotEmpty(formAttributeDataList)) {
                    Map<String, Object> formAttributeDataMap = new HashMap<>();
                    for(int i = 0; i < formAttributeDataList.size(); i++) {
                        JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                        formAttributeDataMap.put(formAttributeDataObj.getString("attributeUuid"), formAttributeDataObj.get("dataList"));
                    }
                    processTaskStepVo.setFormAttributeDataMap(formAttributeDataMap);
                }
                ProcessTaskStepCommentVo commentVo = new ProcessTaskStepCommentVo();
                String content = dataObj.getString("content");
                commentVo.setContent(content);
                List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(dataObj.getJSONArray("fileIdList")), Long.class);
                if(CollectionUtils.isNotEmpty(fileIdList)) {
                    commentVo.setFileList(fileMapper.getFileListByIdList(fileIdList));
                }
                processTaskStepVo.setComment(commentVo);
                /** 当前步骤特有步骤信息 **/
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if(handlerStepInfo != null) {
                    processTaskStepVo.setHandlerStepInfo(handlerStepInfo);
                }
            }
        }
    }
}
