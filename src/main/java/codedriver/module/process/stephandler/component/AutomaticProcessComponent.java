package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import codedriver.framework.util.TimeUtil;
import codedriver.module.process.notify.handler.ProcessNotifyPolicyHandler;
import codedriver.module.process.service.ProcessTaskService;

@Service
public class AutomaticProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(AutomaticProcessComponent.class);
	private static final ThreadLocal<List<RequestFirstThread>> AUTOMATIC_LIST = new ThreadLocal<>();
	
	@Autowired
	ProcessTaskService processTaskService;
	
	
	@Override
	public String getHandler() {
		return ProcessStepHandler.AUTOMATIC.getHandler();
	}
	
	@Override
	public String getType() {
		return ProcessStepHandler.AUTOMATIC.getType();
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
				this.put("icon", "tsfont-auto");
				this.put("shape", "L-rectangle-50%:R-rectangle-50%");
				this.put("width", 68);
				this.put("height", 40);
			}
		};
	}

	@Override
	public String getName() {
		return ProcessStepHandler.AUTOMATIC.getName();
	}

	@Override
	public int getSort() {
		return 0;
	}

	@Override
	protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
		//获取参数
		JSONObject automaticConfig = null;
		try {
			JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.setParamObj(stepConfigObj);
			if (MapUtils.isNotEmpty(stepConfigObj)) {
				automaticConfig = stepConfigObj.getJSONObject("automaticConfig");
			}
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}
		//初始化audit
		AutomaticConfigVo automaticConfigVo = new AutomaticConfigVo(automaticConfig);
		processTaskService.initProcessTaskStepData(currentProcessTaskStepVo,automaticConfigVo,null,"request");
		requestFirst(currentProcessTaskStepVo,automaticConfig);
		return 0;
	}
	
	
	/**
	 * automatic 第一次请求
	 * @param automaticConfig
	 */
	private void requestFirst(ProcessTaskStepVo currentProcessTaskStepVo,JSONObject automaticConfig) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			CachedThreadPool.execute(new RequestFirstThread(currentProcessTaskStepVo,automaticConfig));
		} else {
			List<RequestFirstThread> handlerList = AUTOMATIC_LIST.get();
			if (handlerList == null) {
				handlerList = new ArrayList<>();
				AUTOMATIC_LIST.set(handlerList);
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
					@Override
					public void afterCommit() {
						List<RequestFirstThread> handlerList = AUTOMATIC_LIST.get();
						for (RequestFirstThread automaticConfig : handlerList) {
							CachedThreadPool.execute(automaticConfig);
						}
					}

					@Override
					public void afterCompletion(int status) {
						AUTOMATIC_LIST.remove();
					}
				});
			}
			handlerList.add(new RequestFirstThread(currentProcessTaskStepVo,automaticConfig));
		}
	}
	
	private class RequestFirstThread extends CodeDriverThread {
		private JSONObject automaticConfig;
		private ProcessTaskStepVo currentProcessTaskStepVo;
		private RequestFirstThread(ProcessTaskStepVo currentProcessTaskStepVo,JSONObject automaticConfig) {
			this.automaticConfig = automaticConfig;
			this.currentProcessTaskStepVo = currentProcessTaskStepVo;
		}
		@Override
		protected void execute() {
			UserContext.init(SystemUser.SYSTEM.getConfig(), null, SystemUser.SYSTEM.getTimezone(), null, null);
			AutomaticConfigVo automaticConfigVo = new AutomaticConfigVo(automaticConfig);
			JSONObject timeWindowConfig = automaticConfigVo.getTimeWindowConfig();
			automaticConfigVo.setIsRequest(true);
			Integer isTimeToRun = null;
			//检验执行时间窗口
			if(timeWindowConfig != null) {
				isTimeToRun = TimeUtil.isInTime(timeWindowConfig.getString("startTime"),timeWindowConfig.getString("endTime"));
			}
			if(timeWindowConfig == null || isTimeToRun == 0) {
				processTaskService.runRequest(automaticConfigVo,currentProcessTaskStepVo);
			}else {//loadJob,定时执行第一次请求
				//初始化audit执行状态
				JSONObject audit = null;
				ProcessTaskStepDataVo data = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),currentProcessTaskStepVo.getId(),ProcessTaskStepDataType.AUTOMATIC.getValue()));
				JSONObject dataObject = data.getData();
				audit = dataObject.getJSONObject("requestAudit");
				audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
				processTaskService.initJob(automaticConfigVo,currentProcessTaskStepVo,dataObject);
				data.setData(dataObject.toJSONString());
				data.setFcu("system");
				processTaskStepDataMapper.replaceProcessTaskStepData(data);
			}
		}
		
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
        Long policyId = notifyPolicyConfig.getLong("policyId");
        if(policyId != null) {
        	processStepVo.setNotifyPolicyId(policyId);
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
		/** 收集引用的外部调用uuid **/
		JSONObject automaticConfig = stepConfigObj.getJSONObject("automaticConfig");
		if(MapUtils.isNotEmpty(automaticConfig)) {
			JSONObject requestConfig = automaticConfig.getJSONObject("requestConfig");
			if(MapUtils.isNotEmpty(requestConfig)) {
				String integrationUuid = requestConfig.getString("integrationUuid");
				if(StringUtils.isNotBlank(integrationUuid)) {
					processStepVo.getIntegrationUuidList().add(integrationUuid);
				}
			}
			JSONObject callbackConfig = automaticConfig.getJSONObject("callbackConfig");
			if(MapUtils.isNotEmpty(callbackConfig)) {
				JSONObject config = callbackConfig.getJSONObject("config");
				if(MapUtils.isNotEmpty(config)) {
					String integrationUuid = config.getString("integrationUuid");
					if(StringUtils.isNotBlank(integrationUuid)) {
						processStepVo.getIntegrationUuidList().add(integrationUuid);
					}
				}
			}
		}
	}

	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		/** 分配处理人 **/
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

		JSONObject workerPolicyConfig = null;
		try {
			JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.setParamObj(stepConfigObj);
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
				/** 设置当前步骤状态为处理中 **/
				if ("1".equals(autoStart)) {
					currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
				}		
			}
		}
		return 1;
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
	protected int myStart(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	public Boolean isAllowStart() {
		return null;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
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
		return 0;
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
	protected int mySaveDraft(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 1;
	}
	
	@Override
	public void updateProcessTaskStepUserAndWorker(List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) {
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
				//ProcessTaskStepAction.ABORT, 
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
		
		/** 按钮映射 **/
		JSONArray customButtonArray = new JSONArray();
		ProcessTaskStepAction[] stepButtons = {
				ProcessTaskStepAction.COMPLETE, 
				ProcessTaskStepAction.BACK, 
				//ProcessTaskStepAction.COMMENT, 
				ProcessTaskStepAction.TRANSFER, 
				ProcessTaskStepAction.START//,
				//ProcessTaskStepAction.ABORT, 
				//ProcessTaskStepAction.RECOVER
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