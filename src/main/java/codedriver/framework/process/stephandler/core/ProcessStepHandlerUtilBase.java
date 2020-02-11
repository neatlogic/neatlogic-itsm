package codedriver.framework.process.stephandler.core;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAuditMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTimeAuditMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.notify.NotifyHandlerNotFoundException;
import codedriver.framework.process.notify.core.INotifyHandler;
import codedriver.framework.process.notify.core.NotifyHandlerFactory;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.process.notify.dao.mapper.NotifyMapper;
import codedriver.framework.process.notify.schedule.plugin.ProcessTaskStepNotifyJob;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskSlaNotifyVo;
import codedriver.module.process.dto.ProcessTaskSlaTimeVo;
import codedriver.module.process.dto.ProcessTaskSlaVo;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepTimeAuditVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.dto.WorktimeRangeVo;
import codedriver.module.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.module.process.formattribute.core.IFormAttributeHandler;
import codedriver.module.process.notify.dto.NotifyTemplateVo;
import codedriver.module.process.notify.dto.NotifyVo;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class ProcessStepHandlerUtilBase {
	static Logger logger = LoggerFactory.getLogger(ProcessStepHandlerUtilBase.class);

	private static final ThreadLocal<List<AuditHandler>> AUDIT_HANDLERS = new ThreadLocal<>();
	private static final ThreadLocal<List<SlaHandler>> SLA_HANDLERS = new ThreadLocal<>();
	private static final ThreadLocal<List<NotifyHandler>> NOTIFY_HANDLERS = new ThreadLocal<>();
	protected static ProcessMapper processMapper;
	protected static ProcessTaskMapper processTaskMapper;
	protected static ProcessTaskAuditMapper processTaskAuditMapper;
	protected static FormMapper formMapper;
	protected static UserMapper userMapper;
	protected static ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;
	private static WorktimeMapper worktimeMapper;
	private static ChannelMapper channelMapper;
	private static NotifyMapper notifyMapper;
	private static SchedulerManager schedulerManager;

	@Autowired
	public void setSchedulerManager(SchedulerManager _schedulerManager) {
		schedulerManager = _schedulerManager;
	}

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

	@Autowired
	public void setNotifyMapper(NotifyMapper _notifyMapper) {
		notifyMapper = _notifyMapper;
	}

	/*
	 * public static void main(String[] atr) { ScriptEngineManager sem = new
	 * ScriptEngineManager();
	 * 
	 * ScriptEngine se = sem.getEngineByName("nashorn"); JSONObject paramObj = new
	 * JSONObject(); paramObj.put("form.name", "chenqw"); paramObj.put("form.age",
	 * "37"); se.put("json", paramObj); String script =
	 * "json['form.name'] == 'chen2qw' && json['form.age'] == '37'"; try {
	 * System.out.println(Boolean.parseBoolean(se.eval(script).toString())); } catch
	 * (ScriptException e) { logger.error(e.getMessage(), e); }
	 * 
	 * }
	 */

	protected static class NotifyHandler extends CodeDriverThread {
		private ProcessTaskStepVo currentProcessTaskStepVo;
		private NotifyTriggerType notifyTriggerType;

		public NotifyHandler(ProcessTaskStepVo _currentProcessTaskStepVo, NotifyTriggerType _trigger) {
			currentProcessTaskStepVo = _currentProcessTaskStepVo;
			notifyTriggerType = _trigger;
			if (_currentProcessTaskStepVo != null) {
				this.setThreadName("PROCESSTASK-NOTIFY-" + _currentProcessTaskStepVo.getId());
			}
		}

		protected static void notify(ProcessTaskStepVo currentProcessTaskStepVo, NotifyTriggerType trigger) {
			if (!TransactionSynchronizationManager.isSynchronizationActive()) {
				CommonThreadPool.execute(new NotifyHandler(currentProcessTaskStepVo, trigger));
			} else {
				List<NotifyHandler> handlerList = NOTIFY_HANDLERS.get();
				if (handlerList == null) {
					handlerList = new ArrayList<>();
					NOTIFY_HANDLERS.set(handlerList);
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						@Override
						public void afterCommit() {
							List<NotifyHandler> handlerList = NOTIFY_HANDLERS.get();
							for (NotifyHandler handler : handlerList) {
								CommonThreadPool.execute(handler);
							}
						}

						@Override
						public void afterCompletion(int status) {
							NOTIFY_HANDLERS.remove();
						}
					});
				}
				handlerList.add(new NotifyHandler(currentProcessTaskStepVo, trigger));
			}
		}

		private static String getFreemarkerContent(NotifyVo notifyVo, String content) {
			String resultStr = "";
			if (content != null) {
				Configuration cfg = new Configuration();
				cfg.setNumberFormat("0.##");
				cfg.setClassicCompatible(true);
				StringTemplateLoader stringLoader = new StringTemplateLoader();
				stringLoader.putTemplate("template", content);
				cfg.setTemplateLoader(stringLoader);
				Template temp;
				Writer out = null;
				try {
					temp = cfg.getTemplate("template", "utf-8");
					out = new StringWriter();
					temp.process(notifyVo, out);
					resultStr = out.toString();
					out.flush();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				} catch (TemplateException e) {
					logger.error(e.getMessage(), e);
				}
			}
			return resultStr;
		}

		public static void main(String[] argv) {
			NotifyVo notifyVo = new NotifyVo();
			JSONObject jsonObj = new JSONObject();
			ProcessTaskVo processTaskVo = new ProcessTaskVo();
			processTaskVo.setTitle("标题");
			notifyVo.addData("name", "陈其炜");
			notifyVo.addData("processTask", processTaskVo);
			String content = "abc${data.name}abc${data.processTask.title}";
			System.out.println(getFreemarkerContent(notifyVo, content));
		}

		@Override
		protected void execute() {
			try {
				ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
				ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
				List<ProcessTaskStepWorkerVo> workerList = null;
				if (StringUtils.isNotBlank(stepVo.getConfigHash())) {
					String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(stepVo.getConfigHash());
					JSONObject stepConfigObj = null;
					stepConfigObj = JSONObject.parseObject(stepConfig);

					if (stepConfigObj != null && stepConfigObj.containsKey("notifyList")) {
						JSONArray notifyList = stepConfigObj.getJSONArray("notifyList");
						for (int i = 0; i < notifyList.size(); i++) {
							JSONObject notifyObj = notifyList.getJSONObject(i);
							String trigger = notifyObj.getString("trigger");
							String type = notifyObj.getString("type");
							String templateUuid = notifyObj.getString("template");
							JSONArray receiverList = notifyObj.getJSONArray("receiverList");
							if (receiverList != null && receiverList.size() > 0 && notifyTriggerType.getTrigger().equalsIgnoreCase(trigger)) {
								String titleTemplate = null, contentTemplate = null;
								NotifyTemplateVo notifyTemplateVo = null;
								if (StringUtils.isNotBlank(templateUuid)) {
									notifyTemplateVo = notifyMapper.getNotifyTemplateByUuid(templateUuid);
								}
								if (notifyTemplateVo != null) {
									titleTemplate = notifyTemplateVo.getTitle();
									contentTemplate = notifyTemplateVo.getContent();
								} else {
									titleTemplate = notifyTriggerType.getTitleTemplate();
									contentTemplate = notifyTriggerType.getContentTemplate();
								}
								INotifyHandler handler = NotifyHandlerFactory.getHandler(type);
								if (handler != null) {
									NotifyVo notifyVo = new NotifyVo();
									/***
									 * 注入流程作业信息 不够将来再补充
									 */
									notifyVo.addData("title", processTaskVo.getTitle());
									notifyVo.addData("reporter", processTaskVo.getReporter());
									notifyVo.addData("reporterName", processTaskVo.getReporterName());
									notifyVo.addData("status", processTaskVo.getStatus());
									notifyVo.addData("stepName", stepVo.getName());
									notifyVo.addData("stepStatus", stepVo.getStatus());
									/** 注入结束 **/

									if (StringUtils.isNotBlank(titleTemplate)) {
										notifyVo.setTitle(getFreemarkerContent(notifyVo, titleTemplate));
									}
									if (StringUtils.isNotBlank(contentTemplate)) {
										notifyVo.setContent(getFreemarkerContent(notifyVo, contentTemplate));
									}
									for (int u = 0; u < receiverList.size(); u++) {
										String worker = receiverList.getString(u);
										if (worker.startsWith("default.")) {
											worker = worker.substring(8);
											if (workerList == null) {
												workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(currentProcessTaskStepVo.getId());
											}
											if (worker.equalsIgnoreCase("reporter")) {
												notifyVo.addUserId(processTaskVo.getReporter());
											} else if (worker.equalsIgnoreCase("owner")) {
												notifyVo.addUserId(processTaskVo.getOwner());
											} else if (worker.equalsIgnoreCase("worker")) {
												for (ProcessTaskStepWorkerVo workerVo : workerList) {
													notifyVo.addUserId(workerVo.getUserId());
												}
											}
										} else if (worker.startsWith("user.")) {
											worker = worker.substring(5);
											notifyVo.addUserId(worker);
										} else if (worker.startsWith("team.")) {
											worker = worker.substring(5);
											List<UserVo> teamUserIdList = userMapper.getActiveUserByTeamId(worker);
											for (UserVo userVo : teamUserIdList) {
												notifyVo.addUserId(userVo.getUserId());
											}
										}
									}

									handler.execute(notifyVo);
								} else {
									throw new NotifyHandlerNotFoundException(type);
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				logger.error("通知失败：" + ex.getMessage(), ex);
			}
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
				if (auditVo.getCompleteTimeLong() != null) {
					endTime = auditVo.getCompleteTimeLong();
				} else if (auditVo.getAbortTimeLong() != null) {
					endTime = auditVo.getAbortTimeLong();
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
					if (auditVo.getCompleteTimeLong() != null) {
						endTime = auditVo.getCompleteTimeLong();
					} else if (auditVo.getAbortTimeLong() != null) {
						endTime = auditVo.getAbortTimeLong();
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
					ProcessTaskSlaTimeVo slaTimeVo = slaVo.getSlaTimeVo();
					boolean isSlaTimeExists = false;
					if (slaTimeVo == null) {
						if (slaVo.getConfigObj() != null) {
							JSONArray policyList = slaVo.getConfigObj().getJSONArray("calculatePolicyList");
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
										slaTimeVo = new ProcessTaskSlaTimeVo();
										if (enablePriority == 0) {
											long timecost = getRealtime(time, unit);
											slaTimeVo.setTimeSum(timecost);
											slaTimeVo.setRealTimeLeft(timecost);
											slaTimeVo.setTimeLeft(timecost);
										} else {
											if (priorityList != null && priorityList.size() > 0) {
												for (int p = 0; p < priorityList.size(); p++) {
													JSONObject priorityObj = priorityList.getJSONObject(p);
													if (priorityObj.getString("priority").equals(processTaskVo.getPriorityUuid())) {
														long timecost = getRealtime(priorityObj.getIntValue("time"), priorityObj.getString("unit"));
														slaTimeVo.setTimeSum(timecost);
														slaTimeVo.setRealTimeLeft(timecost);
														slaTimeVo.setTimeLeft(timecost);
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
						isSlaTimeExists = true;
						// 非第一次进入，进行时间扣减
						List<ProcessTaskStepTimeAuditVo> processTaskStepTimeAuditList = processTaskStepTimeAuditMapper.getProcessTaskStepTimeAuditBySlaId(slaVo.getId());
						long realTimeCost = getRealTimeCost(processTaskStepTimeAuditList);
						long timeCost = realTimeCost;
						if (StringUtils.isNotBlank(worktimeUuid)) {// 如果有工作时间，则计算实际消耗的工作时间
							timeCost = getTimeCost(processTaskStepTimeAuditList, worktimeUuid);
						}
						slaTimeVo.setRealTimeLeft(slaTimeVo.getRealTimeLeft() - realTimeCost);
						slaTimeVo.setTimeLeft(slaTimeVo.getTimeLeft() - timeCost);

					}

					// 修正最终超时日期
					if (slaTimeVo != null) {
						slaTimeVo.setRealExpireTime(sdf.format(new Date(now + slaTimeVo.getRealTimeLeft())));
						if (StringUtils.isNotBlank(worktimeUuid)) {
							if (slaTimeVo.getTimeLeft() != null) {
								long expireTime = calculateExpireTime(now, slaTimeVo.getTimeLeft(), worktimeUuid);
								slaTimeVo.setExpireTime(sdf.format(new Date(expireTime)));
							} else {
								throw new RuntimeException("计算剩余时间失败");
							}
						} else {
							if (slaTimeVo.getTimeLeft() != null) {
								slaTimeVo.setExpireTime(sdf.format(new Date(now + slaTimeVo.getTimeLeft())));
							} else {
								throw new RuntimeException("计算剩余时间失败");
							}
						}
						slaTimeVo.setSlaId(slaVo.getId());
						if (isSlaTimeExists) {
							processTaskMapper.updateProcessTaskSlaTime(slaTimeVo);
						} else {
							processTaskMapper.insertProcessTaskSlaTime(slaTimeVo);
						}

						// 执行超时操作
						if (StringUtils.isNotBlank(slaTimeVo.getExpireTime()) && slaVo.getConfigObj() != null) {
							JSONArray notifyPolicyList = slaVo.getConfigObj().getJSONArray("notifyPolicyList");
							if (notifyPolicyList != null && notifyPolicyList.size() > 0) {
								for (int i = 0; i < notifyPolicyList.size(); i++) {
									JSONObject notifyPolicyObj = notifyPolicyList.getJSONObject(i);
									ProcessTaskSlaNotifyVo processTaskSlaNotifyVo = new ProcessTaskSlaNotifyVo();
									processTaskSlaNotifyVo.setSlaId(slaVo.getId());
									processTaskSlaNotifyVo.setConfig(notifyPolicyObj.toJSONString());
									processTaskMapper.insertProcessTaskSlaNotify(processTaskSlaNotifyVo);

									IJob jobHandler = SchedulerManager.getHandler(ProcessTaskStepNotifyJob.class.getName());
									if (jobHandler != null) {
										JobObject jobObject = new JobObject.Builder(currentProcessTaskStepVo.getId().toString(), jobHandler.getGroupName(), jobHandler.getClassName(), TenantContext.get().getTenantUuid()).build();
										jobObject.addData("slaId", slaVo.getId());
										jobObject.addData("hash", processTaskSlaNotifyVo.getHash());
										jobHandler.reloadJob(jobObject);
									} else {
										throw new ScheduleHandlerNotFoundException(ProcessTaskStepNotifyJob.class.getName());
									}

								}
							}
						}
					}
				}
			}
		}
	}

	protected static class TimeAuditHandler {
		protected static void audit(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskStepAction action) {
			ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo = processTaskStepTimeAuditMapper.getLastProcessTaskStepTimeAuditByStepId(currentProcessTaskStepVo.getId());
			boolean needNewAuduit = false;
			ProcessTaskStepTimeAuditVo newAuditVo = new ProcessTaskStepTimeAuditVo();
			newAuditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
			switch (action) {
			case ACTIVE:
				newAuditVo.setActiveTime("now");
				if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getActiveTime())) {
					processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
				}
				break;
			case START:
				newAuditVo.setStartTime("now");
				if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getStartTime())) {
					processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
				} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
					newAuditVo.setId(processTaskStepTimeAuditVo.getId());
					processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
				}
				break;
			case COMPLETE:
				/** 如果找不到审计记录并且completetime不为空，则新建审计记录 **/
				newAuditVo.setCompleteTime("now");
				if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getCompleteTime())) {
					processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
				} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getCompleteTime())) {// 如果completetime为空，则更新completetime
					newAuditVo.setId(processTaskStepTimeAuditVo.getId());
					processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
				}
				break;
			case ABORT:
				/** 如果找不到审计记录并且aborttime不为空，则新建审计记录 **/
				newAuditVo.setAbortTime("now");
				if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getAbortTime())) {
					processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
				} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getAbortTime())) {// 如果aborttime为空，则更新aborttime
					newAuditVo.setId(processTaskStepTimeAuditVo.getId());
					processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
				}
				break;
			case BACK:
				/** 如果找不到审计记录并且backtime不为空，则新建审计记录 **/
				newAuditVo.setBackTime("now");
				if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getBackTime())) {
					processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
				} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getBackTime())) {// 如果backtime为空，则更新backtime
					newAuditVo.setId(processTaskStepTimeAuditVo.getId());
					processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
				}
				break;
			case RECOVER:
				if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.PENDING.getValue())) {
					newAuditVo.setActiveTime("now");
					if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getActiveTime())) {
						processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
					}
				} else if (currentProcessTaskStepVo.getStatus().equals(ProcessTaskStatus.RUNNING.getValue())) {
					newAuditVo.setStartTime("now");
					if (processTaskStepTimeAuditVo == null || StringUtils.isNotBlank(processTaskStepTimeAuditVo.getStartTime())) {
						processTaskStepTimeAuditMapper.insertProcessTaskStepTimeAudit(newAuditVo);
					} else if (StringUtils.isBlank(processTaskStepTimeAuditVo.getStartTime())) {// 如果starttime为空，则更新starttime
						newAuditVo.setId(processTaskStepTimeAuditVo.getId());
						processTaskStepTimeAuditMapper.updateProcessTaskStepTimeAudit(newAuditVo);
					}
				}
				break;
			}
		}
	}

	protected static class ActionRoleChecker {
		protected static boolean isWorker(ProcessTaskStepVo currentProcessTaskStepVo) {
			List<ProcessTaskStepUserVo> userList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getId(), UserType.MAJOR.getValue());
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

		protected static synchronized void audit(ProcessTaskStepVo currentProcessTaskStepVo, ProcessTaskStepAction action) {
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
