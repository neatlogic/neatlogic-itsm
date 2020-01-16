package codedriver.framework.process.stephandler.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAuditMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTimeAuditMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.ProcessTaskStepUserType;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepTimeAuditVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

public abstract class ProcessStepHandlerUtilBase {
	static Logger logger = LoggerFactory.getLogger(ProcessStepHandlerUtilBase.class);

	private static final ThreadLocal<List<AuditHandler>> AUDIT_HANDLERS = new ThreadLocal<>();
	protected static ProcessMapper processMapper;
	protected static ProcessTaskMapper processTaskMapper;
	protected static ProcessTaskAuditMapper processTaskAuditMapper;
	protected static FormMapper formMapper;
	protected static UserMapper userMapper;
	protected static ProcessTaskStepTimeAuditMapper processTaskStepTimeAuditMapper;

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
