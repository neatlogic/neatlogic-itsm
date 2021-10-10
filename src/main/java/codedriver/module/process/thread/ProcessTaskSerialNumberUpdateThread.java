/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;

public class ProcessTaskSerialNumberUpdateThread extends CodeDriverThread {

    private IProcessTaskSerialNumberPolicyHandler handler;
    private ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo;

    public ProcessTaskSerialNumberUpdateThread(IProcessTaskSerialNumberPolicyHandler handler, ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        super("PROCESSTASK-SERIALNUMBER-UPDATER");
        this.handler = handler;
        this.processTaskSerialNumberPolicyVo = processTaskSerialNumberPolicyVo;
    }

    @Override
    protected void execute() {
        handler.batchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo);
    }

}
