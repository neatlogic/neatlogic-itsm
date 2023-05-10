/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.thread;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
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
public class ProcessTaskAutomaticThread extends NeatLogicThread {

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
