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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class BatchDeleteProcessTaskApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getName() {
        return "nmpap.batchdeleteprocesstaskapi.getname";
    }

    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "term.itsm.processtaskidlist"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source")
    })
    @Output({})
    @Description(desc = "nmpap.batchdeleteprocesstaskapi.getname")
    @ResubmitInterval(3)
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<Long> processTaskIdList = paramObj.getJSONArray("processTaskIdList").toJavaList(Long.class);
        List<ProcessTaskVo> withoutAuthTaskIdList = new ArrayList<>();
        for (Long processTaskId : processTaskIdList) {
            // 锁住当前工单
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskVoLockById(processTaskId);
            if (processTaskVo == null || processTaskVo.getIsDeleted() == 1) {
                continue;
            }
            boolean flag = new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_DELETE)
                    .build()
                    .check();
            if (flag) {
                // is_deleted置为1
                processTaskMapper.updateProcessTaskIsDeletedById(processTaskId, 1);
                ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo(processTaskId, null);
                processStepHandlerUtil.action(processTaskStepVo, ProcessTaskNotifyTriggerType.DELETEPROCESSTASK);
                processStepHandlerUtil.notify(processTaskStepVo, ProcessTaskNotifyTriggerType.DELETEPROCESSTASK);
            }else{
                withoutAuthTaskIdList.add(processTaskVo);
            }
        }
        return withoutAuthTaskIdList;
    }

    @Override
    public String getToken() {
        return "processtask/batch/delete";
    }

    @Override
    public int needAudit() {
        return 1;
    }
}
