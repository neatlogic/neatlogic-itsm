package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ITree;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessTaskConfigVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
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
		@Param(name = "processTask", explode = ProcessTaskVo.class, desc = "工单信息"),
		@Param(name = "processTaskStep", explode = ProcessTaskStepVo.class, desc = "工单步骤信息")
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
		if(processTaskStepId != null) {
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
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
		Long startProcessTaskStepId = processTaskStepList.get(0).getId();
		//获取上报描述内容
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepId);
		if(!processTaskStepContentList.isEmpty()) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo != null) {
				processTaskVo.setContent(processTaskContentVo.getContent());
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
			processTaskVo.setFileList(fileList);
		}
		//优先级
		PriorityVo priorityVo = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
		processTaskVo.setPriority(priorityVo);
		//上报服务路径
		ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
		if(channelVo != null) {
			StringBuilder channelPath = new StringBuilder(channelVo.getName());
			String parentUuid = channelVo.getParentUuid();
			while(!ITree.ROOT_UUID.equals(parentUuid)) {
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
//					String data = processTaskFormAttributeDataVo.getData();
//					if(data != null) {
//						if(data.startsWith("[") && data.endsWith("]")) {
//							List<String> dataList = JSON.parseArray(data, String.class);
//							formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), dataList);
//						}else {
//							formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), data);
//						}
//					}
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
				ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
				String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
				processTaskStepVo.setConfig(stepConfig);
//				if(StringUtils.isNotBlank(stepConfig)) {
//					JSONObject stepConfigObj = null;
//					try {
//						stepConfigObj = JSONObject.parseObject(stepConfig);
//					} catch (Exception ex) {
//						logger.error("hash为"+processTaskStepVo.getConfigHash()+"的processtask_step_config内容不是合法的JSON格式", ex);
//					}
//					if (MapUtils.isNotEmpty(stepConfigObj)) {
//						JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
//						if (MapUtils.isNotEmpty(workerPolicyConfig)) {
//							processTaskStepVo.setIsRequired(workerPolicyConfig.getInteger("isRequired"));
//						}
//					}
//				}
				//处理人列表
				List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
				if(CollectionUtils.isNotEmpty(majorUserList)) {
					processTaskStepVo.setMajorUserList(majorUserList);
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
				ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
				processTaskStepAuditVo.setProcessTaskId(processTaskId);
				processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
				processTaskStepAuditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
				processTaskStepAuditVo.setUserId(UserContext.get().getUserId(true));
				List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
				if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
					ProcessTaskStepAuditVo processTaskStepAudit = processTaskStepAuditList.get(0);					
					for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAudit.getAuditDetailList()) {
						if(ProcessTaskAuditDetailType.FORM.getValue().equals(processTaskStepAuditDetailVo.getType())) {
							List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = JSON.parseArray(processTaskStepAuditDetailVo.getNewContent(), ProcessTaskFormAttributeDataVo.class);
							if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
								Map<String, Object> formAttributeDataMap = new HashMap<>();
								for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
									formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
//									String data = processTaskFormAttributeDataVo.getData();
//									if(data.startsWith("[") && data.endsWith("]")) {
//										List<String> dataList = JSON.parseArray(data, String.class);
//										formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), dataList);
//									}else {
//										formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), data);
//									}
								}
								processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
							}
						}
					}
					processTaskStepVo.setComment(new ProcessTaskStepCommentVo(processTaskStepAudit));
				}
				resultObj.put("processTaskStep", processTaskStepVo);
			}
		}

		Map<String, String> formAttributeActionMap = new HashMap<>();
		List<String> verifyActionList = new ArrayList<>();
		verifyActionList.add(ProcessTaskStepAction.COMPLETE.getValue());
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

}
