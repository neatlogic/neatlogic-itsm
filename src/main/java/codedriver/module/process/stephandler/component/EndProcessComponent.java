package codedriver.module.process.stephandler.component;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;

@Service
public class EndProcessComponent extends ProcessStepHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandler.END.getHandler();
	}
	
	@Override
	public String getType() {
		return ProcessStepHandler.END.getType();
	}
	
	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.AT;
	}

	@SuppressWarnings("serial")
	@Override
	public JSONObject getChartConfig() {
		return new JSONObject() {
			{
				this.put("shape", "circle");
				this.put("width", 40);
				this.put("height", 40);
				this.put("deleteable", false);
			}
		};
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
	public Boolean isAllowStart() {
		return true;
	}


	@Override
	public List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo processTaskStepVo) {
		return null;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
		
		return 1;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		/**设置已完成标记位**/
		currentProcessTaskStepVo.setIsAllDone(true);
		return 0;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
		ProcessTaskVo processTaskVo = new ProcessTaskVo();
		processTaskVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
		processTaskVo.setId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskMapper.updateProcessTaskStatus(processTaskVo);
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
	protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}


	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
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

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int mySaveDraft(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	public void updateProcessTaskStepUserAndWorker(List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject makeupConfig(JSONObject configObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getHandlerStepInfo(Long processTaskStepId) {
		// TODO Auto-generated method stub
		return null;
	}
}
