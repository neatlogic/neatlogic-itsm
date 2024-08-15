package neatlogic.module.process.stephandler.component;

import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessStepMode;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DistributaryProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(DistributaryProcessComponent.class);

	@Override
	public String getName() {
		return ProcessStepHandlerType.DISTRIBUTARY.getName();
	}

	@Override
	public String getType() {
		return ProcessStepHandlerType.DISTRIBUTARY.getType();
	}

	@SuppressWarnings("serial")
	@Override
	public JSONObject getChartConfig() {
		return new JSONObject() {
			{
				this.put("icon", "tsfont-shunt");
				this.put("shape", "L-triangle:R-triangle");
				this.put("width", 68);
				this.put("height", 68);
				this.put("rdy", 68 / 4);
			}
		};
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
	public String getHandler() {
		return ProcessStepHandlerType.DISTRIBUTARY.getHandler();
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
	protected Set<Long> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo, List<Long> nextStepIdList, Long nextStepId) throws ProcessTaskException {
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		String action = paramObj.getString("action");
		if (ProcessTaskOperationType.STEP_BACK.getValue().equals(action)) {
			return new HashSet<>();
		} else {
			return new HashSet<>(nextStepIdList);
		}
	}

	@Override
	protected int myRedo(ProcessTaskStepVo currentProcessTaskStepVo) {
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
	protected int myReapproval(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myReapprovalAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	public int getSort() {
		return 2;
	}

	@Override
	public Boolean isAllowStart() {
		return null;
	}

	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
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
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int mySaveDraft(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
		return 0;
	}

    @Override
    protected int myPause(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

	/**
	 * 回退输出路径数量
	 * -1代表不限制
	 * @return
	 */
	@Override
	public int getBackwardOutputQuantity() {
		return 0;
	}

	@Override
	public boolean allowDispatchStepWorker() {
		return false;
	}

}
