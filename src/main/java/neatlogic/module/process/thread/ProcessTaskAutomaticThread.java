/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.thread;

import neatlogic.framework.asynchronization.thread.CodeDriverThread;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.service.ProcessTaskAutomaticService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author linbq
 * @since 2021/12/15 18:10
 **/
@Component
public class ProcessTaskAutomaticThread extends CodeDriverThread {

    private static ProcessTaskAutomaticService processTaskAutomaticService;
    private static ProcessTaskMapper processTaskMapper;
    private Long processTaskStepInOperationId;

    @Resource
    private void setProcessTaskAutomaticService(ProcessTaskAutomaticService _processTaskAutomaticService) {
        processTaskAutomaticService = _processTaskAutomaticService;
    }
    @Resource
    private void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }
    private ProcessTaskStepVo currentProcessTaskStepVo;

    public ProcessTaskAutomaticThread() {
        super("ProcessTask-Automatic-Thread");
    }

    public ProcessTaskAutomaticThread(ProcessTaskStepVo currentProcessTaskStepVo, Long _processTaskStepInOperationId) {
        super("ProcessTask-Automatic-Thread-" + currentProcessTaskStepVo.getId());
        this.currentProcessTaskStepVo = currentProcessTaskStepVo;
        this.processTaskStepInOperationId = _processTaskStepInOperationId;
    }
    @Override
    protected void execute() {
        try {
            processTaskAutomaticService.firstRequest(currentProcessTaskStepVo);
        } finally {
            processTaskMapper.deleteProcessTaskStepInOperationById(processTaskStepInOperationId);
        }
    }
}
