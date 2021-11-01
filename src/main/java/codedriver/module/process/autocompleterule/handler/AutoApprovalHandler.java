/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.autocompleterule.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.asynchronization.threadpool.TransactionSynchronizationPool;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.autocompleterule.core.IAutoCompleteRuleHandler;
import codedriver.framework.process.constvalue.AutoCompleteType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTagMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepTagVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.stephandler.core.ProcessStepThread;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author linbq
 * @since 2021/10/29 16:22
 **/
@Component
public class AutoApprovalHandler implements IAutoCompleteRuleHandler {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTagMapper processTagMapper;

    @Resource
    private UserMapper userMapper;


    @Override
    public String getHandler() {
        return AutoCompleteType.AUTOAPPROVAL.getValue();
    }

    @Override
    public void execute(ProcessTaskStepVo currentProcessTaskStepVo) {
        if (Objects.equals(currentProcessTaskStepVo.getIsActive(), 1)) {
            if (ProcessTaskStatus.RUNNING.getValue().equals(currentProcessTaskStepVo.getStatus())) {
                List<ProcessTaskStepUserVo> stepUserVoList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
                if (stepUserVoList.size() == 1) {
                    Long tagId = processTagMapper.getProcessTagIdByName("审批");
                    if (tagId != null) {
                        ProcessTaskStepTagVo processTaskStepTagVo = new ProcessTaskStepTagVo();
                        processTaskStepTagVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
                        processTaskStepTagVo.setTagId(tagId);
                        if (processTaskMapper.checkProcessTaskStepTagIsExists(processTaskStepTagVo) > 0) {
                            Long startStepId = currentProcessTaskStepVo.getFromProcessTaskStepId();
                            if (startStepId != null) {
                                ProcessTaskStepVo startStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(startStepId);
                                if (startStepVo != null) {
                                    if (Objects.equals(startStepVo.getIsActive(), 2)) {
                                        if (ProcessTaskStatus.SUCCEED.getValue().equals(startStepVo.getStatus())) {
                                            List<ProcessTaskStepUserVo> startStepUserVoList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getStartProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                                            if (startStepUserVoList.size() == 1) {
                                                if (Objects.equals(startStepUserVoList.get(0).getUserUuid(), stepUserVoList.get(0).getUserUuid())) {
                                                    UserVo currentUserVo = userMapper.getUserBaseInfoByUuid(stepUserVoList.get(0).getUserUuid());
                                                    IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
                                                    TransactionSynchronizationPool.execute(new ProcessStepThread(currentProcessTaskStepVo, currentUserVo) {
                                                        @Override
                                                        public void myExecute() {
                                                            UserContext.init(currentUserVo, SystemUser.SYSTEM.getTimezone());
                                                            currentProcessTaskStepVo.getParamObj().put("action", "complete");
                                                            handler.complete(currentProcessTaskStepVo);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
