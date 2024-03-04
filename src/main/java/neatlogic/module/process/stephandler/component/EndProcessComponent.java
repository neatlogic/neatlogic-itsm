package neatlogic.module.process.stephandler.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.threadlocal.ConditionParamContext;
import neatlogic.framework.dto.condition.ConditionConfigVo;
import neatlogic.framework.process.approve.ApproveHandlerNotFoundException;
import neatlogic.framework.process.approve.core.ApproveHandlerFactory;
import neatlogic.framework.process.approve.core.IApproveHandler;
import neatlogic.framework.process.approve.dto.ApproveEntityVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerBase;
import neatlogic.framework.util.RunScriptUtil;
import neatlogic.module.process.dao.mapper.ProcessTaskApproveMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EndProcessComponent extends ProcessStepHandlerBase {

	private Logger logger = LoggerFactory.getLogger(EndProcessComponent.class);

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
		/* 更新工单审批状态 */
		String status = updateApproveStatus(currentProcessTaskStepVo);

		String configStr = processTaskApproveMapper.getProcessTaskApproveEntityConfigByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		if (StringUtils.isNotBlank(configStr)) {
			ApproveEntityVo approveEntity = JSONObject.parseObject(configStr, ApproveEntityVo.class);
			IApproveHandler handler = ApproveHandlerFactory.getHandler(approveEntity.getType());
			if (handler == null) {
				throw new ApproveHandlerNotFoundException(approveEntity.getType());
			}
			ProcessTaskFinalStatus finalStatus = null;
			if (Objects.equals(status, ProcessTaskFinalStatus.SUCCEED.getValue())) {
				finalStatus = ProcessTaskFinalStatus.SUCCEED;
			} else if (Objects.equals(status, ProcessTaskFinalStatus.FAILED.getValue())) {
				finalStatus = ProcessTaskFinalStatus.FAILED;
			}
			handler.callback(currentProcessTaskStepVo.getProcessTaskId(), finalStatus, approveEntity.getId(), null);
		}
		return 0;
	}

	/**
	 * 更新审批状态
	 * @param currentProcessTaskStepVo
	 */
	public String updateApproveStatus(ProcessTaskStepVo currentProcessTaskStepVo) {
		String finalStatus = null;
		ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
		String processTaskConfig = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTask.getConfigHash());
		JSONObject finalStatusConfig = (JSONObject) JSONPath.read(processTaskConfig, "process.processConfig.finalStatusConfig");
		if (MapUtils.isNotEmpty(finalStatusConfig)) {
			JSONArray conditionGroupList = finalStatusConfig.getJSONArray("conditionGroupList");
			if (CollectionUtils.isNotEmpty(conditionGroupList)) {
				List<String> conditionProcessTaskOptions = Arrays.stream(ConditionProcessTaskOptions.values()).map(ConditionProcessTaskOptions::getValue).collect(Collectors.toList());
				JSONObject conditionParamData = ProcessTaskConditionFactory.getConditionParamData(conditionProcessTaskOptions, currentProcessTaskStepVo);
				ConditionConfigVo conditionConfigVo = null;
				try {
					ConditionParamContext.init(conditionParamData).setTranslate(true);
					conditionConfigVo = new ConditionConfigVo(finalStatusConfig);
					String script = conditionConfigVo.buildScript();
					// ((false || true) || (true && false) || (true || false))
					if (RunScriptUtil.runScript(script)) {
						finalStatus = ProcessTaskFinalStatus.SUCCEED.getValue();
					} else {
						finalStatus = ProcessTaskFinalStatus.FAILED.getValue();
					}
					processTaskApproveMapper.insertProcessTaskApproveStatus(currentProcessTaskStepVo.getProcessTaskId(), finalStatus);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					ConditionParamContext.get().release();
				}
			}
		}
		return finalStatus;
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
