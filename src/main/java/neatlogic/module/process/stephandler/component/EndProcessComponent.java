package neatlogic.module.process.stephandler.component;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.crossover.IProcessTaskCrossoverMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.*;
import neatlogic.framework.transaction.core.AfterTransactionJob;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class EndProcessComponent extends ProcessStepHandlerBase {

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

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
        /*设置已完成标记位**/
        currentProcessTaskStepVo.setIsAllDone(true);
        return 0;
    }

    @Override
    protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
        String source = currentProcessTaskStepVo.getParamObj().getString("source");
        IProcessTaskCrossoverMapper processTaskCrossoverMapper = CrossoverServiceFactory.getApi(IProcessTaskCrossoverMapper.class);
        ProcessTaskVo processTaskVo = processTaskCrossoverMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
        processTaskVo.setStatus(ProcessTaskStatus.SUCCEED.getValue());
        //自动评分
        processStepHandlerUtil.autoScore(processTaskVo);

        //调用外部处理器
        List<IProcessStepExtendHandler> exHandlerList = ProcessStepExtendHandlerFactory.getHandlers(this.getHandler());
        if (CollectionUtils.isNotEmpty(exHandlerList)) {
            for (IProcessStepExtendHandler handler : exHandlerList) {
                AfterTransactionJob<ProcessTaskStepVo> job = new AfterTransactionJob<>("PROCESSTASK-STEP-EXTEND-HANDLER");
                job.execute(currentProcessTaskStepVo, handler::complete);
            }
        }

        // 重复事件工单自动取消
        Long repeatGroupId = processTaskCrossoverMapper.getRepeatGroupIdByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        if (repeatGroupId != null) {
            List<Long> repeatProcessTaskIdList = processTaskCrossoverMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
            repeatProcessTaskIdList.remove(currentProcessTaskStepVo.getProcessTaskId());
            if (CollectionUtils.isNotEmpty(repeatProcessTaskIdList)) {
                List<ProcessTaskVo> repeatProcessTaskList = processTaskCrossoverMapper.getProcessTaskListByIdList(repeatProcessTaskIdList);
                for (ProcessTaskVo repeatProcessTask : repeatProcessTaskList) {
                    ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                    processTaskStepVo.setProcessTaskId(repeatProcessTask.getId());
                    processTaskStepVo.setIsAutoGenerateId(false);
                    processTaskStepVo.getParamObj().put("source", source);
                    processTaskStepVo.getParamObj().put("repeatProcessTaskSerialNumber", processTaskVo.getSerialNumber());
                    processTaskStepVo.getParamObj().put("repeatProcessTaskTitle", processTaskVo.getTitle());
                    ProcessTaskStepThread thread = new ProcessTaskStepThread(ProcessTaskOperationType.PROCESSTASK_ABORT, processTaskStepVo, null) {
                        @Override
                        protected void myExecute(ProcessTaskStepVo processTaskStepVo) {
                            processTaskStepVo.getParamObj().put("repeatProcessTaskSerialNumber", processTaskVo.getSerialNumber());
                            processTaskStepVo.getParamObj().put("repeatProcessTaskTitle", processTaskVo.getTitle());
                            processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.AUTOCANCELREPEAT);
                            UserContext.init(SystemUser.SYSTEM);
                            ProcessStepHandlerFactory.getHandler().abortProcessTask(new ProcessTaskVo(processTaskStepVo.getProcessTaskId()));
                        }
                    };
                    AfterTransactionJob<ProcessStepThread> afterTransactionJob = new AfterTransactionJob<>("PROCESSTASK-REPEAT-ABORT");
                    afterTransactionJob.execute(thread);
                }
            }
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
     *
     * @return
     */
    @Override
    public int getForwardOutputQuantity() {
        return 0;
    }

    /**
     * 回退输入路径数量
     * -1代表不限制
     *
     * @return
     */
    @Override
    public int getBackwardInputQuantity() {
        return 0;
    }

    @Override
    public boolean allowDispatchStepWorker() {
        return false;
    }

}
