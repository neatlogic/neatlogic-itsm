package codedriver.framework.process.stephandler.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.constvalue.ProcessStepMode;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;

@Service
public class DistributaryProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(DistributaryProcessComponent.class);

	@Override
	public String getName() {
		return ProcessStepHandler.DISTRIBUTARY.getName();
	}

	@Override
	public String getIcon() {
		return "ts-branch";
	}

	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.AT;
	}

	@Override
	public boolean isAsync() {
		return true;
	}

	@Override
	public String getType() {
		return ProcessStepHandler.DISTRIBUTARY.getType();
	}

	@Override
	protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) {
		/** 设置已完成标记位 **/
		currentProcessTaskStepVo.setIsAllDone(true);
		return 0;
	}

	@Override
	protected List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo) {
		return processTaskMapper.getToProcessTaskStepByFromId(currentProcessTaskStepVo.getId());
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentFlowJobStepVo) {
		return 1;
	}

	@Override
	public int getSort() {
		return 0;
	}

	@Override
	public Boolean isAllowStart() {
		return null;
	}

	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo){
		return 0;
	}

	@Override
	protected int myStart(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
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
	protected int myStartProcess(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		return 0;
	}

}
