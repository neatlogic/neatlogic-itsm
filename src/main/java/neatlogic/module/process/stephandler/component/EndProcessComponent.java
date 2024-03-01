package neatlogic.module.process.stephandler.component;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.approve.ApproveHandlerNotFoundException;
import neatlogic.framework.process.approve.constvalue.ApproveReply;
import neatlogic.framework.process.approve.core.ApproveHandlerFactory;
import neatlogic.framework.process.approve.core.IApproveHandler;
import neatlogic.framework.process.approve.dto.ApproveEntityVo;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessStepMode;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerBase;
import neatlogic.module.process.dao.mapper.ProcessTaskApproveMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class EndProcessComponent extends ProcessStepHandlerBase {

	@Resource
	private ProcessTaskApproveMapper processTaskApproveMapper;

	@Override
	public String getHandler() {
		return ProcessStepHandlerType.END.getHandler();
	}
	
	@Override
	public String getType() {
		return ProcessStepHandlerType.END.getType();
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
		return ProcessStepHandlerType.END.getName();
	}

	@Override
	public int getSort() {
		return 1;
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
	protected Set<Long> myGetNext(ProcessTaskStepVo processTaskStepVo, List<Long> nextStepIdList, Long nextStepId) throws ProcessTaskException {
		return null;
	}

	@Override
	protected int myRedo(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
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
//		processTaskMapper.updateProcessTaskStatus(processTaskVo);
		//自动评分
		IProcessStepHandlerUtil.autoScore(processTaskVo);

		String configStr = processTaskApproveMapper.getProcessTaskApproveEntityConfigByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		if (StringUtils.isNotBlank(configStr)) {
			ApproveEntityVo approveEntity = JSONObject.parseObject(configStr, ApproveEntityVo.class);
			IApproveHandler handler = ApproveHandlerFactory.getHandler(approveEntity.getType());
			if (handler == null) {
				throw new ApproveHandlerNotFoundException(approveEntity.getType());
			}
			ApproveReply approveReply = null;
			String approveStatus = processTaskApproveMapper.getApproveStatusByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
			if (Objects.equals(approveStatus, ApproveReply.ACCEPT.getValue())) {
				approveReply = ApproveReply.ACCEPT;
			} else if (Objects.equals(approveStatus, ApproveReply.DENY.getValue())) {
				approveReply = ApproveReply.DENY;
			} else {
				approveReply = ApproveReply.NEUTRAL;
			}
			handler.callback(currentProcessTaskStepVo.getProcessTaskId(), approveReply, approveEntity.getId(), null);
		}
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
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet) throws ProcessTaskException {
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
	 * 正向输出路径数量
	 * -1代表不限制
	 * @return
	 */
	@Override
	public int getForwardOutputQuantity() {
		return 0;
	}
	/**
	 * 回退输入路径数量
	 * -1代表不限制
	 * @return
	 */
	@Override
	public int getBackwardInputQuantity() {
		return 0;
	}

}
