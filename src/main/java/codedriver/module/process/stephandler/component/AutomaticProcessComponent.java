package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import codedriver.framework.util.TimeUtil;
import codedriver.module.process.service.ProcessTaskService;

@Service
public class AutomaticProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(AutomaticProcessComponent.class);
	private static final ThreadLocal<List<RequestFirstThread>> AUTOMATIC_LIST = new ThreadLocal<>();
	
	@Autowired
	private ProcessTaskStepDataMapper processTaskStepDataMapper;
	@Autowired
	ProcessTaskService processTaskService;
	
	
	@Override
	public String getHandler() {
		return ProcessStepHandlerType.AUTOMATIC.getHandler();
	}
	
	@Override
	public String getType() {
		return ProcessStepHandlerType.AUTOMATIC.getType();
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
		return ProcessStepHandlerType.AUTOMATIC.getName();
	}

	@Override
	public int getSort() {
		return 8;
	}

	@Override
	protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
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
				isTimeToRun = TimeUtil.isInTimeWindow(timeWindowConfig.getString("startTime"),timeWindowConfig.getString("endTime"));
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
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
		return 1;
	}

	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
		/** 获取步骤配置信息 **/
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

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
	protected int myCompleteAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
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
    protected int myPause(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }
	
}