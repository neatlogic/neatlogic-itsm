package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;

@Service
public class OmnipotentProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(OmnipotentProcessComponent.class);

	@Override
	public String getHandler() {
		return ProcessStepHandler.OMNIPOTENT.getHandler();
	}

	@Override
	public String getType() {
		return ProcessStepHandler.OMNIPOTENT.getType();
	}

	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.MT;
	}

	@SuppressWarnings("serial")
	@Override
	public JSONObject getChartConfig() {
		return new JSONObject() {
			{
				this.put("icon", "ts-round-s");
				this.put("shape", "L-rectangle:R-rectangle");
				this.put("width", 68);
				this.put("height", 40);
			}
		};
	}

	@Override
	public String getName() {
		return ProcessStepHandler.OMNIPOTENT.getName();
	}

	@Override
	public int getSort() {
		return 3;
	}

	@Override
	protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {

		return 0;
	}

	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		/** 分配处理人 **/
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

		if (StringUtils.isBlank(stepConfig)) {
			return 1;
		}
		JSONObject stepConfigObj = null;
		try {
			stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.setParamObj(stepConfigObj);
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}
		if (MapUtils.isEmpty(stepConfigObj)) {
			return 1;
		}
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (MapUtils.isEmpty(workerPolicyConfig)) {
			return 1;
		}

		List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicyByProcessTaskStepId(currentProcessTaskStepVo.getId());
		if (CollectionUtils.isEmpty(workerPolicyList)) {
			return 1;
		}
		for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
			IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
			if (workerPolicyHandler == null) {
				continue;
			}
			List<ProcessTaskStepWorkerVo> tmpWorkerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
			/** 顺序分配处理人 **/
			if ("sort".equals(workerPolicyConfig.getString("executeMode")) && tmpWorkerList.size() > 0) {
				// 找到处理人，则退出
				workerList.addAll(tmpWorkerList);
				break;
			} else if ("batch".equals(workerPolicyConfig.getString("executeMode"))) {
				// 去重取并集
				tmpWorkerList.removeAll(workerList);
				workerList.addAll(tmpWorkerList);
			}
		}

		String autoStart = workerPolicyConfig.getString("autoStart");
		if ("1".equals(autoStart) && workerList.size() == 1) {
			/** 设置当前步骤状态为处理中 **/
			if (StringUtils.isNotBlank(workerList.get(0).getUserId())) {
				ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
				userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				userVo.setUserId(workerList.get(0).getUserId());
				UserVo user = userMapper.getUserByUserId(workerList.get(0).getUserId());
				userVo.setUserName(user.getUserName());
				userList.add(userVo);
				currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
			}
		}

		return 1;
	}

	@Override
	protected int myStart(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	public Boolean isAllowStart() {
		return true;
	}

	@Override
	public List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		List<ProcessTaskStepVo> returnNextStepList = new ArrayList<>();
		List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromId(currentProcessTaskStepVo.getId());
		if (nextStepList.size() == 1) {
			return nextStepList;
		} else if (nextStepList.size() > 1) {
			JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
			if (paramObj != null && paramObj.containsKey("nextStepId")) {
				Long nextStepId = paramObj.getLong("nextStepId");
				for (ProcessTaskStepVo processTaskStepVo : nextStepList) {
					if (processTaskStepVo.getId().equals(nextStepId)) {
						returnNextStepList.add(processTaskStepVo);
						break;
					}
				}
			} else {
				throw new ProcessTaskException("找到多个后续节点");
			}
		}
		return returnNextStepList;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		baseInfoValid(currentProcessTaskStepVo);
		return 1;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
		/** 保存描述内容 **/
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
		}
		return 1;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		/** 保存描述内容 **/
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
		}
		return 1;
	}

	@Override
	protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		/** 保存描述内容 **/
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

		if (StringUtils.isBlank(stepConfig)) {
			return 1;
		}
		JSONObject stepConfigObj = null;
		try {
			stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.setParamObj(stepConfigObj);
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}
		if (MapUtils.isEmpty(stepConfigObj)) {
			return 1;
		}
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (MapUtils.isEmpty(workerPolicyConfig)) {
			return 1;
		}
		String autoStart = workerPolicyConfig.getString("autoStart");
		if ("1".equals(autoStart) && workerList.size() == 1) {
			/** 设置当前步骤状态为处理中 **/
			if (StringUtils.isNotBlank(workerList.get(0).getUserId())) {
				ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
				userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				userVo.setUserId(workerList.get(0).getUserId());
				UserVo user = userMapper.getUserByUserId(workerList.get(0).getUserId());
				userVo.setUserName(user.getUserName());
				userList.add(userVo);
				currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
			}
		}
		return 1;
	}

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		/** 组装通知模板 **/
		JSONArray notifyList = stepConfigObj.getJSONArray("notifyList");
		if (!CollectionUtils.isEmpty(notifyList)) {
			List<String> templateUuidList = new ArrayList<>();
			for (int j = 0; j < notifyList.size(); j++) {
				JSONObject notifyObj = notifyList.getJSONObject(j);
				String template = notifyObj.getString("template");
				if (StringUtils.isNotBlank(template)) {
					templateUuidList.add(template);
				}
			}
			processStepVo.setTemplateUuidList(templateUuidList);
		}
		/** 组装分配策略 **/
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (!MapUtils.isEmpty(workerPolicyConfig)) {
			JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
			if (!CollectionUtils.isEmpty(policyList)) {
				List<ProcessStepWorkerPolicyVo> workerPolicyList = new ArrayList<>();
				for (int k = 0; k < policyList.size(); k++) {
					JSONObject policyObj = policyList.getJSONObject(k);
					if (!"1".equals(policyObj.getString("isChecked"))) {
						continue;
					}
					ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
					processStepWorkerPolicyVo.setProcessUuid(processStepVo.getProcessUuid());
					processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
					processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
					processStepWorkerPolicyVo.setSort(k + 1);
					processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
					workerPolicyList.add(processStepWorkerPolicyVo);
				}
				processStepVo.setWorkerPolicyList(workerPolicyList);
			}
		}
	}

	@Override
	protected int mySaveDraft(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();

		/** 更新工单信息 **/
		ProcessTaskVo processTaskVo = new ProcessTaskVo();
		processTaskVo.setId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskVo.setTitle(paramObj.getString("title"));
		processTaskVo.setOwner(paramObj.getString("owner"));
		processTaskVo.setPriorityUuid(paramObj.getString("priorityUuid"));
		processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);

		/** 保存描述内容 **/
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
		}

		/** 保存附件uuid **/
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		processTaskMapper.deleteProcessTaskFile(processTaskFileVo);
		String fileUuidListStr = paramObj.getString("fileUuidList");
		if (StringUtils.isNotBlank(fileUuidListStr)) {
			List<String> fileUuidList = JSON.parseArray(fileUuidListStr, String.class);
			for (String fileUuid : fileUuidList) {
				processTaskFileVo.setFileUuid(fileUuid);
				processTaskMapper.insertProcessTaskFile(processTaskFileVo);
			}
		}
		return 1;
	}
	
	@Override
	public void updateProcessTaskStepUserAndWorker(List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) {
		
		for(ProcessTaskStepUserVo processTaskStepUserVo : userList) {
			//查出userId在当前步骤拥有的子任务
			ProcessTaskStepSubtaskVo stepSubtaskVo = new ProcessTaskStepSubtaskVo();
			stepSubtaskVo.setProcessTaskId(processTaskStepUserVo.getProcessTaskId());
			stepSubtaskVo.setProcessTaskStepId(processTaskStepUserVo.getProcessTaskStepId());
			stepSubtaskVo.setUserId(processTaskStepUserVo.getUserId());
			List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(stepSubtaskVo);
			//子任务状态列表
			List<String> stepSubtaskStatusList = processTaskStepSubtaskList.stream().map(ProcessTaskStepSubtaskVo::getStatus).collect(Collectors.toList());
			
			if(stepSubtaskStatusList.contains(ProcessTaskStatus.RUNNING.getValue())) {
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
			}else if(stepSubtaskStatusList.contains(ProcessTaskStatus.SUCCEED.getValue())) {
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
			}else {//userId不是任何子任务处理人
				processTaskStepUserVo.setStatus(null);
			}
			String minorUserStatus = null;//userId是子任务处理人时的状态，null代表userId不是子任务处理人
			List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepUserVo.getProcessTaskStepId(), ProcessUserType.MINOR.getValue());
			for(ProcessTaskStepUserVo stepUser : processTaskStepUserList) {
				if(processTaskStepUserVo.getUserId().equals(stepUser.getUserId())) {
					minorUserStatus = stepUser.getStatus();
				}
			}
			if(minorUserStatus == null && processTaskStepUserVo.getStatus() == null) {
				//processtask_step_subtask表和processtask_step_user表都没有数据
				//不增不减不更新
			}else if(minorUserStatus == null && processTaskStepUserVo.getStatus() != null) {
				processTaskMapper.insertProcessTaskStepUser(processTaskStepUserVo);
			}else if(minorUserStatus != null && processTaskStepUserVo.getStatus() == null){
				processTaskMapper.deleteProcessTaskStepUser(processTaskStepUserVo);
			}else if(!processTaskStepUserVo.getStatus().equals(minorUserStatus)){
				processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
			}
		}
		
		for(ProcessTaskStepWorkerVo processTaskStepWorkerVo : workerList) {
			//查出userId在当前步骤拥有的子任务
			ProcessTaskStepSubtaskVo stepSubtaskVo = new ProcessTaskStepSubtaskVo();
			stepSubtaskVo.setProcessTaskId(processTaskStepWorkerVo.getProcessTaskId());
			stepSubtaskVo.setProcessTaskStepId(processTaskStepWorkerVo.getProcessTaskStepId());
			stepSubtaskVo.setUserId(processTaskStepWorkerVo.getUserId());
			List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(stepSubtaskVo);
			//子任务状态列表
			List<String> stepSubtaskStatusList = processTaskStepSubtaskList.stream().map(ProcessTaskStepSubtaskVo::getStatus).collect(Collectors.toList());
			String minorUserStatus = null;//userId是子任务处理人时的状态，null代表userId不是子任务处理人
			if(stepSubtaskStatusList.contains(ProcessTaskStatus.RUNNING.getValue())) {
				minorUserStatus = ProcessTaskStepUserStatus.DOING.getValue();
			}else if(stepSubtaskStatusList.contains(ProcessTaskStatus.SUCCEED.getValue())) {
				minorUserStatus = ProcessTaskStepUserStatus.DONE.getValue();
			}
			
			String majorUserStatus = null;//userId是主处理人时的状态，null代表userId不是主处理人
			List<ProcessTaskStepUserVo> stepMajorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepWorkerVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
			for(ProcessTaskStepUserVo stepUser : stepMajorUserList) {
				if(processTaskStepWorkerVo.getUserId().equals(stepUser.getUserId())) {
					majorUserStatus = stepUser.getStatus();
				}
			}
			
			List<ProcessTaskStepWorkerVo> processTaskWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepWorkerVo.getProcessTaskStepId());
			List<String> userIdList = processTaskWorkerList.stream().map(ProcessTaskStepWorkerVo::getUserId).collect(Collectors.toList());
			
			if(ProcessTaskStepUserStatus.DOING.getValue().equals(majorUserStatus) 
					|| ProcessTaskStepUserStatus.DOING.getValue().equals(minorUserStatus)) {//如果userId是主处理人或子任务处理人，且状态时doing
				if(!userIdList.contains(processTaskStepWorkerVo.getUserId())) {//processtask_step_worker不存在userId数据
					//插入processTaskStepWorker
					processTaskMapper.insertProcessTaskStepWorker(processTaskStepWorkerVo);
				}
			}else {
				if(userIdList.contains(processTaskStepWorkerVo.getUserId())) {//processtask_step_worker存在userId数据
					//删除processTaskStepWorker
					processTaskMapper.deleteProcessTaskStepWorker(processTaskStepWorkerVo.getProcessTaskStepId(), processTaskStepWorkerVo.getUserId());
				}
			}
		}
	}
	
	private boolean baseInfoValid(ProcessTaskStepVo currentProcessTaskStepVo) {
		JSONObject paramObj = new JSONObject();
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
		if(processTaskVo.getTitle() == null) {
			throw new ProcessTaskRuntimeException("工单标题格式不能为空");
		}
		Pattern titlePattern = Pattern.compile("^[A-Za-z_\\d\\u4e00-\\u9fa5]+$");
		if (!titlePattern.matcher(processTaskVo.getTitle()).matches()) {
			throw new ProcessTaskRuntimeException("工单标题格式不对");
		}
		paramObj.put(ProcessTaskAuditDetailType.TITLE.getParamName(), processTaskVo.getTitle());
		if (StringUtils.isBlank(processTaskVo.getOwner())) {
			throw new ProcessTaskRuntimeException("工单请求人不能为空");
		}
		if (userMapper.getUserBaseInfoByUserId(processTaskVo.getOwner()) == null) {
			throw new ProcessTaskRuntimeException("工单请求人账号:'" + processTaskVo.getOwner() + "'不存在");
		}
		if (StringUtils.isBlank(processTaskVo.getPriorityUuid())) {
			throw new ProcessTaskRuntimeException("工单优先级不能为空");
		}
		List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(processTaskVo.getChannelUuid());
		List<String> priorityUuidlist = new ArrayList<>(channelPriorityList.size());
		for (ChannelPriorityVo channelPriorityVo : channelPriorityList) {
			priorityUuidlist.add(channelPriorityVo.getPriorityUuid());
		}
		if (!priorityUuidlist.contains(processTaskVo.getPriorityUuid())) {
			throw new ProcessTaskRuntimeException("工单优先级与服务优先级级不匹配");
		}
		paramObj.put(ProcessTaskAuditDetailType.PRIORITY.getParamName(), processTaskVo.getPriorityUuid());

		// 获取上报描述内容
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(currentProcessTaskStepVo.getId());
		if (CollectionUtils.isNotEmpty(processTaskStepContentList)) {
			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), processTaskStepContentList.get(0).getContentHash());
		}
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		if (processTaskFileList.size() > 0) {
			List<String> fileUuidList = new ArrayList<>();
			for (ProcessTaskFileVo processTaskFile : processTaskFileList) {
				if (fileMapper.getFileByUuid(processTaskFile.getFileUuid()) == null) {
					throw new ProcessTaskRuntimeException("上传附件uuid:'" + processTaskFile.getFileUuid() + "'不存在");
				}
				fileUuidList.add(processTaskFile.getFileUuid());
			}
			paramObj.put(ProcessTaskAuditDetailType.FILE.getParamName(), fileUuidList);
		}
		currentProcessTaskStepVo.setParamObj(paramObj);
		return true;
	}
}