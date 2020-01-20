package codedriver.framework.process.stephandler.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAuditMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTimeAuditMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.ProcessTaskStepUserType;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskSlaVo;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepTimeAuditVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.dto.WorktimeRangeVo;
import codedriver.module.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.module.process.formattribute.core.IFormAttributeHandler;

public abstract class ProcessStepHandlerUtilBase {
	static Logger logger = LoggerFactory.getLogger(ProcessStepHandlerUtilBase.class);

	private static final ThreadLocal<List<AuditHandler>> AUDIT_HANDLERS = new ThreadLocal<>();
	private static final ThreadLocal<List<SlaHandler>> SLA_HANDLERS = new ThreadLocal<>();
	protected static ProcessMapper processMapper;
	protected static ProcessTaskMapper processTaskMapper;
	protected static ProcessTaskAuditMapper processTaskAuditMapper;
	protected static FormMapper formMapper;
	protected static UserMapper userMapper;
	protected static ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;
	private static WorktimeMapper worktimeMapper;
	private static ChannelMapper channelMapper;

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

	@Autowired
	public void setUserMapper(UserMapper _userMapper) {
		userMapper = _userMapper;
	}

	@Autowired
	public void setProcessTaskStepTimeAuditMapper(ProcessTaskStepTimeAuditMapper _processTaskStepTimeAuditMapper) {
		processTaskStepTimeAuditMapper = _processTaskStepTimeAuditMapper;
	}

	@Autowired
	public void setWorktimeMapper(WorktimeMapper _worktimeMapper) {
		worktimeMapper = _worktimeMapper;
	}

	@Autowired
	public void setChannelMapper(ChannelMapper _channelMapper) {
		channelMapper = _channelMapper;
	}

	public static void main(String[] atr) {
		ScriptEngineManager sem = new ScriptEngineManager();

		ScriptEngine se = sem.getEngineByName("nashorn");
		JSONObject paramObj = new JSONObject();
		paramObj.put("form.name", "chenqw");
		paramObj.put("form.age", "37");
		se.put("json", paramObj);
		String script = "json['form.name'] == 'chen2qw' && json['form.age'] == '37'";
		try {
			System.out.println(Boolean.parseBoolean(se.eval(script).toString()));
		} catch (ScriptException e) {
			logger.error(e.getMessage(), e);
		}

	}

	protected static class SlaHandler extends CodeDriverThread {
		private ProcessTaskStepVo currentProcessTaskStepVo;

		public SlaHandler(ProcessTaskStepVo _currentProcessTaskStepVo) {
			currentProcessTaskStepVo = _currentProcessTaskStepVo;
			if (_currentProcessTaskStepVo != null) {
				this.setThreadName("PROCESSTASK-SLA-" + _currentProcessTaskStepVo.getId());
			}
		}

		private static long calculateExpireTime(long activeTime, long timeLimit, String worktimeUuid) {
			if (worktimeMapper.checkWorktimeIsExists(worktimeUuid) == 0) {
				throw new WorktimeNotFoundException(worktimeUuid);
			}
			if (timeLimit <= 0) {
				return activeTime;
			}
			WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
			WorktimeRangeVo recentWorktimeRange = null;
			long startTime = 0;
			long endTime = 0;
			long duration = 0;
			while (true) {
				worktimeRangeVo.setWorktimeUuid(worktimeUuid);
				worktimeRangeVo.setStartTime(activeTime);
				recentWorktimeRange = worktimeMapper.getRecentWorktimeRange(worktimeRangeVo);
				if (recentWorktimeRange == null) {
					return activeTime;
				}
				startTime = recentWorktimeRange.getStartTime();
				endTime = recentWorktimeRange.getEndTime();
				if (startTime > activeTime) {
					activeTime = startTime;
				}
				duration = endTime - activeTime;
				if (duration >= timeLimit) {
					return activeTime + timeLimit;
				} else {
					timeLimit -= duration;
					activeTime = endTime;
				}
			}
		}

		private static long getTimeCost(List<ProcessTaskStepTimeAuditVo> processTaskStepTimeAuditList, String worktimeUuid) {
			List<Map<String, Long>> timeList = new ArrayList<>();
			for (ProcessTaskStepTimeAuditVo auditVo : processTaskStepTimeAuditList) {
				Long startTime = null, endTime = null;
				if (auditVo.getActiveTimeLong() != null) {
					startTime = auditVo.getActiveTimeLong();
				}
				if (auditVo.getSuccessTimeLong() != null) {
					endTime = auditVo.getSuccessTimeLong();
				} else if (auditVo.getFailedTimeLong() != null) {
					endTime = auditVo.getFailedTimeLong();
				} else if (auditVo.getAbortTimeLong() != null) {
					endTime = auditVo.getFailedTimeLong();
				} else if (auditVo.getBackTimeLong() != null) {
					endTime = auditVo.getBackTimeLong();
				}
				if (startTime != null && endTime != null) {
					Map<String, Long> stimeMap = new HashMap<>();
					stimeMap.put("s", startTime);
					timeList.add(stimeMap);
					Map<String, Long> etimeMap = new HashMap<>();
					etimeMap.put("e", endTime);
					timeList.add(etimeMap);
				}
			}
			timeList.sort(new Comparator<Map<String, Long>>() {
				@Override
				public int compare(Map<String, Long> o1, Map<String, Long> o2) {
					Long t1 = null, t2 = null;
					if (o1.containsKey("s")) {
						t1 = o1.get("s");
					} else if (o1.containsKey("e")) {
						t1 = o1.get("e");
					}

					if (o2.containsKey("s")) {
						t2 = o2.get("s");
					} else if (o2.containsKey("e")) {
						t2 = o2.get("e");
					}
					return t1.compareTo(t2);
				}
			});
			Stack<Long> timeStack = new Stack<>();
			List<Map<String, Long>> newTimeList = new ArrayList<>();
			for (Map<String, Long> timeMap : timeList) {
				if (timeMap.containsKey("s")) {
					timeStack.push(timeMap.get("s"));
				} else if (timeMap.containsKey("e")) {
					if (!timeStack.isEmpty()) {
						Long currentStartTimeLong = timeStack.pop();
						if (timeStack.isEmpty()) {// 栈被清空时计算时间段
							Map<String, Long> newTimeMap = new HashMap<>();
							newTimeMap.put("s", currentStartTimeLong);
							newTimeMap.put("e", timeMap.get("e"));
							newTimeList.add(newTimeMap);
						}
					}
				}
			}

			long sum = 0;
			for (Map<String, Long> timeMap : newTimeList) {
				sum += worktimeMapper.calculateCostTime(worktimeUuid, timeMap.get("s"), timeMap.get("e"));
			}
			return sum;
		}

		private static long getRealTimeCost(List<ProcessTaskStepTimeAuditVo> processTaskStepTimeAuditList) {
			int timeCost = 0;
			if (processTaskStepTimeAuditList != null && processTaskStepTimeAuditList.size() > 0) {
				List<Map<String, Long>> timeZoneList = new ArrayList<>();
				for (ProcessTaskStepTimeAuditVo auditVo : processTaskStepTimeAuditList) {
					Long startTime = null, endTime = null;
					if (auditVo.getActiveTimeLong() != null) {
						startTime = auditVo.getActiveTimeLong();
					}
					if (auditVo.getSuccessTimeLong() != null) {
						endTime = auditVo.getSuccessTimeLong();
					} else if (auditVo.getFailedTimeLong() != null) {
						endTime = auditVo.getFailedTimeLong();
					} else if (auditVo.getAbortTimeLong() != null) {
						endTime = auditVo.getFailedTimeLong();
					} else if (auditVo.getBackTimeLong() != null) {
						endTime = auditVo.getBackTimeLong();
					}
					if (startTime != null && endTime != null) {
						Map<String, Long> smap = new HashMap<>();
						smap.put("s", startTime);
						timeZoneList.add(smap);
						Map<String, Long> emap = new HashMap<>();
						emap.put("e", endTime);
						timeZoneList.add(emap);
					}
				}
				timeZoneList.sort(new Comparator<Map<String, Long>>() {
					@Override
					public int compare(Map<String, Long> o1, Map<String, Long> o2) {
						Long t1 = null, t2 = null;
						if (o1.containsKey("s")) {
							t1 = o1.get("s");
						} else if (o1.containsKey("e")) {
							t1 = o1.get("e");
						}

						if (o2.containsKey("s")) {
							t2 = o2.get("s");
						} else if (o2.containsKey("e")) {
							t2 = o2.get("e");
						}
						return t1.compareTo(t2);
					}
				});

				Stack<Long> timeStack = new Stack<>();
				for (Map<String, Long> timeMap : timeZoneList) {
					if (timeMap.containsKey("s")) {
						timeStack.push(timeMap.get("s"));
					} else if (timeMap.containsKey("e")) {
						if (!timeStack.isEmpty()) {
							Long currentStartTimeLong = timeStack.pop();
							if (timeStack.isEmpty()) {
								Long tmp = timeMap.get("e") - currentStartTimeLong;
								timeCost += tmp.intValue();
							}
						}
					}
				}
			}
			return timeCost;
		}

		protected static void calculate(ProcessTaskStepVo currentProcessTaskStepVo) {
			if (!TransactionSynchronizationManager.isSynchronizationActive()) {
				CachedThreadPool.execute(new SlaHandler(currentProcessTaskStepVo));
			} else {
				List<SlaHandler> handlerList = SLA_HANDLERS.get();
				if (handlerList == null) {
					handlerList = new ArrayList<>();
					SLA_HANDLERS.set(handlerList);
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						@Override
						public void afterCommit() {
							List<SlaHandler> handlerList = SLA_HANDLERS.get();
							for (SlaHandler handler : handlerList) {
								CachedThreadPool.execute(handler);
							}
						}

						@Override
						public void afterCompletion(int status) {
							SLA_HANDLERS.remove();
						}
					});
				}
				handlerList.add(new SlaHandler(currentProcessTaskStepVo));
			}
		}

		private static final ScriptEngineManager sem = new ScriptEngineManager();

		private boolean validateRule(JSONArray ruleList, String connectionType) {
			if (ruleList != null && ruleList.size() > 0) {
				ScriptEngine se = sem.getEngineByName("nashorn");

				JSONObject paramObj = new JSONObject();
				List<ProcessTaskFormAttributeDataVo> formAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				String script = "";
				for (int i = 0; i < ruleList.size(); i++) {
					JSONObject ruleObj = ruleList.getJSONObject(i);
					String key = ruleObj.getString("key");
					String value = null;
					String compareValue = ruleObj.getString("value");
					String expression = ruleObj.getString("expression");
					if (key.startsWith("form.")) {
						for (ProcessTaskFormAttributeDataVo attributeData : formAttributeDataList) {
							if (attributeData.getAttributeUuid().equals(key.substring(5))) {
								IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeData.getType());
								if (handler != null) {
									value = handler.getValue(attributeData);
								}
								break;
							}
						}
					}
					if (StringUtils.isNotBlank(value)) {
						paramObj.put(key, value);
					} else {
						paramObj.put(key, "");
					}
					if (StringUtils.isNotBlank(script)) {
						if (connectionType.equalsIgnoreCase("and")) {
							script += " && ";
						} else {
							script += " || ";
						}
					}
					script += "json['" + key + "'] " + expression + " '" + compareValue + "'";
				}
				se.put("json", paramObj);
				try {
					return Boolean.parseBoolean(se.eval(script).toString());
				} catch (ScriptException e) {
					logger.error(e.getMessage(), e);
					return false;
				}
			}
			return false;
		}

		private static long getRealtime(int time, String unit) {
			if ("hour".equals(unit)) {
				return time * 60 * 60 * 1000;
			} else if ("day".equals(unit)) {
				return time * 24 * 60 * 60 * 1000;
			} else {
				return time * 60 * 1000;
			}
		}

		@Override
		protected void execute() {
			List<ProcessTaskSlaVo> slaList = processTaskMapper.getProcessTaskSlaByProcessTaskStepId(currentProcessTaskStepVo.getId());
			if (slaList != null && slaList.size() > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long now = System.currentTimeMillis();
				String worktimeUuid = null;
				ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
				if (processTaskVo != null) {
					if (StringUtils.isNotBlank(processTaskVo.getChannelUuid())) {
						ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
						if (channelVo != null && StringUtils.isNotBlank(channelVo.getWorktimeUuid())) {
							worktimeUuid = channelVo.getWorktimeUuid();
						}
					}
				}
				for (ProcessTaskSlaVo slaVo : slaList) {
					/** 如果没有超时时间，证明第一次进入SLA标签范围，开始计算超时时间 **/
					if (slaVo.getTimeSum() == null) {
						if (slaVo.getRuleObj() != null) {
							// 这里要通过rule计算出来
							JSONArray policyList = slaVo.getRuleObj().getJSONArray("policyList");
							if (policyList != null && policyList.size() > 0) {
								POLICY: for (int i = 0; i < policyList.size(); i++) {
									JSONObject policyObj = policyList.getJSONObject(i);
									String connectionType = policyObj.getString("connectType");
									int enablePriority = policyObj.getIntValue("enablePriority");
									int time = policyObj.getIntValue("time");
									String unit = policyObj.getString("unit");
									JSONArray priorityList = policyObj.getJSONArray("priorityList");
									JSONArray ruleList = policyObj.getJSONArray("ruleList");
									boolean isHit = false;
									if (ruleList != null && ruleList.size() > 0) {
										isHit = validateRule(ruleList, connectionType);
									} else {// 如果没有规则，则无需判断
										isHit = true;
									}
									if (isHit) {
										if (enablePriority == 0) {
											long timecost = getRealtime(time, unit);
											slaVo.setTimeSum(timecost);
											slaVo.setRealTimeLeft(timecost);
											slaVo.setTimeLeft(timecost);
										} else {
											if (priorityList != null && priorityList.size() > 0) {
												for (int p = 0; p < priorityList.size(); p++) {
													JSONObject priorityObj = priorityList.getJSONObject(p);
													if (priorityObj.getString("priority").equals(processTaskVo.getPriorityUuid())) {
														long timecost = getRealtime(priorityObj.getIntValue("time"), priorityObj.getString("unit"));
														slaVo.setTimeSum(timecost);
														slaVo.setRealTimeLeft(timecost);
														slaVo.setTimeLeft(timecost);
														break POLICY;
													}
												}
											}
										}
										break;
									}
								}
							}
						}
					} else {
						// 非第一次进入，进行时间扣减
						List<ProcessTaskStepTimeAuditVo> processTaskStepTimeAuditList = processTaskStepTimeAuditMapper.getProcessTaskStepTimeAuditBySlaId(slaVo.getId());
						long realTimeCost = getRealTimeCost(processTaskStepTimeAuditList);
						long timeCost = realTimeCost;
						if (StringUtils.isNotBlank(worktimeUuid)) {// 如果有工作时间，则计算实际消耗的工作时间
							timeCost = getTimeCost(processTaskStepTimeAuditList, worktimeUuid);
						}
						slaVo.setRealTimeLeft(slaVo.getRealTimeLeft() - realTimeCost);
						slaVo.setTimeLeft(slaVo.getTimeLeft() - timeCost);

					}
					// 修正最终超时日期
					if (slaVo.getRealTimeLeft() != null) {
						slaVo.setRealExpireTime(sdf.format(new Date(now + slaVo.getRealTimeLeft())));
					} else {
						throw new RuntimeException("计算实际剩余时间失败");
					}
					if (StringUtils.isNotBlank(worktimeUuid)) {
						if (slaVo.getTimeLeft() != null) {
							long expireTime = calculateExpireTime(now, slaVo.getTimeLeft(), worktimeUuid);
							slaVo.setExpireTime(sdf.format(new Date(expireTime)));
						} else {
							throw new RuntimeException("计算剩余时间失败");
						}
					} else {
						if (slaVo.getTimeLeft() != null) {
							slaVo.setExpireTime(sdf.format(new Date(now + slaVo.getTimeLeft())));
						} else {
							throw new RuntimeException("计算剩余时间失败");
						}
					}
					processTaskMapper.updateProcessTaskSlaTime(slaVo);
				}
			}
		}
	}

	protected static class TimeAuditHandler {

		protected static void active(ProcessTaskStepVo currentProcessTaskStepVo) {
			ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
			/** 如果找不到审计记录并且activetime不为空，则新建审计记录 **/
			if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getActiveTime())) {
				processTaskStepTimeAuditVo = new ProcessTaskStepTimeAuditVo();
				processTaskStepTimeAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskStepTimeAuditVo.setActiveTime("now");
				processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			}
		}

		protected static void start(ProcessTaskStepVo currentProcessTaskStepVo) {
			ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
			/** 如果找不到审计记录并且starttime不为空，则新建审计记录 **/
			if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getStartTime())) {
				processTaskStepTimeAuditVo = new ProcessTaskStepTimeAuditVo();
				processTaskStepTimeAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskStepTimeAuditVo.setStartTime("now");
				processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
				processTaskStepTimeAuditVo.setStartTime("now");
				processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			}
		}

		protected static void success(ProcessTaskStepVo currentProcessTaskStepVo) {
			ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
			/** 如果找不到审计记录并且successtime不为空，则新建审计记录 **/
			if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getSuccessTime())) {
				processTaskStepTimeAuditVo = new ProcessTaskStepTimeAuditVo();
				processTaskStepTimeAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskStepTimeAuditVo.setSuccessTime("now");
				processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getSuccessTime())) {// 如果successtime为空，则更新successtime
				processTaskStepTimeAuditVo.setSuccessTime("now");
				processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			}
		}

		protected static void failed(ProcessTaskStepVo currentProcessTaskStepVo) {
			ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
			/** 如果找不到审计记录并且failedtime不为空，则新建审计记录 **/
			if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getFailedTime())) {
				processTaskStepTimeAuditVo = new ProcessTaskStepTimeAuditVo();
				processTaskStepTimeAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskStepTimeAuditVo.setFailedTime("now");
				processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getSuccessTime())) {// 如果failedtime为空，则更新failedtime
				processTaskStepTimeAuditVo.setFailedTime("now");
				processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			}
		}

		protected static void abort(ProcessTaskStepVo currentProcessTaskStepVo) {
			ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
			/** 如果找不到审计记录并且aborttime不为空，则新建审计记录 **/
			if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getFailedTime())) {
				processTaskStepTimeAuditVo = new ProcessTaskStepTimeAuditVo();
				processTaskStepTimeAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskStepTimeAuditVo.setAbortTime("now");
				processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getSuccessTime())) {// 如果aborttime为空，则更新aborttime
				processTaskStepTimeAuditVo.setAbortTime("now");
				processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			}
		}

		protected static void back(ProcessTaskStepVo currentProcessTaskStepVo) {
			ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
			/** 如果找不到审计记录并且backtime不为空，则新建审计记录 **/
			if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getBackTime())) {
				processTaskStepTimeAuditVo = new ProcessTaskStepTimeAuditVo();
				processTaskStepTimeAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskStepTimeAuditVo.setBackTime("now");
				processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getBackTime())) {// 如果backtime为空，则更新backtime
				processTaskStepTimeAuditVo.setBackTime("now");
				processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(processTaskStepTimeAuditVo);
			}
		}

		protected static void recover(ProcessTaskStepVo currentProcessTaskStepVo) {
			if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())) {
				active(currentProcessTaskStepVo);
			} else if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())) {
				start(currentProcessTaskStepVo);
			}
		}
	}

	protected static class ActionRoleChecker {
		protected static boolean isWorker(ProcessTaskStepVo currentProcessTaskStepVo) {
			List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getId(), ProcessTaskStepUserType.MAJOR.getValue());
			boolean hasRight = false;
			if (userList.size() > 0) {
				for (ProcessTaskStepUserVo userVo : userList) {
					if (userVo.getUserId().equals(UserContext.get().getUserId())) {
						hasRight = true;
						break;
					}
				}
			}
			return hasRight;
		}

		protected static boolean start(ProcessTaskStepVo currentProcessTaskStepVo) {
			boolean isWorker = isWorker(currentProcessTaskStepVo);
			if (!isWorker) {
				throw new ProcessTaskRuntimeException("您不是当前步骤处理人");
			}
			return isWorker;
		}

		protected static boolean abortProcessTask(ProcessTaskVo currentProcessTaskVo) {
			return true;
		}

		protected static boolean recoverProcessTask(ProcessTaskVo currentProcessTaskVo) {
			return true;
		}

		protected static boolean transfer(ProcessTaskStepVo currentProcessTaskStepVo) {
			boolean isWorker = isWorker(currentProcessTaskStepVo);
			if (!isWorker) {
				throw new ProcessTaskRuntimeException("您不是当前步骤处理人");
			}
			return isWorker;
		}
	}

	protected static class AuditHandler extends CodeDriverThread {
		private ProcessTaskStepVo currentProcessTaskStepVo;
		private ProcessTaskStepAction action;

		public AuditHandler(ProcessTaskStepVo _currentProcessTaskStepVo, ProcessTaskStepAction _action) {
			currentProcessTaskStepVo = _currentProcessTaskStepVo;
			action = _action;
		}

		protected static synchronized void save(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskStepAction action) {
			if (!TransactionSynchronizationManager.isSynchronizationActive()) {
				AuditHandler handler = new AuditHandler(currentProcessTaskStepVo, action);
				CommonThreadPool.execute(handler);
			} else {
				List<AuditHandler> handlerList = AUDIT_HANDLERS.get();
				if (handlerList == null) {
					handlerList = new ArrayList<>();
					AUDIT_HANDLERS.set(handlerList);
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						@Override
						public void afterCommit() {
							List<AuditHandler> handlerList = AUDIT_HANDLERS.get();
							for (AuditHandler handler : handlerList) {
								CommonThreadPool.execute(handler);
							}
						}

						@Override
						public void afterCompletion(int status) {
							AUDIT_HANDLERS.remove();
						}
					});
				}
				handlerList.add(new AuditHandler(currentProcessTaskStepVo, action));
			}
		}

		private void saveAuditDetail(ProcessTaskStepAuditVo processTaskStepAuditVo, ProcessTaskStepAuditDetailVo oldAudit, ProcessTaskAuditDetailType detailType, String newValue) {
			if (oldAudit == null) {
				if (StringUtils.isNotBlank(newValue)) {
					processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), detailType.getValue(), null, newValue));
				}
			} else if ((StringUtils.isBlank(oldAudit.getNewContent()) && StringUtils.isNotBlank(newValue)) || !oldAudit.getNewContent().equals(newValue)) {
				processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), detailType.getValue(), oldAudit.getNewContent(), newValue));
			}
		}

		@Override
		public void execute() {
			String oldName = Thread.currentThread().getName();
			Thread.currentThread().setName("PROCESSTASK-AUDIT-" + currentProcessTaskStepVo.getId() + "-" + action.getValue());
			try {
				ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
				processTaskStepAuditVo.setAction(action.getValue());
				processTaskStepAuditVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				processTaskStepAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);
				/** 获取作业信息 **/
				ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
				/** 获取开始节点内容信息 **/
				ProcessTaskContentVo startContentVo = null;
				List<ProcessTaskStepVo> stepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(currentProcessTaskStepVo.getProcessTaskId(), ProcessStepType.START.getValue());
				if (stepList.size() == 1) {
					ProcessTaskStepVo startStepVo = stepList.get(0);
					List<ProcessTaskStepContentVo> contentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startStepVo.getId());
					if (contentList.size() > 0) {
						ProcessTaskStepContentVo contentVo = contentList.get(0);
						startContentVo = processTaskMapper.getProcessTaskContentByHash(contentVo.getContentHash());
					}
				}
				/** 标题修改审计 **/
				ProcessTaskStepAuditDetailVo titleAudit = processTaskMapper.getProcessTaskStepAuditDetail(currentProcessTaskStepVo.getProcessTaskId(), ProcessTaskAuditDetailType.TITLE.getValue());
				saveAuditDetail(processTaskStepAuditVo, titleAudit, ProcessTaskAuditDetailType.TITLE, processTaskVo.getTitle());

				/** 内容修改审计 **/
				if (startContentVo != null) {
					ProcessTaskStepAuditDetailVo contentAudit = processTaskMapper.getProcessTaskStepAuditDetail(currentProcessTaskStepVo.getProcessTaskId(), ProcessTaskAuditDetailType.CONTENT.getValue());
					saveAuditDetail(processTaskStepAuditVo, contentAudit, ProcessTaskAuditDetailType.CONTENT, startContentVo.getHash());
				}
				/** 优先级修改审计 **/
				ProcessTaskStepAuditDetailVo urgencyAudit = processTaskMapper.getProcessTaskStepAuditDetail(currentProcessTaskStepVo.getProcessTaskId(), ProcessTaskAuditDetailType.URGENCY.getValue());
				saveAuditDetail(processTaskStepAuditVo, urgencyAudit, ProcessTaskAuditDetailType.CONTENT, processTaskVo.getUrgency());

				/** 表单修改审计 **/
				ProcessTaskStepAuditDetailVo formAudit = processTaskMapper.getProcessTaskStepAuditDetail(currentProcessTaskStepVo.getProcessTaskId(), ProcessTaskAuditDetailType.FORM.getValue());

				List<ProcessTaskFormAttributeDataVo> formAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				JSONObject newFormObj = new JSONObject();
				for (ProcessTaskFormAttributeDataVo attributeData : formAttributeDataList) {
					newFormObj.put(attributeData.getAttributeUuid(), attributeData.getData());
				}

				if (formAudit == null) {
					if (!newFormObj.isEmpty()) {
						processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.FORM.getValue(), null, newFormObj.toJSONString()));
					}
				} else {
					Javers javers = JaversBuilder.javers().build();
					JSONObject oldFormObj = JSONObject.parseObject(formAudit.getNewContent());
					Diff diff = javers.compare(newFormObj, oldFormObj);
					if (diff.hasChanges()) {
						processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.FORM.getValue(), formAudit.getNewContent(), newFormObj.toJSONString()));
					}
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			} finally {
				Thread.currentThread().setName(oldName);
			}
		}
	}

}
