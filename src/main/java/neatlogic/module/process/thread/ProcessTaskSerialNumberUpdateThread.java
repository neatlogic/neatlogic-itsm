/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.thread;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
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
