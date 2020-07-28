package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import codedriver.module.process.notify.handler.ProcessNotifyPolicyHandler;

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
				this.put("icon", "tsfont-circle-o");
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

		JSONObject workerPolicyConfig = null;
		try {
			JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.getParamObj().putAll(stepConfigObj);
			if (MapUtils.isNotEmpty(stepConfigObj)) {
				workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
			}
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}
		if(workerPolicyConfig == null) {
			workerPolicyConfig = new JSONObject();
		}
		
		/** 如果已经存在过处理人，则继续使用旧处理人，否则启用分派 **/
		List<ProcessTaskStepUserVo> oldUserList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
		if (oldUserList.size() > 0) {
			ProcessTaskStepUserVo oldUserVo = oldUserList.get(0);
			workerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(), oldUserVo.getUserUuid(), ProcessUserType.MAJOR.getValue()));
		} else {
			/** 分配处理人 **/
			ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
			processTaskStepWorkerPolicyVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
			List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
			if (CollectionUtils.isNotEmpty(workerPolicyList)) {
				for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
					IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
					if (workerPolicyHandler != null) {
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
				}
			}
		}
		/** 当只分配到一个用户时，自动设置为处理人，不需要抢单 **/
		if (workerList.size() == 1) {
			if (StringUtils.isNotBlank(workerList.get(0).getUuid()) && GroupSearch.USER.getValue().equals(workerList.get(0).getType())) {
				ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
				userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				userVo.setUserUuid(workerList.get(0).getUuid());
				UserVo user = userMapper.getUserBaseInfoByUuid(workerList.get(0).getUuid());
				userVo.setUserName(user.getUserName());
				userList.add(userVo);	
				String autoStart = workerPolicyConfig.getString("autoStart");
				/** 当步骤设置了自动开始时，设置当前步骤状态为处理中 **/
				if ("1".equals(autoStart)) {
					currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
				}			
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
		List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(currentProcessTaskStepVo.getId(),null);
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
		DataValid.assignWorkerValid(currentProcessTaskStepVo);
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
		return 1;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
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

		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		try {
			String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
			JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
			if (MapUtils.isNotEmpty(stepConfigObj)) {
				JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
				if (MapUtils.isNotEmpty(workerPolicyConfig)) {
					String autoStart = workerPolicyConfig.getString("autoStart");
					if ("1".equals(autoStart) && workerList.size() == 1) {
						/** 设置当前步骤状态为处理中 **/
						if (StringUtils.isNotBlank(workerList.get(0).getUuid()) && GroupSearch.USER.getValue().equals(workerList.get(0).getType())) {
							ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
							userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
							userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
							userVo.setUserUuid(workerList.get(0).getUuid());
							UserVo user = userMapper.getUserBaseInfoByUuid(workerList.get(0).getUuid());
							userVo.setUserName(user.getUserName());
							userList.add(userVo);
							currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}	
		return 1;
	}

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		/** 组装通知策略id **/
		JSONObject notifyPolicyConfig = stepConfigObj.getJSONObject("notifyPolicyConfig");
		if(MapUtils.isNotEmpty(notifyPolicyConfig)) {
	        Long policyId = notifyPolicyConfig.getLong("policyId");
	        if(policyId != null) {
	        	processStepVo.setNotifyPolicyId(policyId);
	        }
		}
		/** 组装分配策略 **/
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (MapUtils.isNotEmpty(workerPolicyConfig)) {
			JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
			if (CollectionUtils.isNotEmpty(policyList)) {
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
		return 1;
	}
	
	@Override
	public void updateProcessTaskStepUserAndWorker(List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) {
		
		for(ProcessTaskStepUserVo processTaskStepUserVo : userList) {
			//查出userUuid在当前步骤拥有的子任务
			ProcessTaskStepSubtaskVo stepSubtaskVo = new ProcessTaskStepSubtaskVo();
			stepSubtaskVo.setProcessTaskId(processTaskStepUserVo.getProcessTaskId());
			stepSubtaskVo.setProcessTaskStepId(processTaskStepUserVo.getProcessTaskStepId());
			stepSubtaskVo.setUserUuid(processTaskStepUserVo.getUserUuid());
			List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(stepSubtaskVo);
			//子任务状态列表
			List<String> stepSubtaskStatusList = processTaskStepSubtaskList.stream().map(ProcessTaskStepSubtaskVo::getStatus).collect(Collectors.toList());
			
			if(stepSubtaskStatusList.contains(ProcessTaskStatus.RUNNING.getValue())) {
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
			}else if(stepSubtaskStatusList.contains(ProcessTaskStatus.SUCCEED.getValue())) {
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
			}else {//userUuid不是任何子任务处理人
				processTaskStepUserVo.setStatus(null);
			}
			String minorUserStatus = null;//userUuid是子任务处理人时的状态，null代表userUuid不是子任务处理人
			List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepUserVo.getProcessTaskStepId(), ProcessUserType.MINOR.getValue());
			for(ProcessTaskStepUserVo stepUser : processTaskStepUserList) {
				if(processTaskStepUserVo.getUserUuid().equals(stepUser.getUserUuid())) {
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
			//查出userUuid在当前步骤拥有的子任务
			ProcessTaskStepSubtaskVo stepSubtaskVo = new ProcessTaskStepSubtaskVo();
			stepSubtaskVo.setProcessTaskId(processTaskStepWorkerVo.getProcessTaskId());
			stepSubtaskVo.setProcessTaskStepId(processTaskStepWorkerVo.getProcessTaskStepId());
			stepSubtaskVo.setUserUuid(processTaskStepWorkerVo.getUuid());
			List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(stepSubtaskVo);
			//子任务状态列表
			List<String> stepSubtaskStatusList = processTaskStepSubtaskList.stream().map(ProcessTaskStepSubtaskVo::getStatus).collect(Collectors.toList());
			String minorUserStatus = null;//userUuid是子任务处理人时的状态，null代表userUuid不是子任务处理人
			if(stepSubtaskStatusList.contains(ProcessTaskStatus.RUNNING.getValue())) {
				minorUserStatus = ProcessTaskStepUserStatus.DOING.getValue();
			}else if(stepSubtaskStatusList.contains(ProcessTaskStatus.SUCCEED.getValue())) {
				minorUserStatus = ProcessTaskStepUserStatus.DONE.getValue();
			}
			
			String majorUserStatus = null;//userUuid是主处理人时的状态，null代表userUuid不是主处理人
			List<ProcessTaskStepUserVo> stepMajorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepWorkerVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
			for(ProcessTaskStepUserVo stepUser : stepMajorUserList) {
				if(processTaskStepWorkerVo.getUuid().equals(stepUser.getUserUuid())) {
					majorUserStatus = stepUser.getStatus();
				}
			}
			
			List<ProcessTaskStepWorkerVo> processTaskWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepWorkerVo.getProcessTaskStepId());
			List<String> userUuidList = processTaskWorkerList.stream().filter(e -> GroupSearch.USER.getValue().equals(e.getType())).map(ProcessTaskStepWorkerVo::getUuid).collect(Collectors.toList());
			
			if(ProcessTaskStepUserStatus.DOING.getValue().equals(majorUserStatus) 
					|| ProcessTaskStepUserStatus.DOING.getValue().equals(minorUserStatus)) {//如果userUuid是主处理人或子任务处理人，且状态时doing
				if(!userUuidList.contains(processTaskStepWorkerVo.getUuid())) {//processtask_step_worker不存在userUuid数据
					//插入processTaskStepWorker
					processTaskMapper.insertProcessTaskStepWorker(processTaskStepWorkerVo);
				}
			}else {
				if(userUuidList.contains(processTaskStepWorkerVo.getUuid())) {//processtask_step_worker存在userUuid数据
					//删除processTaskStepWorker
					processTaskMapper.deleteProcessTaskStepWorker(processTaskStepWorkerVo);
				}
			}
		}
	}	
	
	@SuppressWarnings("serial")
	@Override
	public JSONObject makeupConfig(JSONObject configObj) {				
		if(configObj == null) {
			configObj = new JSONObject();
		}
		JSONObject resultObj = new JSONObject();
		
		/** 授权 **/
		JSONArray authorityArray = new JSONArray();
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.VIEW.getValue());this.put("text", ProcessTaskStepAction.VIEW.getText());this.put("acceptList", Arrays.asList(GroupSearch.COMMON.getValuePlugin() + UserType.ALL.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.ABORT.getValue());this.put("text", ProcessTaskStepAction.ABORT.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.TRANSFER.getValue());this.put("text", ProcessTaskStepAction.TRANSFER.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.UPDATE.getValue());this.put("text", ProcessTaskStepAction.UPDATE.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.URGE.getValue());this.put("text", ProcessTaskStepAction.URGE.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});

		JSONArray authorityList = configObj.getJSONArray("authorityList");
		if(CollectionUtils.isNotEmpty(authorityList)) {
			Map<String, JSONArray> authorityMap = new HashMap<>();
			for(int i = 0; i < authorityList.size(); i++) {
				JSONObject authority = authorityList.getJSONObject(i);
				authorityMap.put(authority.getString("action"), authority.getJSONArray("acceptList"));
			}
			for(int i = 0; i < authorityArray.size(); i++) {
				JSONObject authority = authorityArray.getJSONObject(i);
				JSONArray acceptList = authorityMap.get(authority.getString("action"));
				if(acceptList != null) {
					authority.put("acceptList", acceptList);
				}
			}
		}
		resultObj.put("authorityList", authorityArray);
		
		/** 按钮映射列表 **/
		JSONArray customButtonArray = new JSONArray();
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.COMPLETE.getValue());this.put("customText", ProcessTaskStepAction.COMPLETE.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.BACK.getValue());this.put("customText", ProcessTaskStepAction.BACK.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.COMMENT.getValue());this.put("customText", ProcessTaskStepAction.COMMENT.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.TRANSFER.getValue());this.put("customText", ProcessTaskStepAction.TRANSFER.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.START.getValue());this.put("customText", ProcessTaskStepAction.START.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.ABORT.getValue());this.put("customText", ProcessTaskStepAction.ABORT.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.RECOVER.getValue());this.put("customText", ProcessTaskStepAction.RECOVER.getText());this.put("value", "");}});
		
		/** 子任务按钮映射列表 **/
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.ABORTSUBTASK.getValue());this.put("customText", ProcessTaskStepAction.ABORTSUBTASK.getText() + "(子任务)");this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.COMMENTSUBTASK.getValue());this.put("customText", ProcessTaskStepAction.COMMENTSUBTASK.getText() + "(子任务)");this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.COMPLETESUBTASK.getValue());this.put("customText", ProcessTaskStepAction.COMPLETESUBTASK.getText() + "(子任务)");this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.CREATESUBTASK.getValue());this.put("customText", ProcessTaskStepAction.CREATESUBTASK.getText() + "(子任务)");this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.REDOSUBTASK.getValue());this.put("customText", ProcessTaskStepAction.REDOSUBTASK.getText() + "(子任务)");this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.EDITSUBTASK.getValue());this.put("customText", ProcessTaskStepAction.EDITSUBTASK.getText() + "(子任务)");this.put("value", "");}});
		
		JSONArray customButtonList = configObj.getJSONArray("customButtonList");
		if(CollectionUtils.isNotEmpty(customButtonList)) {
			Map<String, String> customButtonMap = new HashMap<>();
			for(int i = 0; i < customButtonList.size(); i++) {
				JSONObject customButton = customButtonList.getJSONObject(i);
				customButtonMap.put(customButton.getString("name"), customButton.getString("value"));
			}
			for(int i = 0; i < customButtonArray.size(); i++) {
				JSONObject customButton = customButtonArray.getJSONObject(i);
				String value = customButtonMap.get(customButton.getString("name"));
				if(StringUtils.isNotBlank(value)) {
					customButton.put("value", value);
				}
			}
		}
		resultObj.put("customButtonList", customButtonArray);
		
		/** 状态映射列表 **/
//		JSONArray customStatusList = new JSONArray();
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.RUNNING.getValue());this.put("text", ProcessTaskStatus.RUNNING.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.ABORTED.getValue());this.put("text", ProcessTaskStatus.ABORTED.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.SUCCEED.getValue());this.put("text", ProcessTaskStatus.SUCCEED.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.PENDING.getValue());this.put("text", ProcessTaskStatus.PENDING.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.FAILED.getValue());this.put("text", ProcessTaskStatus.FAILED.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.ABORTING.getValue());this.put("text", ProcessTaskStatus.ABORTING.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.BACK.getValue());this.put("text", ProcessTaskStatus.BACK.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.HANG.getValue());this.put("text", ProcessTaskStatus.HANG.getText());this.put("value", "");}});
//		customStatusList.add(new JSONObject() {{this.put("name", ProcessTaskStatus.DRAFT.getValue());this.put("text", ProcessTaskStatus.DRAFT.getText());this.put("value", "");}});
//		resultObj.put("customStatusList", customStatusList);
		
		/** 通知 **/
		JSONObject notifyPolicyObj = new JSONObject();
		JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
		if(MapUtils.isNotEmpty(notifyPolicyConfig)) {
			notifyPolicyObj.putAll(notifyPolicyConfig);
		}
		notifyPolicyObj.put("handler", ProcessNotifyPolicyHandler.class.getName());
		resultObj.put("notifyPolicyConfig", notifyPolicyObj);
		
		return resultObj;
	}

	@Override
	public Object getHandlerStepInfo(Long processTaskStepId) {
		// TODO Auto-generated method stub
		return null;
	}
}