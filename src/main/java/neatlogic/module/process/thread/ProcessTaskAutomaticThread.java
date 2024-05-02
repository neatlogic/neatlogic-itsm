/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
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
