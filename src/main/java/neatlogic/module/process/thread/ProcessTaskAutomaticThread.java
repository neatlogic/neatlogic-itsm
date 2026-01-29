/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.thread;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskAutomaticService;
import neatlogic.module.process.service.ProcessTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author linbq
 * @since 2021/12/15 18:10
 **/
@Component
public class ProcessTaskAutomaticThread extends NeatLogicThread {

    private final static Logger logger = LoggerFactory.getLogger(ProcessTaskAutomaticThread.class);

    private static ProcessTaskAutomaticService processTaskAutomaticService;
    private static ProcessTaskMapper processTaskMapper;
    private static ProcessTaskService processTaskService;
    private Long processTaskStepInOperationId;

    @Resource
    private void setProcessTaskAutomaticService(ProcessTaskAutomaticService _processTaskAutomaticService) {
        processTaskAutomaticService = _processTaskAutomaticService;
    }
    @Resource
    private void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }
    @Resource
    private void setProcessTaskService(ProcessTaskService _processTaskService) {
        processTaskService = _processTaskService;
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            processTaskService.deleteProcessTaskStepInOperationById(processTaskStepInOperationId);
        }
    }
}
