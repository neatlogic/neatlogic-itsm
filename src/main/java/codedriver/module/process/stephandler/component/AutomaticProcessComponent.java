package codedriver.module.process.stephandler.component;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.exception.worktime.WorktimeConfigIllegalException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;

@Service
public class AutomaticProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(AutomaticProcessComponent.class);
	private static final ThreadLocal<List<RequestFirstThread>> AUTOMATIC_LIST = new ThreadLocal<>();
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
				this.put("icon", "ts-shunt");
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
		requestFirst(automaticConfig);
		return 0;
	}
	
	/**
	 * automatic 第一次请求
	 * @param automaticConfig
	 */
	private void requestFirst(JSONObject automaticConfig) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			CachedThreadPool.execute(new RequestFirstThread(automaticConfig));
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
			handlerList.add(new RequestFirstThread(automaticConfig));
		}
	}
	
	private class RequestFirstThread extends CodeDriverThread {
		private JSONObject automaticConfig;
		private RequestFirstThread(JSONObject automaticConfig) {
			this.automaticConfig = automaticConfig;
		}
		@Override
		protected void execute() {
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
			JSONObject baseConfig = automaticConfig.getJSONObject("base");
			String baseIntegrationUuid = baseConfig.getString("integrationUuid");
			JSONObject runWindowJson = baseConfig.getJSONObject("runWindow");
			JSONObject baseParamsJson = baseConfig.getJSONObject("params");
			JSONObject baseSucessFlagJson = baseConfig.getJSONObject("sucessFlag");
			String baseFailPolicy = baseConfig.getString("failPolicy");
			String baseShowTemplate = baseConfig.getString("baseShowTemplate");
			//检验执行时间窗口
			
			try {
				timeFormatter.parse("");
			}catch(DateTimeException e) {
				throw new WorktimeConfigIllegalException("");
			}
			
		}
		
	}
	
	private Boolean isTimeToRun(String startTime,String endTime) {
		Boolean isTimeToRun = true;
		Calendar nowData = Calendar.getInstance();
		nowData.set(Calendar.HOUR_OF_DAY, 12);
		nowData.set(Calendar.MINUTE, 12);
		nowData.set(Calendar.SECOND, 0);
		
		return isTimeToRun;
	}
	
	/**
	 * 返回result对应key的值
	 * @param key
	 * @param returnObj
	 * @return
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	private String getReturnValue(String key, JSONObject returnObj) throws ScriptException, NoSuchMethodException {
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("nashorn");
		StringBuilder scriptBuilder = new StringBuilder();
		scriptBuilder.append("function run(){");
		scriptBuilder.append("return json." + key + ";\n");
		scriptBuilder.append("}");

		se.put("json", returnObj);
		se.eval(scriptBuilder.toString());
		Invocable invocableEngine = (Invocable) se;
		Object callbackvalue = invocableEngine.invokeFunction("run");
		if (callbackvalue != null) {
			return callbackvalue.toString();
		}
		return "";
	}
	
	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
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
		return 1;
	}

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		/** 组装通知模板 **/
//		JSONArray notifyList = stepConfigObj.getJSONArray("notifyList");
//		if (!CollectionUtils.isEmpty(notifyList)) {
//			List<String> templateUuidList = new ArrayList<>();
//			for (int j = 0; j < notifyList.size(); j++) {
//				JSONObject notifyObj = notifyList.getJSONObject(j);
//				String template = notifyObj.getString("template");
//				if (StringUtils.isNotBlank(template)) {
//					templateUuidList.add(template);
//				}
//			}
//			processStepVo.setTemplateUuidList(templateUuidList);
//		}
		/** 组装通知策略id **/
		JSONObject notifyPolicyConfig = stepConfigObj.getJSONObject("notifyPolicyConfig");
        Long policyId = notifyPolicyConfig.getLong("policyId");
        if(policyId != null) {
        	processStepVo.setNotifyPolicyId(policyId);
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
			workerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(), oldUserVo.getUserUuid()));
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
		return 1;
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
	
}