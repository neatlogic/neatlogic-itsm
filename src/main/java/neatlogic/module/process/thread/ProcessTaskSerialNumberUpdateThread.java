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
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class ProcessTaskSerialNumberUpdateThread extends NeatLogicThread {
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
