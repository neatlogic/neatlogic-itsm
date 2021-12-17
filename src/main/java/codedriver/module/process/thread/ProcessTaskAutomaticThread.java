/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.thread;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskAutomaticService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author linbq
 * @since 2021/12/15 18:10
 **/
@Component
public class ProcessTaskAutomaticThread extends CodeDriverThread {

    private static ProcessTaskAutomaticService processTaskAutomaticService;

    @Resource
    private void setProcessTaskAutomaticService(ProcessTaskAutomaticService _processTaskAutomaticService) {
        processTaskAutomaticService = _processTaskAutomaticService;
    }
    private ProcessTaskStepVo currentProcessTaskStepVo;

    public ProcessTaskAutomaticThread() {
        super("ProcessTask-Automatic-Thread");
    }

    public ProcessTaskAutomaticThread(ProcessTaskStepVo currentProcessTaskStepVo) {
        super("ProcessTask-Automatic-Thread-" + currentProcessTaskStepVo.getId());
        this.currentProcessTaskStepVo = currentProcessTaskStepVo;
    }
    @Override
    protected void execute() {
        processTaskAutomaticService.firstRequest(currentProcessTaskStepVo);
    }
}
