package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskConfigVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskSlaVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskStepGetApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;
	
	@Autowired
	private ProcessTaskStepDataMapper processTaskStepDataMapper;
	
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
		ProcessStepHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		//获取工单基本信息(title、channel_uuid、config_hash、priority_uuid、status、start_time、end_time、expire_time、owner、ownerName、reporter、reporterName)
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}

		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		ProcessTaskStepVo processTaskStepVo = null;
		if(processTaskStepId != null) {
			processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			if(processTaskStepVo == null) {
				throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
			}
			if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
				throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
			}
		}
		
		//获取工单流程图信息
		ProcessTaskConfigVo processTaskConfig = processTaskMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash());
		if(processTaskConfig == null) {
			throw new ProcessTaskRuntimeException("没有找到工单：'" + processTaskId + "'的流程图配置信息");
		}
		processTaskVo.setConfig(processTaskConfig.getConfig());
		//获取开始步骤id
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}

		ProcessTaskStepVo startProcessTaskStepVo = processTaskStepList.get(0);
		String startStepConfig = processTaskMapper.getProcessTaskStepConfigByHash(startProcessTaskStepVo.getConfigHash());
		startProcessTaskStepVo.setConfig(startStepConfig);
		ProcessStepHandlerVo processStepHandlerConfig = stepHandlerMapper.getProcessStepHandlerByHandler(startProcessTaskStepVo.getHandler());
		if(processStepHandlerConfig != null) {
			startProcessTaskStepVo.setGlobalConfig(processStepHandlerConfig.getConfig());					
		}
		Long startProcessTaskStepId = startProcessTaskStepVo.getId();
		ProcessTaskStepCommentVo comment = new ProcessTaskStepCommentVo();
		//获取上报描述内容
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepId);
		if(!processTaskStepContentList.isEmpty()) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo != null) {
				comment.setContent(processTaskContentVo.getContent());
			}
		}
		//附件
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(processTaskId);
		processTaskFileVo.setProcessTaskStepId(startProcessTaskStepId);
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		
		if(processTaskFileList.size() > 0) {
			List<String> fileUuidList = new ArrayList<>();
			List<FileVo> fileList = new ArrayList<>();
			for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
				fileUuidList.add(processTaskFile.getFileUuid());
				FileVo fileVo = fileMapper.getFileByUuid(processTaskFile.getFileUuid());
				fileList.add(fileVo);
			}
			comment.setFileList(fileList);
		}
		startProcessTaskStepVo.setComment(comment);
		processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);
		
		//优先级
		PriorityVo priorityVo = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
		processTaskVo.setPriority(priorityVo);
		//上报服务路径
		ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
		if(channelVo != null) {
			StringBuilder channelPath = new StringBuilder(channelVo.getName());
			String parentUuid = channelVo.getParentUuid();
			while(!CatalogVo.ROOT_UUID.equals(parentUuid)) {
				CatalogVo catalogVo = catalogMapper.getCatalogByUuid(parentUuid);
				if(catalogVo != null) {
					channelPath.insert(0, "/");
					channelPath.insert(0, catalogVo.getName());
					parentUuid = catalogVo.getParentUuid();
				}else {
					break;
				}
			}
			processTaskVo.setChannelPath(channelPath.toString());
			processTaskVo.setChannelType(channelMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid()));
		}
		//耗时
		if(processTaskVo.getEndTime() != null) {
			long timeCost = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(), processTaskVo.getStartTime().getTime(), processTaskVo.getEndTime().getTime());
			processTaskVo.setTimeCost(timeCost);
		}
		
		//获取工单表单信息
		ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
		if(processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContent())) {
			processTaskVo.setFormConfig(processTaskFormVo.getFormContent());			
			List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
			if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
				Map<String, Object> formAttributeDataMap = new HashMap<>();
				for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
					formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
				}
				processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
			}
		}
		
		JSONObject resultObj = new JSONObject();
		resultObj.put("processTask", processTaskVo);
		
		if(processTaskStepId != null) {
			List<String> verifyActionList = new ArrayList<>();
			verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
			List<String> actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
			if(actionList.contains(ProcessTaskStepAction.VIEW.getValue())){
				//获取步骤信息
				String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
				processTaskStepVo.setConfig(stepConfig);
				processStepHandlerConfig = stepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
				if(processStepHandlerConfig != null) {
					processTaskStepVo.setGlobalConfig(processStepHandlerConfig.getConfig());					
				}
				//处理人列表
				List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
				if(CollectionUtils.isNotEmpty(majorUserList)) {
					processTaskStepVo.setMajorUser(majorUserList.get(0));
				}
				List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
				if(CollectionUtils.isNotEmpty(minorUserList)) {
					processTaskStepVo.setMinorUserList(minorUserList);
				}
				List<ProcessTaskStepUserVo> agentUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.AGENT.getValue());
				if(CollectionUtils.isNotEmpty(agentUserList)) {
					processTaskStepVo.setAgentUserList(agentUserList);
				}
				
				//回复框内容和附件暂存回显
//				ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
//				processTaskStepAuditVo.setProcessTaskId(processTaskId);
//				processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
//				processTaskStepAuditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
//				processTaskStepAuditVo.setUserUuid(UserContext.get().getUserUuid(true));
//				List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
//				if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
//					ProcessTaskStepAuditVo processTaskStepAudit = processTaskStepAuditList.get(processTaskStepAuditList.size() - 1);
//					for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAudit.getAuditDetailList()) {
//						if(ProcessTaskAuditDetailType.FORM.getValue().equals(processTaskStepAuditDetailVo.getType())) {
//							if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
//								ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepAuditDetailVo.getNewContent());
//								if(processTaskContentVo != null) {
//									List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = JSON.parseArray(processTaskContentVo.getContent(), ProcessTaskFormAttributeDataVo.class);
//									if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
//										Map<String, Object> formAttributeDataMap = new HashMap<>();
//										for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
//											formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
//										}
//										processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
//									}
//								}
//							}
//						}
//					}
//					processTaskStepVo.setComment(new ProcessTaskStepCommentVo(processTaskStepAudit));
//				}
				
				ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
				processTaskStepDataVo.setProcessTaskId(processTaskId);
				processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
				processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
				processTaskStepDataVo.setType("stepDraftSave");
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
							processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
						}
						ProcessTaskStepCommentVo commentVo = new ProcessTaskStepCommentVo();
						String content = dataObj.getString("content");
						commentVo.setContent(content);
						List<String> fileUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileUuidList")), String.class);
						if(CollectionUtils.isNotEmpty(fileUuidList)) {
							List<FileVo> fileList = new ArrayList<>();
							for(String fileUuid : fileUuidList) {
								FileVo fileVo = fileMapper.getFileByUuid(fileUuid);
								if(fileVo != null) {
									fileList.add(fileVo);
								}
							}
							commentVo.setFileList(fileList);
						}
						processTaskStepVo.setComment(commentVo);
					}
				}
				
				//步骤评论列表
				List<ProcessTaskStepCommentVo> processTaskStepCommentList = processTaskMapper.getProcessTaskStepCommentListByProcessTaskStepId(processTaskStepId);
				for(ProcessTaskStepCommentVo processTaskStepComment : processTaskStepCommentList) {
					processTaskService.parseProcessTaskStepComment(processTaskStepComment);
				}
				processTaskStepVo.setCommentList(processTaskStepCommentList);
				//获取当前用户有权限的所有子任务
				//子任务列表
				if(processTaskStepVo.getIsActive().intValue() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
					List<ProcessTaskStepSubtaskVo> subtaskList = new ArrayList<>();
					ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
					processTaskStepSubtaskVo.setProcessTaskId(processTaskId);
					processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepId);
					List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(processTaskStepSubtaskVo);
					for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
						String currentUser = UserContext.get().getUserUuid(true);
						if((currentUser.equals(processTaskStepSubtask.getOwner()) && !ProcessTaskStatus.ABORTED.getValue().equals(processTaskStepSubtask.getStatus()))
								|| (currentUser.equals(processTaskStepSubtask.getUserUuid()) && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus()))) {
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
							subtaskList.add(processTaskStepSubtask);
						}
					}
					processTaskStepVo.setProcessTaskStepSubtaskList(subtaskList);
				}
				
				//获取可分配处理人的步骤列表				
				ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
				processTaskStepWorkerPolicyVo.setProcessTaskId(processTaskId);
				List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
				if(CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
					List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
					for(ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
						if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
							List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
							for(String processStepUuid : processStepUuidList) {
								if(processTaskStepVo.getProcessStepUuid().equals(processStepUuid)) {
									List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
									if(CollectionUtils.isEmpty(majorList)) {
										ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
										assignableWorkerStep.setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
										assignableWorkerStepList.add(assignableWorkerStep);
									}
								}
							}
						}
					}
					processTaskStepVo.setAssignableWorkerStepList(assignableWorkerStepList);
				}
				
				//时效列表
				List<ProcessTaskSlaVo> processTaskSlaList = processTaskMapper.getProcessTaskSlaByProcessTaskStepId(processTaskStepId);
				for(ProcessTaskSlaVo processTaskSlaVo : processTaskSlaList) {
					ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaVo.getSlaTimeVo();
					if(processTaskSlaTimeVo != null) {
						processTaskSlaTimeVo.setName(processTaskSlaVo.getName());
						if(processTaskSlaTimeVo.getExpireTime() != null) {
							long timeLeft = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(), System.currentTimeMillis(), processTaskSlaTimeVo.getExpireTime().getTime());
							processTaskSlaTimeVo.setTimeLeft(timeLeft);
							processTaskSlaTimeVo.setTimeLeftDesc(conversionTimeUnit(timeLeft));
						}
						if(processTaskSlaTimeVo.getRealExpireTime() != null) {
							long realTimeLeft = processTaskSlaTimeVo.getExpireTime().getTime() - System.currentTimeMillis();
							processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
							processTaskSlaTimeVo.setRealTimeLeftDesc(conversionTimeUnit(realTimeLeft));
						}
						processTaskStepVo.getSlaTimeList().add(processTaskSlaTimeVo);
					}
				}
				processTaskVo.setCurrentProcessTaskStep(processTaskStepVo);
			}
			//processtaskStepData
			ProcessTaskStepDataVo  stepDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),processTaskStepVo.getHandler()));
			if(stepDataVo != null) {
				JSONObject stepDataJson = stepDataVo.getData();
				processTaskStepVo.setProcessTaskStepData(stepDataJson);
				verifyActionList = new ArrayList<>();
				verifyActionList.add(ProcessTaskStepAction.WORK.getValue());
				actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
				if(actionList.removeAll(verifyActionList)) {//有处理权限
					stepDataJson.put("isStepUser", 1);
					if(processTaskStepVo.getHandler().equals(ProcessStepHandler.AUTOMATIC.getHandler())){
						JSONObject requestAuditJson = stepDataJson.getJSONObject("requestAudit");
						if(requestAuditJson.containsKey("status")
								&&requestAuditJson.getJSONObject("status").getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
							requestAuditJson.put("isRetry", 1);
						}
						JSONObject callbackAuditJson = stepDataJson.getJSONObject("callbackAudit");
						if(callbackAuditJson.containsKey("status")
								&&callbackAuditJson.getJSONObject("status").getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
							callbackAuditJson.put("isRetry", 1);
						}
					}
				}
			}
		}

		Map<String, String> formAttributeActionMap = new HashMap<>();
		List<String> verifyActionList = new ArrayList<>();
		verifyActionList.add(ProcessTaskStepAction.WORK.getValue());
		List<String> actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
		if(actionList.removeAll(verifyActionList)) {//有处理权限
			//表单属性显示控制
			List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
			if(processTaskStepFormAttributeList.size() > 0) {
				for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
					formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
				}
			}
			processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
		}else {
			processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 0);
		}
		return resultObj;
	}
	
	private String conversionTimeUnit(long milliseconds) {
		StringBuilder stringBuilder = new StringBuilder();
		milliseconds = Math.abs(milliseconds);
		if(milliseconds < 1000) {
			stringBuilder.append("0秒");
		} else {
			if(milliseconds >= (60 * 60 * 1000)) {
				long hours = milliseconds / (60 * 60 * 1000);
				stringBuilder.append(hours);
				stringBuilder.append("小时");
				milliseconds = milliseconds % (60 * 60 * 1000);
			}
			if(milliseconds >= (60 * 1000)) {
				long minutes = milliseconds / (60 * 1000);
				stringBuilder.append(minutes);
				stringBuilder.append("分钟");
				milliseconds = milliseconds % (60 * 1000);
			}
			if(milliseconds >= 1000) {
				long seconds = milliseconds / 1000;
				stringBuilder.append(seconds);
				stringBuilder.append("秒");
			}
		}
		return stringBuilder.toString();
	}
}
