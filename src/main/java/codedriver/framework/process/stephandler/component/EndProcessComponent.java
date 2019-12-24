package codedriver.framework.process.stephandler.component;

import java.util.List;

import org.springframework.stereotype.Service;

import codedriver.framework.process.exception.ProcessTaskAbortException;
import codedriver.framework.process.exception.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Service
public class EndProcessComponent extends ProcessStepHandlerBase {

	@Override
	public String getType() {
		return ProcessStepHandler.END.getType();
	}

	@Override
	public String getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return ProcessStepHandler.END.getName();
	}

	@Override
	public int getSort() {
		return 3;
	}

	@Override
	protected int myActive(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	protected int myStart(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	public String getEditPage() {
		return "process.step.handler.end.edit";
	}

	@Override
	public String getViewPage() {
		return "process.step.handler.end.view";
	}

	@Override
	public Boolean isAllowStart() {
		return true;
	}


	@Override
	public List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo processTaskStepVo) {
		return null;
	}

	@Override
	protected int myInit(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
		
		return 1;
	}

	@Override
	public boolean isAsync() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException, ProcessTaskAbortException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int mySave(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

}
