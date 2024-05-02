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

package neatlogic.module.process.autocompleterule.handler;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.TransactionSynchronizationPool;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.autocompleterule.core.IAutoCompleteRuleHandler;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepInOperationVo;
import neatlogic.framework.process.dto.ProcessTaskStepUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.stephandler.core.*;
import neatlogic.framework.service.AuthenticationInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


/**
 * @author linbq
 * @since 2021/10/29 16:20
 **/
@Component
public class AutoCompleteHandler implements IAutoCompleteRuleHandler {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getHandler() {
        return "autoComplete";
    }

    @Override
    public String getName() {
        return "自动流转";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean execute(ProcessTaskStepVo currentProcessTaskStepVo) {
        //如果当前步骤有多条可流转路径时，自动流转不生效
        List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(currentProcessTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
        if (nextStepIdList.size() != 1) {
            return false;
        }
        if (Objects.equals(currentProcessTaskStepVo.getIsActive(), 1)) {
            if (ProcessTaskStepStatus.RUNNING.getValue().equals(currentProcessTaskStepVo.getStatus())) {
                List<ProcessTaskStepUserVo> stepUserVoList =  processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
                if (stepUserVoList.size() == 1) {
                    UserVo currentUserVo = userMapper.getUserBaseInfoByUuid(stepUserVoList.get(0).getUserUuid());
                    IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                    ProcessStepThread thread = new ProcessStepThread(currentProcessTaskStepVo, currentUserVo) {
                        @Override
                        public void myExecute() {
                            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(currentUserVo.getUuid());
                            UserContext.init(currentUserVo, authenticationInfoVo, SystemUser.SYSTEM.getTimezone());
                            currentProcessTaskStepVo.getParamObj().put("action", "complete");
                            handler.complete(currentProcessTaskStepVo);
                        }
                    };
                    ProcessTaskStepInOperationVo processTaskStepInOperationVo = new ProcessTaskStepInOperationVo(
                            currentProcessTaskStepVo.getProcessTaskId(),
                            currentProcessTaskStepVo.getId(),
                            ProcessTaskOperationType.STEP_COMPLETE.getValue()
                    );
                    IProcessStepInternalHandler processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                    if (processStepInternalHandler == null) {
                        throw new ProcessStepUtilHandlerNotFoundException(currentProcessTaskStepVo.getHandler());
                    }
                    processStepInternalHandler.insertProcessTaskStepInOperation(processTaskStepInOperationVo);
                    thread.setSupplier(() -> processTaskMapper.deleteProcessTaskStepInOperationById(processTaskStepInOperationVo.getId()));
                    TransactionSynchronizationPool.execute(thread);
                    return true;
                }
            }
        }

        return false;
    }
}
