package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
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
	
	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
		/** 获取步骤配置信息 **/
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

		String executeMode = "";
		int autoStart = 0;
		try {
			JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.getParamObj().putAll(stepConfigObj);
			if (MapUtils.isNotEmpty(stepConfigObj)) {
				JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
				if(MapUtils.isNotEmpty(stepConfigObj)) {
					executeMode = workerPolicyConfig.getString("executeMode");
					autoStart = workerPolicyConfig.getIntValue("autoStart");
				}
			}
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}
		
		/** 如果workerList.size()>0，说明已经存在过处理人，则继续使用旧处理人，否则启用分派 **/
		if (CollectionUtils.isEmpty(workerList))  {
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
						if ("sort".equals(executeMode) && CollectionUtils.isEmpty(tmpWorkerList)) {
							// 找到处理人，则退出
							workerList.addAll(tmpWorkerList);
							break;
						} else if ("batch".equals(executeMode)) {
							// 去重取并集
							tmpWorkerList.removeAll(workerList);
							workerList.addAll(tmpWorkerList);
						}
					}
				}
			}
		}
		
		return autoStart;
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
	protected Set<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepVo> nextStepList, Long nextStepId) throws ProcessTaskException {
		Set<ProcessTaskStepVo> nextStepSet = new HashSet<>();
		if (nextStepList.size() == 1) {
			nextStepSet.add(nextStepList.get(0));
		} else if (nextStepList.size() > 1) {
			if(nextStepId == null) {
				throw new ProcessTaskException("找到多个后续节点");
			}
			for (ProcessTaskStepVo processTaskStepVo : nextStepList) {
				if (processTaskStepVo.getId().equals(nextStepId)) {
					nextStepSet.add(processTaskStepVo);
					break;
				}
			}
		}
		return nextStepSet;
//		List<ProcessTaskStepVo> returnNextStepList = new ArrayList<>();
//		List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(currentProcessTaskStepVo.getId(),null);
//		if (nextStepList.size() == 1) {
//			return nextStepList;
//		} else if (nextStepList.size() > 1) {
//			JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
//			if (paramObj != null && paramObj.containsKey("nextStepId")) {
//				Long nextStepId = paramObj.getLong("nextStepId");
//				for (ProcessTaskStepVo processTaskStepVo : nextStepList) {
//					if (processTaskStepVo.getId().equals(nextStepId)) {
//						returnNextStepList.add(processTaskStepVo);
//						break;
//					}
//				}
//			} else {
//				throw new ProcessTaskException("找到多个后续节点");
//			}
//		}
//		return returnNextStepList;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
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
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
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
	public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
		/** 查出processtask_step_subtask表中当前步骤子任务处理人列表 **/		
		Set<String> runningSubtaskUserUuidSet = new HashSet<>();
		Set<String> succeedSubtaskUserUuidSet = new HashSet<>();
		ProcessTaskStepSubtaskVo stepSubtaskVo = new ProcessTaskStepSubtaskVo();
		stepSubtaskVo.setProcessTaskId(processTaskId);
		stepSubtaskVo.setProcessTaskStepId(processTaskStepId);
		List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(stepSubtaskVo);
		for(ProcessTaskStepSubtaskVo subtaskVo : processTaskStepSubtaskList) {
			if(ProcessTaskStatus.RUNNING.getValue().equals(subtaskVo.getStatus())) {
				runningSubtaskUserUuidSet.add(subtaskVo.getUserUuid());
			}else if(ProcessTaskStatus.SUCCEED.getValue().equals(subtaskVo.getStatus())) {
				succeedSubtaskUserUuidSet.add(subtaskVo.getUserUuid());
			}
		}
		
		/** 查出processtask_step_worker表中当前步骤子任务处理人列表 **/
		Set<String> workerMinorUserUuidSet = new HashSet<>();
		Set<String> workerMinorUserUuidSet2 = new HashSet<>();
		List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepId);
		for(ProcessTaskStepWorkerVo workerVo : workerList) {
			if(ProcessUserType.MINOR.getValue().equals(workerVo.getUserType())) {
				workerMinorUserUuidSet.add(workerVo.getUuid());
				workerMinorUserUuidSet2.add(workerVo.getUuid());
			}
		}
		
		/** 查出processtask_step_user表中当前步骤子任务处理人列表 **/
		Set<String> doingMinorUserUuidSet = new HashSet<>();
		Set<String> doneMinorUserUuidSet = new HashSet<>();
		List<ProcessTaskStepUserVo> minorUserList =  processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
		for(ProcessTaskStepUserVo userVo : minorUserList) {
			if(ProcessTaskStepUserStatus.DOING.getValue().equals(userVo.getStatus())) {
				doingMinorUserUuidSet.add(userVo.getUserUuid());
			}else if(ProcessTaskStepUserStatus.DONE.getValue().equals(userVo.getStatus())) {
				doneMinorUserUuidSet.add(userVo.getUserUuid());
			}
		}
		
		ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo();
		processTaskStepWorkerVo.setProcessTaskId(processTaskId);
		processTaskStepWorkerVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepWorkerVo.setType(GroupSearch.USER.getValue());
		processTaskStepWorkerVo.setUserType(ProcessUserType.MINOR.getValue());
		
		ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
		processTaskStepUserVo.setProcessTaskId(processTaskId);
		processTaskStepUserVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
		/** 删除processtask_step_worker表中当前步骤多余的子任务处理人 **/
		workerMinorUserUuidSet.removeAll(runningSubtaskUserUuidSet);		
		for(String userUuid : workerMinorUserUuidSet) {
			processTaskStepWorkerVo.setUuid(userUuid);
			processTaskMapper.deleteProcessTaskStepWorker(processTaskStepWorkerVo);
			if(succeedSubtaskUserUuidSet.contains(userUuid)) {
				if(doingMinorUserUuidSet.contains(userUuid)) {
					/** 完成子任务 **/
					processTaskStepUserVo.setUserUuid(userUuid);
					processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
					processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
				}
			}else {
				if(doingMinorUserUuidSet.contains(userUuid)) {
					/** 取消子任务 **/
					processTaskStepUserVo.setUserUuid(userUuid);
					processTaskMapper.deleteProcessTaskStepUser(processTaskStepUserVo);
				}
			}
		}
		/** 向processtask_step_worker表中插入当前步骤的子任务处理人 **/	
		runningSubtaskUserUuidSet.removeAll(workerMinorUserUuidSet2);
		for(String userUuid : runningSubtaskUserUuidSet) {
			processTaskStepWorkerVo.setUuid(userUuid);
			processTaskMapper.insertProcessTaskStepWorker(processTaskStepWorkerVo);
			
			if(doneMinorUserUuidSet.contains(userUuid)) {
				/** 重做子任务 **/
				processTaskStepUserVo.setUserUuid(userUuid);
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
				processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
			}else if(!doingMinorUserUuidSet.contains(userUuid)) {
				/** 创建子任务 **/
				processTaskStepUserVo.setUserUuid(userUuid);
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
				processTaskMapper.insertProcessTaskStepUser(processTaskStepUserVo);
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
		ProcessTaskStepAction[] stepActions = {
				ProcessTaskStepAction.VIEW, 
				ProcessTaskStepAction.ABORT, 
				ProcessTaskStepAction.TRANSFER, 
				ProcessTaskStepAction.UPDATE, 
				ProcessTaskStepAction.URGE
		};
		for(ProcessTaskStepAction stepAction : stepActions) {
			authorityArray.add(new JSONObject() {{
				this.put("action", stepAction.getValue());
				this.put("text", stepAction.getText());
				this.put("acceptList", stepAction.getAcceptList());
				this.put("groupList", stepAction.getGroupList());
			}});
		}
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
		ProcessTaskStepAction[] stepButtons = {
				ProcessTaskStepAction.COMPLETE, 
				ProcessTaskStepAction.BACK, 
				ProcessTaskStepAction.COMMENT, 
				ProcessTaskStepAction.TRANSFER, 
				ProcessTaskStepAction.START,
				ProcessTaskStepAction.ABORT, 
				ProcessTaskStepAction.RECOVER
		};
		for(ProcessTaskStepAction stepButton : stepButtons) {
			customButtonArray.add(new JSONObject() {{
				this.put("name", stepButton.getValue());
				this.put("customText", stepButton.getText());
				this.put("value", "");
			}});
		}
		/** 子任务按钮映射列表 **/
		ProcessTaskStepAction[] subtaskButtons = {
				ProcessTaskStepAction.ABORTSUBTASK, 
				ProcessTaskStepAction.COMMENTSUBTASK, 
				ProcessTaskStepAction.COMPLETESUBTASK, 
				ProcessTaskStepAction.CREATESUBTASK, 
				ProcessTaskStepAction.REDOSUBTASK, 
				ProcessTaskStepAction.EDITSUBTASK
		};
		for(ProcessTaskStepAction subtaskButton : subtaskButtons) {
			customButtonArray.add(new JSONObject() {{
				this.put("name", subtaskButton.getValue());
				this.put("customText", subtaskButton.getText() + "(子任务)");
				this.put("value", "");
			}});
		}
		
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