package codedriver.framework.process.stephandler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAuditMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.ProcessTaskAbortException;
import codedriver.framework.process.exception.ProcessTaskException;
import codedriver.framework.process.exception.ProcessTaskRuntimeException;
import codedriver.framework.process.timeoutpolicy.handler.ITimeoutPolicyHandler;
import codedriver.framework.process.timeoutpolicy.handler.TimeoutPolicyHandlerFactory;
import codedriver.framework.process.workerpolicy.handler.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.handler.WorkerPolicyHandlerFactory;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.module.process.dto.FormVersionVo;
import codedriver.module.process.dto.ProcessAttributeVo;
import codedriver.module.process.dto.ProcessStepRelVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskAttributeVo;
import codedriver.module.process.dto.ProcessTaskConvergeVo;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepAuditAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepRelVo;
import codedriver.module.process.dto.ProcessTaskStepTimeoutPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.dto.ProcessVo;

public abstract class ProcessStepHandlerBase implements IProcessStepHandler {
	static Logger logger = LoggerFactory.getLogger(ProcessStepHandlerBase.class);

	private static final ThreadLocal<List<ProcessStepThread>> PROCESS_STEP_RUNNABLES = new ThreadLocal<>();

	protected static ProcessMapper processMapper;
	protected static ProcessTaskMapper processTaskMapper;
	protected static ProcessTaskAuditMapper processTaskAuditMapper;
	protected static FormMapper formMapper;

	@Autowired
	public void setProcessMapper(ProcessMapper _processMapper) {
		processMapper = _processMapper;
	}

	@Autowired
	public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
		processTaskMapper = _processTaskMapper;
	}

	@Autowired
	public void setProcessTaskAuditMapper(ProcessTaskAuditMapper _processTaskAuditMapper) {
		processTaskAuditMapper = _processTaskAuditMapper;
	}

	@Autowired
	public void setFormMapper(FormMapper _formMapper) {
		formMapper = _formMapper;
	}

	@Override
	public final int active(ProcessTaskStepVo currentProcessTaskStepVo) {
		processTaskMapper.getProcessTaskLockById(currentProcessTaskStepVo.getProcessTaskId());
		// 判断前置步骤是不是都完成
		List<ProcessTaskStepVo> stepList = processTaskMapper.getProcessTaskStepByConvergeId(currentProcessTaskStepVo.getId());
		List<ProcessTaskStepVo> fromStepList = processTaskMapper.getFromProcessTaskStepByToId(currentProcessTaskStepVo.getId());
		List<ProcessTaskStepRelVo> fromStepRelList = processTaskMapper.getProcessTaskStepRelByToId(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByStepId(currentProcessTaskStepVo.getId());
		JSONObject stepConfigObj = null;
		if (StringUtils.isNotBlank(stepConfig)) {
			try {
				stepConfigObj = JSONObject.parseObject(stepConfig);
			} catch (Exception ex) {
				logger.error("转换步骤设置配置失败，" + ex.getMessage(), ex);
			}
		}
		if (currentProcessTaskStepVo.getFromProcessTaskStepId() != null) {
			processTaskMapper.updateProcessTaskStepConvergeIsCheck(1, currentProcessTaskStepVo.getId(), currentProcessTaskStepVo.getFromProcessTaskStepId());
		}
		boolean hasDoingStep = false;
		boolean hasUntriggerStep = false;// 流转未触发标记位，如果因为前置步骤流转未触发，则不需要修改任何状态，只需等待。
		// List<Long> checkStepIdList = new ArrayList<>();
		for (ProcessTaskStepVo step : stepList) {
			if (step.getIsActive().equals(1)) {
				hasDoingStep = true;
				break;
			} else {
				/** 检查路径是否已经流转过，解决冲突问题，当有fromStep的时候才检查 **/
				if (currentProcessTaskStepVo.getFromProcessTaskStepId() != null && fromStepList.contains(step) && step.getIsCheck().equals(0) && !step.getId().equals(currentProcessTaskStepVo.getFromProcessTaskStepId())) {
					hasUntriggerStep = true;
					for (ProcessTaskStepRelVo relVo : fromStepRelList) {
						if (relVo.getFromProcessTaskStepId().equals(step.getId()) && relVo.getIsHit().equals(-1)) {// 如果路径状态是不流转的，则跳过检查
							hasUntriggerStep = false;
						}
					}
					if (hasUntriggerStep) {
						break;
					}
				}
				/**
				 * 检查关键节点是否已经流转到后续任意一条路径，如果已经流转过，证明关键节点到当前节点是死路，则不需要等待，可以fire
				 **/
				List<ProcessTaskStepRelVo> toStepRelList = processTaskMapper.getProcessTaskStepRelByFromId(step.getId());
				boolean isFlowed = false;
				for (ProcessTaskStepRelVo toStepRel : toStepRelList) {
					if (toStepRel.getIsHit().equals(1)) {
						isFlowed = true;
					}
				}
				if (!isFlowed) {
					hasUntriggerStep = true;
				}
			}
		}

		/** 判断路径是否有激活 **/
		if (!hasDoingStep && !hasUntriggerStep) {
			fromStepList.retainAll(stepList);
			for (ProcessTaskStepVo fromStep : fromStepList) {
				for (ProcessTaskStepRelVo relVo : fromStepRelList) {
					if (relVo.getFromProcessTaskStepId().equals(fromStep.getId())) {
						if (relVo.getIsHit().equals(0)) {
							hasUntriggerStep = true;
						}
					}
				}
			}
		}

		/** 如果有处理中步骤，则什么都不做 **/
		if (hasDoingStep) {
			/** 如果无法流转下去，再次根据前置步骤的状态，决定是否更新作业流程状态 **/
			updateProcessTaskStatus(new ProcessTaskVo(currentProcessTaskStepVo.getProcessTaskId()));
			return 1;
		}
		if (hasUntriggerStep) {
			/** 如果无法流转下去，再次根据前置步骤的状态，决定是否更新作业流程状态 **/
			updateProcessTaskStatus(new ProcessTaskVo(currentProcessTaskStepVo.getProcessTaskId()));
			return 1;
		}

		try {
			myActive(currentProcessTaskStepVo);
			if (stepConfigObj != null) {
				List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
				if (stepConfigObj.containsKey("assignPolicy")) {
					/** 顺序分配处理人 **/
					if ("serial".equals(stepConfigObj.getString("assignPolicy"))) {
						List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicyByProcessTaskStepId(currentProcessTaskStepVo.getId());
						if (workerPolicyList != null && workerPolicyList.size() > 0) {
							for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
								IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
								if (workerPolicyHandler != null) {
									workerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
									if (workerList.size() > 0) {
										// 找到处理人，则退出
										break;
									}
								}
							}
						}
					} else if ("parallel".equals(stepConfigObj.getString("assignPolicy"))) {
						List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicyByProcessTaskStepId(currentProcessTaskStepVo.getId());
						if (workerPolicyList != null && workerPolicyList.size() > 0) {
							for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
								IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
								if (workerPolicyHandler != null) {
									List<ProcessTaskStepWorkerVo> tmpWorkerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
									// 去重并集
									tmpWorkerList.removeAll(workerList);
									workerList.addAll(tmpWorkerList);
								}
							}
						}
					}
				}
				if (workerList.size() > 0) {
					for (ProcessTaskStepWorkerVo workerVo : workerList) {
						processTaskMapper.insertProcessTaskStepWorker(workerVo);
					}
				}
				if (stepConfigObj.containsKey("isAutoStart") && stepConfigObj.getString("isAutoStart").equals("1") && workerList.size() == 1) {
					if (StringUtils.isNotBlank(workerList.get(0).getUserId())) {
						ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
						userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
						userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
						userVo.setUserId(workerList.get(0).getUserId());
						userVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
						processTaskMapper.insertProcessTaskStepUser(userVo);
					}
				}
			}
			/** 启动超时计时器 **/
			List<ProcessTaskStepTimeoutPolicyVo> timeoutPolicyList = processTaskMapper.getProcessTaskStepTimeoutPolicyByProcessTaskStepId(currentProcessTaskStepVo.getId());
			if (timeoutPolicyList != null && timeoutPolicyList.size() > 0) {
				boolean hasTimeout = false;
				for (ProcessTaskStepTimeoutPolicyVo timeoutPolicyVo : timeoutPolicyList) {
					if (hasTimeout) {
						break;
					}
					ITimeoutPolicyHandler timeoutPolicyHandler = TimeoutPolicyHandlerFactory.getHandler(timeoutPolicyVo.getPolicy());
					if (timeoutPolicyHandler != null) {
						hasTimeout = timeoutPolicyHandler.execute(timeoutPolicyVo, currentProcessTaskStepVo);
					}
				}
				if (hasTimeout && currentProcessTaskStepVo.getExpireTimeLong() != null) {
					/** TODO 结合工作日历计算最终超时时间，启动超时告警线程 **/
				}
			}

			currentProcessTaskStepVo.setIsActive(1);
			currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		} catch (ProcessTaskException e) {
			currentProcessTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
			currentProcessTaskStepVo.setError(e.getMessage());
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			currentProcessTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
			currentProcessTaskStepVo.setError(e.getMessage());
		} finally {
			processTaskMapper.updateProcessTaskStepStatus(currentProcessTaskStepVo);
		}
		return 1;
	}

	protected abstract int myActive(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException;

	@Override
	public final int handle(ProcessTaskStepVo currentProcessTaskStepVo) {
		processTaskMapper.getProcessTaskLockById(currentProcessTaskStepVo.getProcessTaskId());
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());

		if (!this.isAsync()) {// 同步模式
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(this.getType());

			try {
				myHandle(currentProcessTaskStepVo);
				if (currentProcessTaskStepVo.getIsCurrentUserDone()) {
					processTaskMapper.updateProcessTaskStepUserStatus(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId(), ProcessTaskStatus.SUCCEED.getValue()));
				}
				if (currentProcessTaskStepVo.getIsAllDone()) {
					doNext(new ProcessStepThread(currentProcessTaskStepVo) {
						@Override
						public void execute() {
							handler.complete(currentProcessTaskStepVo);
						}
					});
				}
			} catch (ProcessTaskAbortException ex) {// 终止状态
				if (currentProcessTaskStepVo.getIsCurrentUserDone()) {
					processTaskMapper.updateProcessTaskStepUserStatus(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId(), ProcessTaskStatus.ABORTED.getValue()));
				}
				if (currentProcessTaskStepVo.getIsAllDone()) {
					doNext(new ProcessStepThread(currentProcessTaskStepVo) {
						@Override
						public void execute() {
							handler.abort(currentProcessTaskStepVo);
						}
					});
				}
			} catch (ProcessTaskException ex) {// 异常
				logger.error(ex.getMessage(), ex);
				processTaskMapper.updateProcessTaskStepUserStatus(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId(), ProcessTaskStatus.ABORTED.getValue()));
				currentProcessTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
				if (ex.getMessage() != null && !ex.getMessage().equals("")) {
					currentProcessTaskStepVo.setError(ex.getMessage());
				} else {
					currentProcessTaskStepVo.setError(ExceptionUtils.getStackTrace(ex));
				}
			} finally {
				/** 告诉超时探测器步骤已经完成 **/
				// timeoutDetector.setIsDone(true);
				// timeoutDetector.interrupt();

			}
		} else {// 异步模式
			String type = this.getType();
			ProcessStepThread thread = new ProcessStepThread(currentProcessTaskStepVo) {
				@Override
				public void execute() {
					IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(type);
					try {
						// 这里不会有事务控制
						myHandle(currentProcessTaskStepVo);
						if (currentProcessTaskStepVo.getIsCurrentUserDone()) {
							processTaskMapper.updateProcessTaskStepUserStatus(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId(), ProcessTaskStatus.SUCCEED.getValue()));
						}
						if (currentProcessTaskStepVo.getIsAllDone()) {
							doNext(new ProcessStepThread(currentProcessTaskStepVo) {
								@Override
								public void execute() {
									handler.complete(currentProcessTaskStepVo);
								}
							});
						}
					} catch (ProcessTaskAbortException ex) {// 终止状态
						if (currentProcessTaskStepVo.getIsCurrentUserDone()) {
							processTaskMapper.updateProcessTaskStepUserStatus(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId(), ProcessTaskStatus.ABORTED.getValue()));
						}
						if (currentProcessTaskStepVo.getIsAllDone()) {
							doNext(new ProcessStepThread(currentProcessTaskStepVo) {
								@Override
								public void execute() {
									handler.abort(currentProcessTaskStepVo);
								}
							});
						}
					} catch (ProcessTaskException ex) {
						logger.error(ex.getMessage(), ex);
						processTaskMapper.updateProcessTaskStepUserStatus(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId(), ProcessTaskStatus.FAILED.getValue()));
						currentProcessTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
						if (ex.getMessage() != null && !ex.getMessage().equals("")) {
							currentProcessTaskStepVo.setError(ex.getMessage());
						} else {
							currentProcessTaskStepVo.setError(ExceptionUtils.getStackTrace(ex));
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						processTaskMapper.updateProcessTaskStepUserStatus(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId(), ProcessTaskStatus.FAILED.getValue()));
						currentProcessTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
						if (ex.getMessage() != null && !ex.getMessage().equals("")) {
							currentProcessTaskStepVo.setError(ex.getMessage());
						} else {
							currentProcessTaskStepVo.setError(ExceptionUtils.getStackTrace(ex));
						}
					} finally {
						/** 告诉超时探测器步骤已经完成 **/
						// if (this.getTimeoutDetector() != null) {
						// this.getTimeoutDetector().setIsDone(true);
						// this.getTimeoutDetector().interrupt();
						// }
						// 是否有步骤结果
						// if (flowJobStepVo.getHasResult()) {
						// flowJobMapper.insertFlowJobStepResult(flowJobStepVo.getId(),
						// flowJobStepVo.getResultPath());
						// }
					}
				}
			};
			/** 设置超时探测器到步骤处理线程 **/
			// thread.setTimeoutDetector(timeoutDetector);
			doNext(thread);
		}

		return 1;
	}

	protected abstract int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException, ProcessTaskAbortException;

	private int updateProcessTaskStatus(ProcessTaskVo processTaskVo) {
		return 1;
	}

	@Override
	public final int start(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub

		return myStart(currentProcessTaskStepVo);
	}

	protected abstract int myStart(ProcessTaskStepVo currentProcessTaskStepVo);

	@Override
	public final int complete(ProcessTaskStepVo currentProcessTaskStepVo) {
		processTaskMapper.getProcessTaskLockById(currentProcessTaskStepVo.getProcessTaskId());

		/** 校验用户是否合法 **/
		boolean hasRole = false;
		List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(currentProcessTaskStepVo.getId());
		for (ProcessTaskStepWorkerVo worker : workerList) {
			if (worker.getUserId().equals(UserContext.get().getUserId())) {
				hasRole = true;
				break;
			}
		}

		if (!hasRole) {
			throw new ProcessTaskRuntimeException("您不是当前步骤的处理人，没有权限处理");
		}

		/** 检查步骤是否激活，状态是否正常 **/
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		if (!processTaskStepVo.getIsActive().equals(1)) {
			throw new ProcessTaskRuntimeException("流程步骤未激活");
		}

		if (!processTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())) {
			throw new ProcessTaskRuntimeException("步骤状态不是进行中");
		}

		/** 写入当前步骤的自定义属性值 **/

		// ProcessTaskStepAttributeVo attributeVo = new
		// ProcessTaskStepAttributeVo();
		// attributeVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		// List<ProcessTaskStepAttributeVo> attributeList =
		// processTaskMapper.getProcessTaskStepAttributeByStepId(attributeVo);
		// currentProcessTaskStepVo.setAttributeList(attributeList);
		// JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		// if (attributeList != null && attributeList.size() > 0) {
		// JSONArray attributeObjList = null;
		// if (paramObj != null && paramObj.containsKey("attributeValueList") &&
		// paramObj.get("attributeValueList") instanceof JSONArray) {
		// attributeObjList = paramObj.getJSONArray("attributeValueList");
		// }
		// for (ProcessTaskStepAttributeVo attribute : attributeList) {
		// if (attribute.getIsEditable().equals(1)) {
		// if (attributeObjList != null && attributeObjList.size() > 0) {
		// for (int i = 0; i < attributeObjList.size(); i++) {
		// JSONObject attrObj = attributeObjList.getJSONObject(i);
		// if (attrObj.getString("uuid").equals(attribute.getAttributeUuid())) {
		// ProcessTaskAttributeDataVo attributeData = new
		// ProcessTaskAttributeDataVo();
		// attributeData.setData(attrObj.getString("value"));
		// attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		// attributeData.setAttributeUuid(attribute.getAttributeUuid());
		// processTaskMapper.replaceProcessTaskAttributeData(attributeData);
		// /** 清除原来的attribute value **/
		// processTaskMapper.deleteProcessTaskAttributeValueByProcessTaskIdAndAttributeUuid(currentProcessTaskStepVo.getProcessTaskId(),
		// attribute.getAttributeUuid());
		//
		// attribute.setData(attributeData);
		// IAttributeHandler handler =
		// AttributeHandlerFactory.getHandler(attribute.getHandler());
		// if (handler != null) {
		// List<String> valueList = handler.getValueList(attrObj.get("value"));
		// if (valueList != null && valueList.size() > 0) {
		// for (String value : valueList) {
		// if (StringUtils.isNotBlank(value)) {
		// ProcessTaskAttributeValueVo attributeValue = new
		// ProcessTaskAttributeValueVo();
		// attributeValue.setValue(value);
		// attributeValue.setAttributeUuid(attribute.getAttributeUuid());
		// attributeValue.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		// processTaskMapper.insertProcessTaskAttributeValue(attributeValue);
		// }
		// }
		// }
		// }
		// break;
		// }
		// }
		// }
		// if (attribute.getIsRequired().equals(1) && (attribute.getValueList()
		// == null || attribute.getValueList().size() == 0)) {
		// throw new ProcessTaskRuntimeException("缺少必要参数：" +
		// attribute.getLabel());
		// }
		// }
		//
		// }
		// }

		ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
		processTaskStepUserVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		processTaskStepUserVo.setUserId(UserContext.get().getUserId());
		try {
			myComplete(currentProcessTaskStepVo);

			processTaskStepUserVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());

			/** 如果已经完成，则清除worker表 **/
			if (currentProcessTaskStepVo.getIsAllDone()) {
				processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getId()));
			} else if (currentProcessTaskStepVo.getIsCurrentUserDone()) {
				processTaskMapper.deleteProcessTaskStepWorker(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getId(), UserContext.get().getUserId()));
			}
		} catch (ProcessTaskException ex) {
			processTaskStepUserVo.setStatus(ProcessTaskStatus.FAILED.getValue());
		} finally {
			processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
		}
		// TODO Auto-generated method stub
		return 0;
	}

	protected abstract int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException;

	@Override
	public final int retreat(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 1;
	}

	protected abstract int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException;

	@Override
	public final int abort(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 1;
	}

	protected abstract int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskAbortException;

	@Override
	public final int accept(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	public final int transfer(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	public final int save(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	protected abstract int mySave(ProcessTaskStepVo currentProcessTaskStepVo);

	@Override
	public final int init(ProcessTaskStepVo currentProcessTaskStepVo,ProcessTaskVo processTaskVo) {

		//ProcessTaskVo processTaskVo = new ProcessTaskVo();
		processTaskVo.setProcessUuid(currentProcessTaskStepVo.getProcessUuid());
		processTaskVo.setReporter(UserContext.get().getUserId());

		ProcessVo processVo = processMapper.getProcessByUuid(currentProcessTaskStepVo.getProcessUuid());
		List<ProcessStepVo> processStepList = processMapper.getProcessStepDetailByProcessUuid(currentProcessTaskStepVo.getProcessUuid());
		List<ProcessStepRelVo> processStepRelList = processMapper.getProcessStepRelByProcessUuid(currentProcessTaskStepVo.getProcessUuid());

		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		if (paramObj != null) {
			if (paramObj.containsKey("title")) {
				processTaskVo.setTitle(paramObj.getString("title"));
			}
			if (paramObj.containsKey("owner")) {
				processTaskVo.setOwner(paramObj.getString("owner"));
			} 			
			if (paramObj.containsKey("channelUuid")) {
				processTaskVo.setChannelUuid(paramObj.getString("channelUuid"));
			}
			if (paramObj.containsKey("processMd")) {
				processTaskVo.setProcessMd(paramObj.getString("processMd"));
			} else {
				processTaskVo.setOwner(UserContext.get().getUserId());
			}
			processTaskVo.setReporter(UserContext.get().getUserId());
		}
		if (StringUtils.isBlank(processTaskVo.getTitle())) {
			throw new ProcessTaskRuntimeException("缺少必要参数：标题");
		}
		processTaskVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
		processTaskMapper.insertProcessTask(processTaskVo);

		
		/** 写入关联通道 **/
		if(processTaskVo.getChannelUuid()!=null) {
			processTaskMapper.insertProcessTaskChannel(processTaskVo);
		}
		
		/** 写入流程属性 **/
		if (processVo.getAttributeList() != null && processVo.getAttributeList().size() > 0) {
			// List<ProcessTaskAttributeVo> processTaskAttributeList = new
			// ArrayList<>();
			for (ProcessAttributeVo attributeVo : processVo.getAttributeList()) {
				ProcessTaskAttributeVo processTaskAttributeVo = new ProcessTaskAttributeVo(attributeVo);
				processTaskAttributeVo.setProcessTaskId(processTaskVo.getId());
				processTaskMapper.insertProcessTaskAttribute(processTaskAttributeVo);
			}
		}

		/** 写入表单信息 **/
		if (StringUtils.isNotBlank(processVo.getFormUuid())) {
			FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processVo.getFormUuid());
			if (formVersionVo != null) {
				ProcessTaskFormVo processTaskFormVo = new ProcessTaskFormVo();
				processTaskFormVo.setFormContent(formVersionVo.getContent());
				processTaskFormVo.setProcessTaskId(processTaskVo.getId());
				processTaskFormVo.setFormUuid(formVersionVo.getFormUuid());
				processTaskFormVo.setFormName(formVersionVo.getFormName());
				processTaskMapper.insertProcessTaskForm(processTaskFormVo);
			}
		}

		Map<String, Long> stepIdMap = new HashMap<>();
		/** 写入所有步骤信息 **/
		if (processStepList != null && processStepList.size() > 0) {
			for (ProcessStepVo stepVo : processStepList) {
				ProcessTaskStepVo ptStepVo = new ProcessTaskStepVo(stepVo);
				ptStepVo.setStatus(ProcessTaskStatus.PENDING.getValue());
				ptStepVo.setProcessTaskId(processTaskVo.getId());
				processTaskMapper.insertProcessTaskStep(ptStepVo);
				if (StringUtils.isNotBlank(ptStepVo.getConfig())) {
					processTaskMapper.insertProcessTaskStepConfig(ptStepVo.getId(), ptStepVo.getConfig());
				}
				stepIdMap.put(ptStepVo.getProcessStepUuid(), ptStepVo.getId());

				/** 写入步骤自定义属性 **/
				if (ptStepVo.getAttributeList() != null && ptStepVo.getAttributeList().size() > 0) {
					for (ProcessTaskStepAttributeVo processTaskStepAttributeVo : ptStepVo.getAttributeList()) {
						processTaskStepAttributeVo.setProcessTaskId(processTaskVo.getId());
						processTaskStepAttributeVo.setProcessTaskStepId(ptStepVo.getId());
						processTaskMapper.insertProcessTaskStepAttribute(processTaskStepAttributeVo);
					}
				}

				/** 写入步骤表单属性 **/
				if (ptStepVo.getFormAttributeList() != null && ptStepVo.getFormAttributeList().size() > 0) {
					for (ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : ptStepVo.getFormAttributeList()) {
						processTaskStepFormAttributeVo.setProcessTaskId(processTaskVo.getId());
						processTaskStepFormAttributeVo.setProcessTaskStepId(ptStepVo.getId());
						processTaskMapper.insertProcessTaskStepFormAttribute(processTaskStepFormAttributeVo);
					}
				}

				/** 写入用户分配策略信息 **/
				if (ptStepVo.getWorkerPolicyList() != null && ptStepVo.getWorkerPolicyList().size() > 0) {
					for (ProcessTaskStepWorkerPolicyVo policyVo : ptStepVo.getWorkerPolicyList()) {
						policyVo.setProcessTaskId(processTaskVo.getId());
						policyVo.setProcessTaskStepId(ptStepVo.getId());
						processTaskMapper.insertProcessTaskStepWorkerPolicy(policyVo);
					}
				}

				/** 写入超时策略信息 **/
				if (ptStepVo.getTimeoutPolicyList() != null && ptStepVo.getTimeoutPolicyList().size() > 0) {
					for (ProcessTaskStepTimeoutPolicyVo policyVo : ptStepVo.getTimeoutPolicyList()) {
						policyVo.setProcessTaskId(processTaskVo.getId());
						policyVo.setProcessTaskStepId(ptStepVo.getId());
						processTaskMapper.insertProcessTaskStepTimeoutPolicy(policyVo);
					}
				}

				/** 找到开始节点 **/
				if (stepVo.getType().equals(ProcessStepType.START.getValue())) {
					currentProcessTaskStepVo = ptStepVo;
					if (paramObj.containsKey("step") && paramObj.get("step") instanceof JSONObject) {
						currentProcessTaskStepVo.setParamObj(paramObj.getJSONObject("step"));
					}
					currentProcessTaskStepVo.setProcessTaskId(processTaskVo.getId());
				}
			}
		}

		/** 写入关系信息 **/
		if (processStepRelList != null && processStepRelList.size() > 0) {
			for (ProcessStepRelVo relVo : processStepRelList) {
				ProcessTaskStepRelVo processTaskStepRelVo = new ProcessTaskStepRelVo(relVo);
				processTaskStepRelVo.setProcessTaskId(processTaskVo.getId());
				processTaskStepRelVo.setFromProcessTaskStepId(stepIdMap.get(processTaskStepRelVo.getFromProcessStepUuid()));
				processTaskStepRelVo.setToProcessTaskStepId(stepIdMap.get(processTaskStepRelVo.getToProcessStepUuid()));
				/** 同时找到from step id 和to step id 时才写入，其他数据舍弃 **/
				if (processTaskStepRelVo.getFromProcessTaskStepId() != null && processTaskStepRelVo.getToProcessTaskStepId() != null) {
					processTaskMapper.insertProcessTaskStepRel(processTaskStepRelVo);
				}
			}
		}

		try {
			myInit(currentProcessTaskStepVo);
			currentProcessTaskStepVo.setIsActive(0);
			currentProcessTaskStepVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
		} catch (ProcessTaskException ex) {
			logger.error(ex.getMessage(), ex);
			currentProcessTaskStepVo.setIsActive(1);
			currentProcessTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
			if (StringUtils.isBlank(ex.getMessage())) {
				currentProcessTaskStepVo.setError(ExceptionUtils.getStackTrace(ex));
			} else {
				currentProcessTaskStepVo.setError(ex.getMessage());
			}
			/** 加入上报人为处理人，让处理人可以处理异常 **/
			ProcessTaskStepWorkerVo processTaskStepWorkerVo = new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), processTaskVo.getReporter());
			processTaskMapper.insertProcessTaskStepWorker(processTaskStepWorkerVo);
		} finally {
			processTaskMapper.updateProcessTaskStepStatus(currentProcessTaskStepVo);
		}

		/** 处理历史记录 **/
		saveProcessTaskStepAudit(currentProcessTaskStepVo, ProcessTaskStepAction.INIT);

		/** 流转到下一步 **/
		List<ProcessTaskStepVo> nextStepList = getNext(currentProcessTaskStepVo);
		for (ProcessTaskStepVo nextStep : nextStepList) {
			IProcessStepHandler nextStepHandler = ProcessStepHandlerFactory.getHandler(nextStep.getHandler());
			doNext(new ProcessStepThread(nextStep) {
				@Override
				public void execute() {
					nextStepHandler.active(nextStep);
				}

			});
		}
		return 0;
	}

	private int saveProcessTaskStepAudit(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskStepAction action) {
		/* 尝试补充内容信息 */
		if (currentProcessTaskStepVo.getContentId() == null) {
			currentProcessTaskStepVo.setContentId(processTaskMapper.getProcessTaskStepContentIdByProcessTaskStepId(currentProcessTaskStepVo.getId()));
		}

		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo(currentProcessTaskStepVo);
		processTaskStepAuditVo.setAction(action.getValue());
		processTaskAuditMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);

		List<ProcessTaskAttributeDataVo> attrList = processTaskMapper.getProcessTaskStepAttributeDataByStepId(currentProcessTaskStepVo.getId());
		for (ProcessTaskAttributeDataVo attributeData : attrList) {
			ProcessTaskStepAuditAttributeDataVo processTaskStepAuditAttributeDataVo = new ProcessTaskStepAuditAttributeDataVo(attributeData);
			processTaskStepAuditAttributeDataVo.setAuditId(processTaskStepAuditVo.getId());
			processTaskMapper.insertProcessTaskStepAuditAttributeData(processTaskStepAuditAttributeDataVo);
		}

		List<ProcessTaskAttributeDataVo> formAttrList = processTaskMapper.getProcessTaskStepFormAttributeDataByStepId(currentProcessTaskStepVo.getId());
		for (ProcessTaskAttributeDataVo attributeData : formAttrList) {
			ProcessTaskStepAuditAttributeDataVo processTaskStepAuditAttributeDataVo = new ProcessTaskStepAuditAttributeDataVo(attributeData);
			processTaskStepAuditAttributeDataVo.setAuditId(processTaskStepAuditVo.getId());
			processTaskMapper.insertProcessTaskStepAuditAttributeData(processTaskStepAuditAttributeDataVo);
		}

		return 1;
	}

	protected abstract int myInit(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException;

	@Override
	public final List<ProcessTaskStepVo> getNext(ProcessTaskStepVo currentProcessTaskStepVo) {
		// ProcessTaskStepVo processTaskStepVo =
		// processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		List<ProcessTaskStepRelVo> relList = processTaskMapper.getProcessTaskStepRelByFromId(currentProcessTaskStepVo.getId());
		// 重置所有关系状态为-1
		for (ProcessTaskStepRelVo rel : relList) {
			processTaskMapper.updateProcessTaskStepRelIsHit(rel.getFromProcessTaskStepId(), rel.getToProcessTaskStepId(), -1);
		}
		currentProcessTaskStepVo.setRelList(relList);

		List<ProcessTaskStepVo> nextStepList = null;
		try {
			nextStepList = myGetNext(currentProcessTaskStepVo);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			if (ex.getMessage() != null && !ex.getMessage().equals("")) {
				currentProcessTaskStepVo.appendError(ex.getMessage());
			} else {
				currentProcessTaskStepVo.appendError(ExceptionUtils.getStackTrace(ex));
			}
			currentProcessTaskStepVo.setStatus(ProcessTaskStatus.FAILED.getValue());
		}
		if (nextStepList != null && nextStepList.size() > 0) {
			Iterator<ProcessTaskStepVo> stepIter = nextStepList.iterator();
			Set<Long> checkSet = new HashSet<Long>();
			while (stepIter.hasNext()) {// 去掉重复的路径
				ProcessTaskStepVo step = stepIter.next();
				if (!checkSet.contains(step.getId())) {
					checkSet.add(step.getId());
				} else {
					stepIter.remove();
				}
			}
			for (ProcessTaskStepVo step : nextStepList) {
				processTaskMapper.updateProcessTaskStepRelIsHit(currentProcessTaskStepVo.getId(), step.getId(), 1);
				resetConvergeInfo(step);
			}
		}
		return nextStepList;
	}

	private void resetConvergeInfo(ProcessTaskStepVo nextStepVo) {
		processTaskMapper.getProcessTaskLockById(nextStepVo.getProcessTaskId());
		List<ProcessTaskStepVo> stepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(nextStepVo.getProcessTaskId(), ProcessStepType.END.getValue());
		ProcessTaskStepVo endStepVo = null;
		if (stepList.size() == 1) {
			endStepVo = stepList.get(0);
		}
		// 重新插入汇聚数据
		List<List<Long>> routeList = new ArrayList<>();
		List<Long> routeStepList = new ArrayList<>();
		routeList.add(routeStepList);

		getAllRouteList(nextStepVo.getId(), routeList, routeStepList, endStepVo);
		// 如果最后一个步骤不是结束节点的路由全部删掉，因为这是回环路由
		Iterator<List<Long>> routeStepIt = routeList.iterator();
		List<Long> convergeIdList = new ArrayList<>();
		while (routeStepIt.hasNext()) {
			List<Long> rsList = routeStepIt.next();
			if (!rsList.get(rsList.size() - 1).equals(endStepVo.getId())) {
				routeStepIt.remove();
			} else {
				for (Long cid : rsList) {
					if (!convergeIdList.contains(cid) && !cid.equals(nextStepVo.getId())) {
						convergeIdList.add(cid);
					}
				}
			}
		}
		if (convergeIdList.size() > 0) {
			for (Long convergeId : convergeIdList) {
				ProcessTaskConvergeVo processTaskStepConvergeVo = new ProcessTaskConvergeVo(nextStepVo.getProcessTaskId(), nextStepVo.getId(), convergeId);
				if (processTaskMapper.checkProcessTaskConvergeIsExists(processTaskStepConvergeVo) == 0) {
					processTaskMapper.insertProcessTaskConverge(processTaskStepConvergeVo);
				}
			}
		}
	}

	private void getAllRouteList(Long processTaskStepId, List<List<Long>> routeList, List<Long> routeStepList, ProcessTaskStepVo endStepVo) {
		if (!routeStepList.contains(processTaskStepId)) {
			routeStepList.add(processTaskStepId);
			List<Long> tmpRouteStepList = new ArrayList<>(routeStepList);
			if (!processTaskStepId.equals(endStepVo.getId())) {
				List<ProcessTaskStepVo> toProcessTaskStepList = processTaskMapper.getToProcessTaskStepByFromId(processTaskStepId);
				for (int i = 0; i < toProcessTaskStepList.size(); i++) {
					ProcessTaskStepVo toProcessTaskStepVo = toProcessTaskStepList.get(i);
					if (i > 0) {
						List<Long> newRouteStepList = new ArrayList<>(tmpRouteStepList);
						routeList.add(newRouteStepList);
						getAllRouteList(toProcessTaskStepVo.getId(), routeList, newRouteStepList, endStepVo);
					} else {
						getAllRouteList(toProcessTaskStepVo.getId(), routeList, routeStepList, endStepVo);
					}
				}
			}
		}
	}

	protected abstract List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo);

	protected synchronized void doNext(ProcessStepThread thread) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			Thread t = new Thread(thread);
			if (thread.getProcessTaskStepVo() != null) {
				t.setName("PROCESSTASK-STEP-HANDLER-" + thread.getProcessTaskStepVo().getId());
			} else {
				t.setName("PROCESSTASK-STEP-HANDLERS");
			}
			t.setDaemon(true);
			t.start();
		} else {
			List<ProcessStepThread> runableActionList = PROCESS_STEP_RUNNABLES.get();
			if (runableActionList == null) {
				runableActionList = new ArrayList<>();
				PROCESS_STEP_RUNNABLES.set(runableActionList);
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
					@Override
					public void afterCommit() {
						List<ProcessStepThread> runableActionList = PROCESS_STEP_RUNNABLES.get();
						for (int i = 0; i < runableActionList.size(); i++) {
							ProcessStepThread runnable = runableActionList.get(i);
							Thread t = new Thread(runnable);
							if (runnable.getProcessTaskStepVo() != null) {
								t.setName("PROCESSTASK-STEP-HANDLER-" + runnable.getProcessTaskStepVo().getId());
							} else {
								t.setName("PROCESSTASK-STEP-HANDLERS");
							}
							t.setDaemon(true);
							t.start();
						}
					}

					@Override
					public void afterCompletion(int status) {
						PROCESS_STEP_RUNNABLES.remove();
					}
				});
			}
			runableActionList.add(thread);
		}
	}

}
