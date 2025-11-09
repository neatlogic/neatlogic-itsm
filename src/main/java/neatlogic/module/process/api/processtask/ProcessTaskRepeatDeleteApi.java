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
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author linbq
 * @since 2021/9/14 11:09
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskRepeatDeleteApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/repeat/delete";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskrepeatdeleteapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "repeatProcessTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.repeatprocesstaskid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source"),
    })
    @Description(desc = "nmpap.processtaskrepeatdeleteapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String source = paramObj.getString("source");
        Long processTaskId = paramObj.getLong("processTaskId");
        Long repeatProcessTaskId = paramObj.getLong("repeatProcessTaskId");
        ProcessTaskVo processTask = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        ProcessTaskVo repeatProcessTask = processTaskService.checkProcessTaskParamsIsLegal(repeatProcessTaskId);
        Long repeatGroupId = processTaskMapper.getRepeatGroupIdByProcessTaskId(repeatProcessTaskId);
        if (repeatGroupId != null) {
            processTaskMapper.deleteProcessTaskRepeatByProcessTaskId(repeatProcessTaskId);
            {
                ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                processTaskStepVo.setProcessTaskId(repeatProcessTaskId);
                processTaskStepVo.getParamObj().put("source", source);
                processTaskStepVo.getParamObj().put("repeatProcessTaskSerialNumber", processTask.getSerialNumber());
                processTaskStepVo.getParamObj().put("repeatProcessTaskTitle", processTask.getTitle());
                processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UNBOUNDREPEAT);
            }
            List<Long> repeatProcessTaskIdList = processTaskMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
            if (repeatProcessTaskIdList.size() == 1) {
                processTaskMapper.deleteProcessTaskRepeatByProcessTaskId(repeatProcessTaskIdList.get(0));
            }
            {
                ProcessTaskStepVo processTaskStep = new ProcessTaskStepVo();
                processTaskStep.setProcessTaskId(processTaskId);
                processTaskStep.getParamObj().put("source", source);
                processTaskStep.getParamObj().put("repeatProcessTaskSerialNumber", repeatProcessTask.getSerialNumber());
                processTaskStep.getParamObj().put("repeatProcessTaskTitle", repeatProcessTask.getTitle());
                processStepHandlerUtil.audit(processTaskStep, ProcessTaskAuditType.UNBINDREPEAT);
            }
        }
        return null;
    }
}
