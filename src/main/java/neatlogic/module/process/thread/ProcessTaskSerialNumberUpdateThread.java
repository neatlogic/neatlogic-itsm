/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.thread;

import neatlogic.framework.asynchronization.thread.CodeDriverThread;
import neatlogic.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class ProcessTaskSerialNumberUpdateThread extends CodeDriverThread {
    private static Logger logger = LoggerFactory.getLogger(ProcessTaskSerialNumberUpdateThread.class);

    private static ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Resource
    public void setProcessTaskSerialNumberMapper(ProcessTaskSerialNumberMapper _processTaskSerialNumberMapper) {
        processTaskSerialNumberMapper = _processTaskSerialNumberMapper;
    }
    private IProcessTaskSerialNumberPolicyHandler handler;
    private ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo;

    public ProcessTaskSerialNumberUpdateThread() {
        super("PROCESSTASK-SERIALNUMBER-UPDATER");
    }

    public ProcessTaskSerialNumberUpdateThread(IProcessTaskSerialNumberPolicyHandler handler, ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        super("PROCESSTASK-SERIALNUMBER-UPDATER");
        this.handler = handler;
        this.processTaskSerialNumberPolicyVo = processTaskSerialNumberPolicyVo;
    }

    @Override
    protected void execute() {
        try {
            handler.batchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicyEndTimeByChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        }
    }

}
