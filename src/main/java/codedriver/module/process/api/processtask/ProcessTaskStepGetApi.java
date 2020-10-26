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
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.ProcessTaskScoreTemplateVo;
import codedriver.framework.process.dto.ProcessTaskStepAgentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepRemindVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.module.process.service.ProcessTaskStepSubtaskService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepGetApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
    
    @Autowired
    private ProcessTaskStepDataMapper processTaskStepDataMapper;
    
    @Autowired
    private FileMapper fileMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;
	
    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;
    
    @Autowired
    private ScoreTemplateMapper scoreTemplateMapper;
    
    @Autowired
    private UserMapper userMapper;
    
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
        IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
        handler.verifyOperationAuthoriy(processTaskId, ProcessTaskOperationType.POCESSTASKVIEW, true);
		
		ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
		
        if(ProcessTaskStatus.SUCCEED.getValue().equals(processTaskVo.getStatus())) {
            ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskId);
            if(processTaskScoreTemplateVo != null) {
               if(StringUtils.isNotBlank(processTaskScoreTemplateVo.getConfigHash())) {
                   String configStr = selectContentByHashMapper.getProcessTaskScoreTempleteConfigStringIsByHash(processTaskScoreTemplateVo.getConfigHash());
                   if(StringUtils.isNotBlank(configStr)) {
                       processTaskScoreTemplateVo.setConfig(configStr);
                       List<String> stepUuidList = JSON.parseArray(JSON.toJSONString(processTaskScoreTemplateVo.getConfig().getJSONArray("stepUuidList")), String.class);
                       if(CollectionUtils.isNotEmpty(stepUuidList)) {
                           processTaskVo.setRedoStepList(processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskIdAndProcessStepUuidList(processTaskId, stepUuidList));
                       }
                   }
               }
               processTaskVo.setScoreTemplateVo(scoreTemplateMapper.getScoreTemplateById(processTaskScoreTemplateVo.getScoreTemplateId()));
            }
        }
        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));
        Map<String, String> formAttributeActionMap = new HashMap<>();
		if(processTaskStepId != null) {		    
            ProcessTaskStepVo currentProcessTaskStepVo = getCurrentProcessTaskStepById(processTaskStepId);
			if(currentProcessTaskStepVo != null){
			    handler = ProcessStepUtilHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
				processTaskVo.setCurrentProcessTaskStep(currentProcessTaskStepVo);
				if(MapUtils.isNotEmpty(currentProcessTaskStepVo.getFormAttributeDataMap())) {
				    processTaskVo.setFormAttributeDataMap(currentProcessTaskStepVo.getFormAttributeDataMap());
				}
				if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
	                //表单属性显示控制
	                List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
	                for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
	                    processTaskStepFormAttributeVo.setProcessStepUuid(currentProcessTaskStepVo.getProcessStepUuid());
	                    formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
	                }
	                currentProcessTaskStepVo.setStepFormConfig(processTaskStepFormAttributeList);
				}
			}			
		}
		if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
	        boolean isAuthority = handler.verifyOperationAuthoriy(processTaskId, processTaskStepId, ProcessTaskOperationType.WORK, false);
	        processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, isAuthority ? 1 : 0);
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
	
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取当前步骤信息 
    * @param processTaskStepId 步骤id
    * @return ProcessTaskStepVo
     */
	private ProcessTaskStepVo getCurrentProcessTaskStepById(Long processTaskStepId) {
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        Long processTaskId = processTaskStepVo.getProcessTaskId();
        IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if(handler.verifyOperationAuthoriy(processTaskId, processTaskStepId, ProcessTaskOperationType.VIEW, false)){
            //处理人列表
            processTaskService.setProcessTaskStepUser(processTaskStepVo);

            /** 当前步骤特有步骤信息 **/
            IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if(processStepUtilHandler == null) {
                throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            //获取步骤信息
            processStepUtilHandler.setProcessTaskStepConfig(processTaskStepVo);
            processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getHandlerStepInitInfo(processTaskStepVo));
            if(handler.verifyOperationAuthoriy(processTaskId, processTaskStepId, ProcessTaskOperationType.SAVE, false)){
                //回复框内容和附件暂存回显              
                setTemporaryData(processTaskStepVo);
            }
            //步骤评论列表
            List<String> typeList = new ArrayList<>();
            typeList.add(ProcessTaskOperationType.COMMENT.getValue());
            typeList.add(ProcessTaskOperationType.COMPLETE.getValue());
            typeList.add(ProcessTaskOperationType.BACK.getValue());
            typeList.add(ProcessTaskOperationType.RETREAT.getValue());
            typeList.add(ProcessTaskOperationType.TRANSFER.getValue());
            processTaskStepVo.setCommentList(processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepId, typeList));
            
            //获取当前用户有权限的所有子任务
            //子任务列表
            if(processTaskStepVo.getIsActive().intValue() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
                if(CollectionUtils.isNotEmpty(processTaskStepSubtaskList)) {
                    Map<String, String> customButtonMap = handler.getCustomButtonMapByProcessTaskStepId(processTaskStepVo.getId());
                    for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
                        String currentUser = UserContext.get().getUserUuid(true);
                        if((currentUser.equals(processTaskStepSubtask.getMajorUser()) && !ProcessTaskStatus.ABORTED.getValue().equals(processTaskStepSubtask.getStatus()))
                            || (currentUser.equals(processTaskStepSubtask.getUserUuid()) && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus()))) {
                            if(processTaskStepSubtask.getIsAbortable() == 1) {
                                String value = ProcessTaskOperationType.ABORTSUBTASK.getValue();
                                String text = customButtonMap.get(value);
                                if(StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.ABORTSUBTASK.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if(processTaskStepSubtask.getIsCommentable() == 1) {
                                String value = ProcessTaskOperationType.COMMENTSUBTASK.getValue();
                                String text = customButtonMap.get(value);
                                if(StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.COMMENTSUBTASK.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if(processTaskStepSubtask.getIsCompletable() == 1) {
                                String value = ProcessTaskOperationType.COMPLETESUBTASK.getValue();
                                String text = customButtonMap.get(value);
                                if(StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.COMPLETESUBTASK.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if(processTaskStepSubtask.getIsEditable() == 1) {
                                String value = ProcessTaskOperationType.EDITSUBTASK.getValue();
                                String text = customButtonMap.get(value);
                                if(StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.EDITSUBTASK.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            if(processTaskStepSubtask.getIsRedoable() == 1) {
                                String value = ProcessTaskOperationType.REDOSUBTASK.getValue();
                                String text = customButtonMap.get(value);
                                if(StringUtils.isBlank(text)) {
                                    text = ProcessTaskOperationType.REDOSUBTASK.getText();
                                }
                                processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
                            }
                            processTaskStepVo.getProcessTaskStepSubtaskList().add(processTaskStepSubtask);
                        }
                    }
                }
            }
            
            //获取可分配处理人的步骤列表             
            processTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepList(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getProcessStepUuid()));
            
            //时效列表
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
            processTaskStepVo.setSlaTimeList(processTaskService.getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(processTaskStepId, processTaskVo.getWorktimeUuid()));
            
            //processtaskStepData
            ProcessTaskStepDataVo  stepDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),processTaskStepVo.getHandler()));
            if(stepDataVo != null) {
                JSONObject stepDataJson = stepDataVo.getData();
                processTaskStepVo.setProcessTaskStepData(stepDataJson);
                if(handler.verifyOperationAuthoriy(processTaskId, processTaskStepId, ProcessTaskOperationType.WORK, false)) {//有处理权限
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
            processTaskStepVo.setForwardNextStepList(processTaskService.getForwardNextStepListByProcessTaskStepId(processTaskStepVo.getId()));
            processTaskStepVo.setBackwardNextStepList(processTaskService.getBackwardNextStepListByProcessTaskStepId(processTaskStepVo.getId()));;
            /** 提醒列表 **/
            List<ProcessTaskStepRemindVo> processTaskStepRemindList = processTaskService.getProcessTaskStepRemindListByProcessTaskStepId(processTaskStepId);
            processTaskStepVo.setProcessTaskStepRemindList(processTaskStepRemindList);
            
            ProcessTaskStepAgentVo processTaskStepAgentVo = processTaskMapper.getProcessTaskStepAgentByProcessTaskStepId(processTaskStepId);
            if(processTaskStepAgentVo != null) {
                processTaskStepVo.setOriginalUser(processTaskStepAgentVo.getUserUuid());
                UserVo userVo = userMapper.getUserByUuid(processTaskStepAgentVo.getUserUuid());
                if(userVo != null) {
                    processTaskStepVo.setOriginalUserName(userVo.getUserName());
                }
            }
            return processTaskStepVo;
        }
        return null;
    }
	/**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 设置步骤当前用户的暂存数据
    * @param ProcessTaskStepVo 步骤信息
    * @return void
     */
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
                ProcessTaskStepReplyVo commentVo = new ProcessTaskStepReplyVo();
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
