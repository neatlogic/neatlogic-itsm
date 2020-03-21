package codedriver.module.process.api.processtask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ITree;
import codedriver.module.process.dto.PriorityVo;
import codedriver.module.process.dto.ProcessTaskConfigVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepCommentVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
public class ProcessTaskStepGetApi extends ApiComponentBase {
	
	private final static Logger logger = LoggerFactory.getLogger(ProcessTaskStepGetApi.class);
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private WorktimeMapper worktimeMapper;
	
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
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(!processTaskService.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.VIEW)) {
			throw new ProcessTaskRuntimeException("您没有权限执行此操作");
		}
		
		//获取工单基本信息(title、channel_uuid、config_hash、priority_uuid、status、start_time、end_time、expire_time、owner、ownerName、reporter、reporterName)
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
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
					String data = processTaskFormAttributeDataVo.getData();
					if(data.startsWith("[") && data.endsWith("]")) {
						List<String> dataList = JSON.parseArray(data, String.class);
						formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), dataList);
					}else {
						formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), data);
					}
				}
				processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
			}
		}
		
		JSONObject resultObj = new JSONObject();
		resultObj.put("processTask", processTaskVo);
		
		if(processTaskStepId != null) {
			//获取步骤信息
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
			if(StringUtils.isNotBlank(stepConfig)) {
				JSONObject stepConfigObj = null;
				try {
					stepConfigObj = JSONObject.parseObject(stepConfig);
				} catch (Exception ex) {
					logger.error("hash为"+processTaskStepVo.getConfigHash()+"的processtask_step_config内容不是合法的JSON格式", ex);
				}
				if (MapUtils.isNotEmpty(stepConfigObj)) {
					JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
					if (MapUtils.isNotEmpty(workerPolicyConfig)) {
						processTaskStepVo.setIsRequired(stepConfigObj.getInteger("isRequired"));
					}
				}
			}
			//处理人列表
			List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.MAJOR.getValue());
			if(CollectionUtils.isNotEmpty(majorUserList)) {
				processTaskStepVo.setMajorUserList(majorUserList);
			}
			List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.MINOR.getValue());
			if(CollectionUtils.isNotEmpty(minorUserList)) {
				processTaskStepVo.setMinorUserList(minorUserList);
			}
			List<ProcessTaskStepUserVo> agentUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.AGENT.getValue());
			if(CollectionUtils.isNotEmpty(agentUserList)) {
				processTaskStepVo.setAgentUserList(agentUserList);
			}
			//表单属性显示控制
			List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
			if(processTaskStepFormAttributeList.size() > 0) {
				Map<String, String> formAttributeActionMap = new HashMap<>();
				for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
					formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
				}
				processTaskStepVo.setFormAttributeActionMap(formAttributeActionMap);
			}
			//回复框内容和附件暂存回显
			ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
			processTaskStepAuditVo.setProcessTaskId(processTaskId);
			processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
			processTaskStepAuditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
			processTaskStepAuditVo.setUserId(UserContext.get().getUserId(true));
			List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
			if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
				ProcessTaskStepCommentVo temporaryComment = new ProcessTaskStepCommentVo(processTaskStepAuditList.get(0));
				processTaskStepVo.setComment(temporaryComment);
			}
			resultObj.put("processTaskStep", processTaskStepVo);
		}
		return resultObj;
	}

}
