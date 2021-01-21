package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;

public class ProcessTaskSerialNumberUpdateThread extends CodeDriverThread {

    private IProcessTaskSerialNumberPolicyHandler handler;
    private ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo;

    public ProcessTaskSerialNumberUpdateThread(IProcessTaskSerialNumberPolicyHandler handler, ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        this.handler = handler;
        this.processTaskSerialNumberPolicyVo = processTaskSerialNumberPolicyVo;
    }

    @Override
    protected void execute() {
        handler.batchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo);
    }

}
