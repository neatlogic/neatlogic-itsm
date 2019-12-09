package codedriver.framework.process.event.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.process.dao.mapper.ProcessEventMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.ProcessTaskRuntimeException;
import codedriver.module.process.constvalue.ProcessTaskEvent;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Service
public class ProcessEventHandler {
	static Logger logger = LoggerFactory.getLogger(ProcessEventHandler.class);

	private static final ThreadLocal<List<Runnable>> RUNNABLES = new ThreadLocal<List<Runnable>>();

	private static ProcessEventMapper processEventMapper;

	private static ProcessTaskMapper processTaskMapper;

	@Autowired
	public void setProcessEventMapper(ProcessEventMapper _processEventMapper) {
		processEventMapper = _processEventMapper;
	}

	@Autowired
	public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
		processTaskMapper = _processTaskMapper;
	}

	public synchronized static void doEvent(ProcessTaskEvent event, Long flowJobStepId) {
		ProcessEventHandler.EventRunner runner = new ProcessEventHandler.EventRunner(event, flowJobStepId);
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			CommonThreadPool.execute(runner);
			return;
		}
		List<Runnable> runableActionList = RUNNABLES.get();
		if (runableActionList == null) {
			runableActionList = new ArrayList<Runnable>();
			RUNNABLES.set(runableActionList);
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				@Override
				public void afterCommit() {
					List<Runnable> runableActionList = RUNNABLES.get();
					for (int i = 0; i < runableActionList.size(); i++) {
						Runnable runnable = runableActionList.get(i);
						CommonThreadPool.execute(runnable);
					}
				}

				@Override
				public void afterCompletion(int status) {
					RUNNABLES.remove();
				}
			});
		}
		runableActionList.add(runner);

	}

	static class EventRunner implements Runnable {
		private Long processTaskStepId;
		private ProcessTaskEvent event;

		public EventRunner(ProcessTaskEvent _event, Long _processTaskStepId) {
			event = _event;
			processTaskStepId = _processTaskStepId;
		}

		@Override
		public void run() {
			String oldName = Thread.currentThread().getName();
			Thread.currentThread().setName("PROCESSTASK-EVENTHANDLER-" + processTaskStepId);
			try {
				ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);

				if (processTaskStepVo == null) {
					throw new ProcessTaskRuntimeException("找不到步骤信息，processTaskStepId：" + processTaskStepId);
				}

			} finally {
				Thread.currentThread().setName(oldName);
			}
		}

	}
}
