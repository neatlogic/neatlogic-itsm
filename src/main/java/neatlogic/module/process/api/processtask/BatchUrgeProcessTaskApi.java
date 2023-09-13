/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.service.ProcessTaskAgentService;
import neatlogic.framework.process.stephandler.core.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class BatchUrgeProcessTaskApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private ProcessTaskService processTaskService;
    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;
    @Resource
    private ProcessTaskAgentService processTaskAgentService;

    @Override
    public String getName() {
        return "批量催办工单";
    }

    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "工单Id列表"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
    })
    @Output({})
    @Description(desc = "批量催办工单")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String source = paramObj.getString("source");
        List<Long> processTaskIdList = paramObj.getJSONArray("processTaskIdList").toJavaList(Long.class);
        for (Long processTaskId : processTaskIdList) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
            if (processTaskVo == null) {
                return null;
            }
            new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_URGE)
                    .build()
                    .checkAndNoPermissionThrowException();
            List<ProcessTaskStepVo> processTaskStepList = processTaskService.getUrgeableStepList(processTaskVo, UserContext.get().getUserUuid(true));
            /** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
            List<String> fromUserUUidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
            for (String userUuid : fromUserUUidList) {
                processTaskStepList.addAll(processTaskService.getUrgeableStepList(processTaskVo, userUuid));
            }
            for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                /** 触发通知 **/
                processStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepNotifyTriggerType.URGE);
                processStepHandlerUtil.action(processTaskStepVo, ProcessTaskStepNotifyTriggerType.URGE);
            }
            // 催办记录
            processTaskMapper.insertProcessTaskUrge(processTaskId, UserContext.get().getUserUuid(true));
            /*生成催办活动*/
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskId);
            processTaskStepVo.getParamObj().put("source", source);
            processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.URGE);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "processtask/batch/urge";
    }
}
