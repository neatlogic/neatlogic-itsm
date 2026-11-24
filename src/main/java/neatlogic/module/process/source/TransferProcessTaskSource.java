package neatlogic.module.process.source;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.IProcessTaskSource;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskSource;
import neatlogic.framework.process.dto.ProcessTaskInvokeVo;
import neatlogic.framework.process.dto.ProcessTaskReportSourceVo;
import neatlogic.framework.process.dto.ProcessTaskTransferReportVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/** 通过转报关系定位触发建单的工单。 */
@Component
public class TransferProcessTaskSource implements IProcessTaskSource {
    @Resource
    private ProcessTaskMapper processTaskMapper;

    /** 复用历史转报来源标识。 */
    @Override
    public String getValue() {
        return ProcessTaskSource.PROCESSTASK_TRANSFER_REPORT.getValue();
    }

    /** 转报来源的本地化名称。 */
    @Override
    public String getText() {
        return ProcessTaskSource.PROCESSTASK_TRANSFER_REPORT.getText();
    }

    /** 统一使用转报关系定位原工单，兼容旧来源 ID 的两种含义。 */
    @Override
    public ProcessTaskReportSourceVo getReportSource(ProcessTaskInvokeVo invoke) {
        ProcessTaskTransferReportVo transfer = processTaskMapper.getProcessTaskTransferReportByToProcessTaskId(invoke.getProcessTaskId());
        if (transfer == null || transfer.getFromProcessTaskId() == null) {
            return IProcessTaskSource.super.getReportSource(invoke);
        }
        Long fromId = transfer.getFromProcessTaskId();
        ProcessTaskVo task = processTaskMapper.getProcessTaskBaseInfoById(fromId);
        if (task == null || !new ProcessAuthManager.TaskOperationChecker(fromId, ProcessTaskOperationType.PROCESSTASK_VIEW).build().check()) {
            return IProcessTaskSource.super.getReportSource(invoke);
        }
        return new ProcessTaskReportSourceVo(task.getSerialNumber() + " " + task.getTitle(), ProcessTaskReportSourceVo.PROCESS_TASK_TARGET,
                new JSONObject().fluentPut("processTaskId", fromId.toString()));
    }
}
